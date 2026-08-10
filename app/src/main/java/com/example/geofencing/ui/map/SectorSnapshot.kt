package com.example.geofencing.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.geofencing.R
import com.example.geofencing.ui.theme.extendedColors
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.sin
import com.google.android.gms.maps.GoogleMap as GmsGoogleMap

// 고정 프레임 지도(썸네일 · Sector)용 스냅샷: "타일만" 캡처하고, 오버레이(경계/카트)는 Canvas로.
// 캡처 시점의 카메라를 함께 저장해(StoredProjector) 나중에 LatLng→픽셀 투영을 재현한다.
// 캐시 키에 표시 크기(dp)를 넣어 크기가 다른 소비자(썸네일 160dp vs Sector 240dp)가 섞이지 않게 한다.

// 스냅샷 시점 카메라의 화면 매핑 정보(북-업 기준). bitmap의 (0,0)=NW, (w,h)=SE.
data class SnapshotProjection(
    val south: Double, val west: Double,
    val north: Double, val east: Double,
    val widthPx: Int, val heightPx: Int
)

data class SectorSnapshot(val bitmap: ImageBitmap, val projection: SnapshotProjection)

// 저장된 카메라로 LatLng→캡처 픽셀 투영(Web Mercator). 카메라 고정이라 계속 유효.
class StoredProjector(private val p: SnapshotProjection) : MapProjector {
    private fun worldX(lng: Double) = (lng + 180.0) / 360.0
    private fun worldY(lat: Double): Double {
        val s = sin(lat * PI / 180.0).coerceIn(-0.9999, 0.9999)
        return 0.5 - ln((1 + s) / (1 - s)) / (4 * PI)
    }

    private val x0 = worldX(p.west)
    private val x1 = worldX(p.east)
    private val y0 = worldY(p.north) // 위(top)
    private val y1 = worldY(p.south) // 아래(bottom)

    override fun project(latLng: LatLng): Offset {
        val fx = (worldX(latLng.longitude) - x0) / (x1 - x0)
        val fy = (worldY(latLng.latitude) - y0) / (y1 - y0)
        return Offset((fx * p.widthPx).toFloat(), (fy * p.heightPx).toFloat())
    }
}

data class SectorSnapshotRequest(val sectorId: Int, val geofence: List<LatLng>)

// 스냅샷 캡처 로직(geofence fit 박스 등)이 바뀌면 올린다. 키에 붙어 옛 캐시를 무효화하고, prune으로 삭제.
private const val SnapshotCacheVersion = "v8"

// 스냅샷에서 geofence를 담는 최대 박스(WholeSector 카드 · Sector 페이지 지도 공통 축척).
// 높이는 카드에서 geofence가 위/아래 글자와 ~15dp 간격이 나도록 축소. TODO(측정): 미세 조정.
val GeofenceFitMaxWidth = 208.dp
val GeofenceFitMaxHeight = 110.dp

// 메모리(관찰 가능) + 디스크(png + .meta) 2단 캐시. 키 = 섹터 id + geofence 해시 + 표시 크기(dp).
object SectorSnapshotCache {
    private val memory = mutableStateMapOf<String, SectorSnapshot>()

    // 키 = 섹터 id + geofence 해시 + 표시 크기(dp) + 버전.
    // geofence가 바뀌면 hashCode가 바뀌어 자동으로 다른 키 → 새 스냅샷 재생성(옛 것은 매칭 안 됨).
    // 참고: 같은 크기에서 서로 다른 fit을 쓰면 충돌하니, 그럴 땐 fit도 키에 넣어야 함(현재는 화면별 크기가 달라 안전).
    fun keyOf(sectorId: Int, geofence: List<LatLng>, widthDp: Int, heightDp: Int): String =
        "sector_${sectorId}_${geofence.hashCode()}_${widthDp}x${heightDp}_$SnapshotCacheVersion"

    private fun dir(context: Context): File =
        File(context.cacheDir, "sector_snapshots").apply { mkdirs() }

    private fun pngFile(context: Context, key: String) = File(dir(context), "$key.png")
    private fun metaFile(context: Context, key: String) = File(dir(context), "$key.meta")

    fun peek(key: String): SectorSnapshot? = memory[key]

    fun isCached(context: Context, key: String): Boolean =
        memory.containsKey(key) || (pngFile(context, key).exists() && metaFile(context, key).exists())

    // 현재 버전이 아닌(옛) 스냅샷 파일 삭제. 프로세스당 1회.
    // 주의: 같은 버전인데 geofence가 바뀐 옛 파일은 여기서 안 지워짐(orphan) — 백엔드 연동 시 LRU/용량 상한 필요.
    @Volatile
    private var pruned = false

    suspend fun pruneStale(context: Context) {
        if (pruned) return
        pruned = true
        withContext(Dispatchers.IO) {
            dir(context).listFiles()?.forEach { f ->
                if (!f.name.contains("_$SnapshotCacheVersion.")) f.delete()
            }
        }
    }

    suspend fun loadIntoMemory(context: Context, key: String) {
        if (memory.containsKey(key)) return
        val snap = withContext(Dispatchers.IO) {
            val png = pngFile(context, key)
            val meta = metaFile(context, key)
            if (!png.exists() || !meta.exists()) return@withContext null
            val parts = meta.readText().split(",")
            if (parts.size < 6) return@withContext null
            val bmp = BitmapFactory.decodeFile(png.absolutePath) ?: return@withContext null
            SectorSnapshot(
                bmp.asImageBitmap(),
                SnapshotProjection(
                    south = parts[0].toDouble(), west = parts[1].toDouble(),
                    north = parts[2].toDouble(), east = parts[3].toDouble(),
                    widthPx = parts[4].toInt(), heightPx = parts[5].toInt()
                )
            )
        } ?: return
        memory[key] = snap
    }

    suspend fun store(context: Context, key: String, bitmap: Bitmap, projection: SnapshotProjection) {
        withContext(Dispatchers.IO) {
            pngFile(context, key).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            metaFile(context, key).writeText(
                "${projection.south},${projection.west},${projection.north}," +
                    "${projection.east},${projection.widthPx},${projection.heightPx}"
            )
        }
        memory[key] = SectorSnapshot(bitmap.asImageBitmap(), projection)
    }
}

// 캐시된 스냅샷(bitmap + projection) 또는 null. 디스크 로드는 백그라운드.
@Composable
fun rememberSectorSnapshot(sectorId: Int, geofence: List<LatLng>, widthDp: Int, heightDp: Int): SectorSnapshot? {
    val context = LocalContext.current
    val key = remember(sectorId, geofence, widthDp, heightDp) {
        SectorSnapshotCache.keyOf(sectorId, geofence, widthDp, heightDp)
    }
    LaunchedEffect(key) { SectorSnapshotCache.loadIntoMemory(context, key) }
    return SectorSnapshotCache.peek(key)
}

// 고정 프레임 지도: 캐시된 타일 Bitmap 배경 + Canvas 오버레이(경계/카트). 썸네일 · Sector가 사용.
// autoGenerate=true면 캐시 미스 시 스스로 오프스크린 캡처(단일 인스턴스 화면용, 예: Sector).
// 썸네일 리스트는 autoGenerate=false로 두고 SectorSnapshotPrefetcher가 한 번에 미리 생성.
@Composable
fun FixedGeofenceMap(
    content: GeofenceMapContent,
    sectorId: Int,
    modifier: Modifier = Modifier,
    autoGenerate: Boolean = false,
    // geofence 최대 박스(null=캡처 전체). autoGenerate 캡처 시 이 축척으로 잡는다.
    fitWidth: Dp? = null,
    fitHeight: Dp? = null,
    placeholderColor: Color = MaterialTheme.extendedColors.fillPrimary
) {
    val density = LocalDensity.current
    BoxWithConstraints(modifier = modifier) {
        val widthDp = with(density) { constraints.maxWidth.toDp().value }.roundToInt()
        val heightDp = with(density) { constraints.maxHeight.toDp().value }.roundToInt()
        val snap = rememberSectorSnapshot(sectorId, content.geofence, widthDp, heightDp)
        if (snap != null) {
            val base = remember(snap) { StoredProjector(snap.projection) }
            val sx = constraints.maxWidth.toFloat() / snap.projection.widthPx
            val sy = constraints.maxHeight.toFloat() / snap.projection.heightPx
            val projector = MapProjector { ll ->
                val o = base.project(ll)
                Offset(o.x * sx, o.y * sy)
            }
            Image(
                bitmap = snap.bitmap,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            GeofenceMapOverlay(content = content, projector = projector, modifier = Modifier.fillMaxSize())
        } else {
            Box(Modifier.fillMaxSize().background(placeholderColor))
            if (autoGenerate && content.geofence.size >= 3) {
                SnapshotCapture(
                    request = SectorSnapshotRequest(sectorId, content.geofence),
                    widthDp = widthDp,
                    heightDp = heightDp,
                    fitWidth = fitWidth,
                    fitHeight = fitHeight
                )
            }
        }
    }
}

// 카메라 이동 후 타일이 다 그려질 때까지 대기(ms). / geofence를 화면에 맞출 때 여백(px).
private const val SnapshotSettleMillis = 900L // TODO(튜닝): 디바이스/네트워크에 따라 조정.
private const val SnapshotBoundsPaddingPx = 24
private val OffscreenOffset = 5000.dp

private val SnapshotUiSettings = MapUiSettings(
    compassEnabled = false,
    indoorLevelPickerEnabled = false,
    mapToolbarEnabled = false,
    myLocationButtonEnabled = false,
    rotationGesturesEnabled = false,
    scrollGesturesEnabled = false,
    scrollGesturesEnabledDuringRotateOrZoom = false,
    tiltGesturesEnabled = false,
    zoomControlsEnabled = false,
    zoomGesturesEnabled = false
)

// 오프스크린 맵 1개로 타일만 캡처 + 투영 저장 후 onCaptured. 프리페처/자동생성이 공용.
@OptIn(MapsComposeExperimentalApi::class)
@Composable
private fun SnapshotCapture(
    request: SectorSnapshotRequest,
    widthDp: Int,
    heightDp: Int,
    // geofence가 들어갈 최대 박스(null=캡처 전체). 이 박스에 맞게 줌이 자동 조정돼, 남는 상하/좌우는
    // 빈 지도(글자 자리)로 남는다.
    fitWidth: Dp? = null,
    fitHeight: Dp? = null,
    onCaptured: () -> Unit = {}
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val fitWidthPx = with(density) { (fitWidth ?: widthDp.dp).roundToPx() }
    val fitHeightPx = with(density) { (fitHeight ?: heightDp.dp).roundToPx() }
    // fit 박스를 지정하면 그 크기에 딱 맞추고(추가 여백 0), 미지정(전체)이면 기본 여백.
    val fitPaddingPx = if (fitWidth != null || fitHeight != null) 0 else SnapshotBoundsPaddingPx
    var loaded by remember(request, widthDp, heightDp) { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState()
    val mapProperties = remember {
        MapProperties(mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark))
    }
    Box(
        modifier = Modifier
            .offset(x = OffscreenOffset)
            .size(widthDp.dp, heightDp.dp)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = SnapshotUiSettings,
            onMapLoaded = { loaded = true }
        ) {
            MapEffect(loaded) { map ->
                if (!loaded) return@MapEffect
                val bounds = LatLngBounds.builder().apply { request.geofence.forEach(::include) }.build()
                map.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(bounds, fitWidthPx, fitHeightPx, fitPaddingPx)
                )
                delay(SnapshotSettleMillis)
                val visible = map.projection.visibleRegion.latLngBounds
                awaitMapSnapshot(map)?.let { bmp ->
                    SectorSnapshotCache.store(
                        context,
                        SectorSnapshotCache.keyOf(request.sectorId, request.geofence, widthDp, heightDp),
                        bmp,
                        SnapshotProjection(
                            south = visible.southwest.latitude, west = visible.southwest.longitude,
                            north = visible.northeast.latitude, east = visible.northeast.longitude,
                            widthPx = bmp.width, heightPx = bmp.height
                        )
                    )
                }
                onCaptured()
            }
        }
    }
}

// 캐시에 없는 섹터 스냅샷을 "미리" 생성. 오프스크린 지도 1개를 재사용해 카메라만 옮기며 한 번에 전부 캡처한다.
// (섹터마다 지도를 새로 만들지 않아 빠르고, onMapLoaded가 인스턴스당 1회만 발화하는 문제도 피함.)
// 스크롤 레이아웃 밖(페이지 루트 Box 형제)에 둘 것.
@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun SectorSnapshotPrefetcher(
    requests: List<SectorSnapshotRequest>,
    width: Dp,
    height: Dp,
    // geofence 최대 박스(null=캡처 전체). 카드처럼 글자 자리를 비워야 하면 지정.
    fitWidth: Dp? = null,
    fitHeight: Dp? = null
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    // 버전이 바뀌었으면 옛 캐시 파일 정리(프로세스당 1회).
    LaunchedEffect(Unit) { SectorSnapshotCache.pruneStale(context) }
    val widthDp = width.value.roundToInt()
    val heightDp = height.value.roundToInt()
    val pending = remember(requests, widthDp, heightDp) {
        requests.filter {
            it.geofence.size >= 3 &&
                !SectorSnapshotCache.isCached(
                    context,
                    SectorSnapshotCache.keyOf(it.sectorId, it.geofence, widthDp, heightDp)
                )
        }
    }
    if (pending.isEmpty()) return

    // 전부 캡처하면 오프스크린 지도를 컴포지션에서 제거.
    var done by remember(pending) { mutableStateOf(false) }
    if (done) return

    val fitWidthPx = with(density) { (fitWidth ?: widthDp.dp).roundToPx() }
    val fitHeightPx = with(density) { (fitHeight ?: heightDp.dp).roundToPx() }
    val fitPaddingPx = if (fitWidth != null || fitHeight != null) 0 else SnapshotBoundsPaddingPx

    var loaded by remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState()
    val mapProperties = remember {
        MapProperties(mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark))
    }
    Box(
        modifier = Modifier
            .offset(x = OffscreenOffset)
            .size(width, height)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = SnapshotUiSettings,
            onMapLoaded = { loaded = true }
        ) {
            // 지도 1개로 pending 전체를 순회: 카메라 이동 → 타일 대기 → 캡처 → 저장.
            MapEffect(loaded) { map ->
                if (!loaded) return@MapEffect
                for (req in pending) {
                    val key = SectorSnapshotCache.keyOf(req.sectorId, req.geofence, widthDp, heightDp)
                    if (SectorSnapshotCache.isCached(context, key)) continue
                    val bounds = LatLngBounds.builder().apply { req.geofence.forEach(::include) }.build()
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngBounds(bounds, fitWidthPx, fitHeightPx, fitPaddingPx)
                    )
                    delay(SnapshotSettleMillis)
                    val visible = map.projection.visibleRegion.latLngBounds
                    awaitMapSnapshot(map)?.let { bmp ->
                        SectorSnapshotCache.store(
                            context, key, bmp,
                            SnapshotProjection(
                                south = visible.southwest.latitude, west = visible.southwest.longitude,
                                north = visible.northeast.latitude, east = visible.northeast.longitude,
                                widthPx = bmp.width, heightPx = bmp.height
                            )
                        )
                    }
                }
                done = true
            }
        }
    }
}

private suspend fun awaitMapSnapshot(map: GmsGoogleMap): Bitmap? =
    suspendCancellableCoroutine { cont ->
        map.snapshot { bmp -> if (cont.isActive) cont.resume(bmp) }
    }
