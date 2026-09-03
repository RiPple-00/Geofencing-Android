package com.example.geofencing.ui.dashboard

import com.example.geofencing.data.remote.supabase.SbCart
import com.example.geofencing.data.remote.supabase.SbEvent
import com.example.geofencing.data.remote.supabase.SupabaseApi
import com.example.geofencing.ui.common.LoadState
import com.example.geofencing.ui.common.map
import com.example.geofencing.ui.components.StatusKind
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

// Temporary Supabase-backed implementation for the dashboard UI.
// It keeps one app-scoped cache so WholeSector, Sector, and Cart screens share the same snapshot.
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class SupabaseDashboardRepository @Inject constructor(
    private val api: SupabaseApi
) : DashboardRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val refreshTrigger = MutableStateFlow(0)

    private val cached: StateFlow<LoadState<List<DashboardSector>>> =
        refreshTrigger
            .flatMapLatest {
                flow {
                    emit(LoadState.Loading)
                    val result: LoadState<List<DashboardSector>> = try {
                        LoadState.Success(loadSectors())
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        LoadState.Error(e.message ?: "Failed to load data")
                    }
                    emit(result)
                }
            }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), LoadState.Loading)

    override fun refresh() {
        refreshTrigger.value++
    }

    override fun observeSectors(): Flow<LoadState<List<DashboardSector>>> = cached

    override fun observeSector(name: String): Flow<LoadState<DashboardSector?>> =
        cached.map { state -> state.map { list -> list.find { it.name == name } } }

    override fun observeCart(sectorName: String, cartId: String): Flow<LoadState<DashboardCart?>> =
        cached.map { state ->
            state.map { list -> list.find { it.name == sectorName }?.carts?.find { it.id == cartId } }
        }

    private suspend fun loadSectors(): List<DashboardSector> {
        val sectors = api.getSectors()
        val events = api.getEvents()
        val cartsBySector = api.getCarts().groupBy { it.sectorId }
        val eventsBySector = events.groupBy { it.sectorId }
        val latestEventByCart: Map<Long, SbEvent> = events
            .groupBy { it.cartId }
            .mapValues { (_, evs) -> evs.maxByOrNull { parseInstant(it.occurredAt) ?: Instant.MIN }!! }

        return sectors.map { sector ->
            val geofence = geoJsonRingToLatLng(sector.geofence.coordinates)
            val sectorCarts = cartsBySector[sector.id].orEmpty()
            val violationPoints = eventsBySector[sector.id].orEmpty()
                .map { LatLng(it.location.coordinates[1], it.location.coordinates[0]) }
            DashboardSector(
                id = Math.toIntExact(sector.id),
                name = sector.name,
                address = sector.address,
                totalCarts = sectorCarts.size,
                geofence = geofence,
                carts = placeCarts(geofence, sectorCarts, latestEventByCart),
                violationPoints = violationPoints
            )
        }
    }

    private fun placeCarts(
        geofence: List<LatLng>,
        carts: List<SbCart>,
        latestEventByCart: Map<Long, SbEvent>
    ): List<DashboardCart> {
        if (geofence.isEmpty()) return emptyList()
        val now = Instant.now()
        val positions = synthesizePositions(geofence, carts)
        return carts.mapIndexed { i, cart ->
            toDashboardCart(cart, positions[i], latestEventByCart[cart.id], now)
        }
    }

    // 실 좌표(lat/lng)가 있으면 그대로, 없으면 geofence 박스 기준으로 합성한다
    // (위반=경계 밖, 그 외=안쪽). 모든 카트가 마커 위치를 갖도록 인덱스를 순환한다.
    private fun synthesizePositions(geofence: List<LatLng>, carts: List<SbCart>): List<LatLng> {
        val centerLat = geofence.map { it.latitude }.average()
        val centerLng = geofence.map { it.longitude }.average()
        val halfLat = (geofence.maxOf { it.latitude } - geofence.minOf { it.latitude }) / 2
        val halfLng = (geofence.maxOf { it.longitude } - geofence.minOf { it.longitude }) / 2
        val insideFracs = listOf(
            0.0 to 0.0, 0.25 to 0.3, -0.3 to 0.2, 0.2 to -0.3,
            -0.25 to -0.2, 0.35 to 0.1, -0.15 to 0.35, 0.1 to -0.4
        )
        val outsideFracs = listOf(1.3 to 0.2, -0.2 to 1.3, 1.2 to -0.9, -1.1 to -0.7)
        var insideIdx = 0
        var outsideIdx = 0
        return carts.map { cart ->
            if (cart.lat != null && cart.lng != null) {
                LatLng(cart.lat, cart.lng)
            } else {
                val (fracLat, fracLng) = if (cart.geofenceStatus == "violating") {
                    outsideFracs[outsideIdx++ % outsideFracs.size]
                } else {
                    insideFracs[insideIdx++ % insideFracs.size]
                }
                LatLng(centerLat + fracLat * halfLat, centerLng + fracLng * halfLng)
            }
        }
    }

    private fun toDashboardCart(cart: SbCart, position: LatLng, event: SbEvent?, now: Instant): DashboardCart {
        val status = statusOf(cart.geofenceStatus)
        val eventAt = event?.let { parseInstant(it.occurredAt) }
        // 위반 상세는 위반 상태일 때만 채운다(그 외 전부 null). 단일 분기로 4개 필드를 함께 계산.
        val violation = if (status == StatusKind.Violation) violationInfo(event, eventAt, now) else null
        return DashboardCart(
            id = cart.name,
            position = position,
            status = status,
            drivingState = if (cart.drivingStatus == "driving") "Driving" else "Idle",
            registeredId = "SB-%03d".format(cart.id),
            timestamp = eventAt?.let { timeFormatter.format(it) } ?: "",
            violationDuration = violation?.duration,
            maxSpeed = violation?.maxSpeed,
            violationAtTime = violation?.atTime,
            violationAtAddress = violation?.atAddress
        )
    }

    private fun statusOf(geofenceStatus: String): StatusKind = when (geofenceStatus) {
        "violating" -> StatusKind.Violation
        "disconnected" -> StatusKind.Disconnect
        else -> StatusKind.Compliance
    }

    private class ViolationInfo(
        val duration: String,
        val maxSpeed: String,
        val atTime: String,
        val atAddress: String
    )

    private fun violationInfo(event: SbEvent?, eventAt: Instant?, now: Instant) = ViolationInfo(
        duration = eventAt?.let { formatDuration(Duration.between(it, now)) } ?: "—",
        maxSpeed = event?.maxSpeed ?: "—",
        atTime = eventAt?.let { timeFormatter.format(it) } ?: "—",
        atAddress = event?.address ?: "—"
    )

    private companion object {
        val timeFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss").withZone(ZoneId.systemDefault())
    }
}

// Converts the GeoJSON polygon outer ring from [lng, lat] pairs into LatLng(lat, lng).
// A closing point that repeats the first vertex is dropped.
internal fun geoJsonRingToLatLng(coordinates: List<List<List<Double>>>): List<LatLng> {
    val ring = coordinates.firstOrNull().orEmpty().map { LatLng(it[1], it[0]) }
    return if (ring.size > 1 && ring.first() == ring.last()) ring.dropLast(1) else ring
}

// Parses offset timestamps first, then UTC instants; invalid values become null.
internal fun parseInstant(value: String): Instant? = runCatching {
    OffsetDateTime.parse(value).toInstant()
}.recoverCatching { Instant.parse(value) }.getOrNull()

// Formats violation elapsed time. Negative values are clamped to zero for clock skew.
internal fun formatDuration(elapsed: Duration): String {
    val seconds = elapsed.seconds.coerceAtLeast(0)
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) "%dh %02dm".format(hours, minutes) else "%dm %02ds".format(minutes, secs)
}
