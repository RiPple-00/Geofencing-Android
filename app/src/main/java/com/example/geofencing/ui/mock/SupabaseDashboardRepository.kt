package com.example.geofencing.ui.mock

import com.example.geofencing.data.remote.supabase.SbCart
import com.example.geofencing.data.remote.supabase.SbEvent
import com.example.geofencing.data.remote.supabase.SupabaseApi
import com.example.geofencing.ui.common.LoadState
import com.example.geofencing.ui.common.map
import com.example.geofencing.ui.components.StatusKind
import com.google.android.gms.maps.model.LatLng
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

// DashboardRepository의 Supabase(PostgREST) 구현 — 팀 BE 전까지의 임시 실백엔드.
// sectors/carts/geofence_events를 읽어 UI 도메인(MockSector/MockCart)으로 조립한다.
//
// 캐시: 한 번 fetch한 결과를 앱-스코프 StateFlow로 공유 → 세 화면(전체/섹터/카트)이 같은 데이터를
//   재사용(드릴다운마다 재요청 안 함). refresh()로 재조회(에러 재시도/새로고침). WhileSubscribed라
//   구독자가 있는 동안 유지된다. 실패는 LoadState.Error로 실어 스트림이 죽지 않게 한다.
//
// 값 처리:
//   - 위치: carts.lat/lng 사용, 없으면 geofence+상태로 합성(위반=경계 밖, 그 외=안쪽)
//   - 위반 지속시간: 발생 후 경과 시간(now - occurred_at)으로 계산. 발생시각·속도·주소: 이벤트 컬럼
//   - disconnect 상태: 스키마에 없음(violating/compliant 뿐)
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class SupabaseDashboardRepository @Inject constructor(
    private val api: SupabaseApi
) : DashboardRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val refreshTrigger = MutableStateFlow(0)

    // 세 화면이 공유하는 단일 캐시. refresh마다 Loading → Success/Error를 방출한다.
    private val cached: StateFlow<LoadState<List<MockSector>>> =
        refreshTrigger
            .flatMapLatest {
                flow {
                    emit(LoadState.Loading)
                    emit(
                        runCatching { loadSectors() }.fold(
                            onSuccess = { LoadState.Success(it) },
                            onFailure = { LoadState.Error(it.message ?: "Failed to load data") }
                        )
                    )
                }
            }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), LoadState.Loading)

    override fun refresh() {
        refreshTrigger.value++
    }

    override fun observeSectors(): Flow<LoadState<List<MockSector>>> = cached

    override fun observeSector(name: String): Flow<LoadState<MockSector?>> =
        cached.map { state -> state.map { list -> list.find { it.name == name } } }

    override fun observeCart(sectorName: String, cartId: String): Flow<LoadState<MockCart?>> =
        cached.map { state ->
            state.map { list -> list.find { it.name == sectorName }?.carts?.find { it.id == cartId } }
        }

    private suspend fun loadSectors(): List<MockSector> {
        val sectors = api.getSectors()
        val events = api.getEvents()
        val cartsBySector = api.getCarts().groupBy { it.sectorId }
        val eventsBySector = events.groupBy { it.sectorId }
        // 카트별 최신 위반 이벤트(지속시간·속도·주소용).
        val latestEventByCart: Map<Int, SbEvent> = events
            .groupBy { it.cartId }
            .mapValues { (_, evs) -> evs.maxByOrNull { parseInstant(it.occurredAt) ?: Instant.MIN }!! }

        return sectors.map { sector ->
            // GeoJSON 외곽 링 → LatLng. [lng,lat] 순서 뒤집고, 닫힘점(첫=마지막) 제거.
            val geofence = sector.geofence.coordinates.firstOrNull().orEmpty()
                .map { LatLng(it[1], it[0]) }
                .let { ring -> if (ring.size > 1 && ring.first() == ring.last()) ring.dropLast(1) else ring }
            val sectorCarts = cartsBySector[sector.id].orEmpty()
            val violationPoints = eventsBySector[sector.id].orEmpty()
                .map { LatLng(it.location.coordinates[1], it.location.coordinates[0]) }
            MockSector(
                id = sector.id,
                name = sector.name,
                address = sector.address,
                totalCarts = sectorCarts.size,
                geofence = geofence,
                carts = placeCarts(geofence, sectorCarts, latestEventByCart),
                violationPoints = violationPoints
            )
        }
    }

    // 실 좌표(lat/lng)가 있으면 사용, 없으면 geofence 중심 기준으로 상태별 위치를 합성한다.
    // 위반 카트의 상세는 최신 이벤트 컬럼(백엔드 값)에서 가져온다.
    private fun placeCarts(
        geofence: List<LatLng>,
        carts: List<SbCart>,
        latestEventByCart: Map<Int, SbEvent>
    ): List<MockCart> {
        if (geofence.isEmpty()) return emptyList()
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
        val now = Instant.now()
        return carts.map { cart ->
            val status = if (cart.geofenceStatus == "violating") StatusKind.Violation else StatusKind.Compliance
            val position = if (cart.lat != null && cart.lng != null) {
                LatLng(cart.lat, cart.lng)
            } else {
                val (fracLat, fracLng) = if (status == StatusKind.Violation) {
                    outsideFracs[outsideIdx++ % outsideFracs.size]
                } else {
                    insideFracs[insideIdx++ % insideFracs.size]
                }
                LatLng(centerLat + fracLat * halfLat, centerLng + fracLng * halfLng)
            }
            val event = latestEventByCart[cart.id]
            val eventAt = event?.let { parseInstant(it.occurredAt) }
            MockCart(
                id = cart.name,
                position = position,
                status = status,
                drivingState = if (cart.drivingStatus == "driving") "Driving" else "Idle",
                registeredId = "SB-%03d".format(cart.id),
                timestamp = eventAt?.let { timeFormatter.format(it) } ?: "",
                // 위반일 때만 채움. 지속시간은 발생 후 경과 시간(now - occurred_at), 나머지는 이벤트 컬럼.
                violationDuration = if (status == StatusKind.Violation) {
                    eventAt?.let { formatDuration(Duration.between(it, now)) } ?: "—"
                } else null,
                maxSpeed = if (status == StatusKind.Violation) event?.maxSpeed ?: "—" else null,
                violationAtTime = if (status == StatusKind.Violation) {
                    eventAt?.let { timeFormatter.format(it) } ?: "—"
                } else null,
                violationAtAddress = if (status == StatusKind.Violation) event?.address ?: "—" else null
            )
        }
    }

    // "8m 45s" 또는 1시간 이상이면 "2h 05m" 형식.
    private fun formatDuration(elapsed: Duration): String {
        val seconds = elapsed.seconds.coerceAtLeast(0)
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) "%dh %02dm".format(hours, minutes) else "%dm %02ds".format(minutes, secs)
    }

    private fun parseInstant(value: String): Instant? = runCatching {
        OffsetDateTime.parse(value).toInstant()
    }.recoverCatching { Instant.parse(value) }.getOrNull()

    private companion object {
        val timeFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss").withZone(ZoneId.systemDefault())
    }
}
