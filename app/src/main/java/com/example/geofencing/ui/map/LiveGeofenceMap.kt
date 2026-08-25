package com.example.geofencing.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.geofencing.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.rememberCameraPositionState
import kotlin.math.abs
import kotlin.math.log2
import com.google.android.gms.maps.GoogleMap as GmsGoogleMap

// 라이브 지도(Cart). 배경은 실제 MapView(카메라가 카트를 추적/줌 변경), 그 위에 GeofenceMapOverlay를
// 라이브 projection으로 겹친다. 카메라가 움직이거나 카트 위치가 갱신되면 오버레이가 다시 그려진다.
private const val FollowAnimMillis = 700
private const val FitPaddingPx = 48
private const val DefaultFitZoom = 15f
// 카트 재중심 판단 오차(위경도 합). 이보다 벗어나면 카트로 다시 중심 이동.
private const val CenterEpsilon = 1e-6

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun LiveGeofenceMap(
    content: GeofenceMapContent,
    camera: MapCamera,
    modifier: Modifier = Modifier,
    gesturesEnabled: Boolean = false,
    // 제스처 확대/축소 한계(줌 레벨, null=지도 기본). 100%~300% 같은 배율은 호출부에서 log2로 환산해 전달.
    minZoom: Float? = null,
    maxZoom: Float? = null,
    // 지도 위(타일/네이티브)에 얹을 추가 오버레이. 예: 히트맵 TileOverlay.
    mapContent: @Composable @GoogleMapComposable () -> Unit = {}
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialTarget(content, camera), initialZoom(camera))
    }
    var map by remember { mutableStateOf<GmsGoogleMap?>(null) }
    // 실제 지도 영역 크기(px). FitGeofence가 뷰의 내부 측정 크기(레이아웃 타이밍에 stale할 수 있음)에
    // 기대지 않고 이 값으로 명시 fit → 지도가 다른 크기 컨테이너로 이동(배너→히트맵)해도 정확히 맞춤.
    var mapSize by remember { mutableStateOf(IntSize.Zero) }

    val mapProperties = remember(minZoom, maxZoom) {
        val defaults = MapProperties()
        MapProperties(
            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark),
            minZoomPreference = minZoom ?: defaults.minZoomPreference,
            maxZoomPreference = maxZoom ?: defaults.maxZoomPreference
        )
    }
    val uiSettings = remember(gesturesEnabled) {
        MapUiSettings(
            compassEnabled = false,
            indoorLevelPickerEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
            rotationGesturesEnabled = false,
            tiltGesturesEnabled = false,
            zoomControlsEnabled = false,
            // 팬(스크롤)은 꺼서 중심이 카트에서 벗어나지 않게 하고, 줌만 허용.
            scrollGesturesEnabled = false,
            scrollGesturesEnabledDuringRotateOrZoom = false,
            zoomGesturesEnabled = gesturesEnabled
        )
    }

    // 카메라 제어: FollowCart는 카트 위치가 갱신될 때마다 재중심(실시간 추적), FitGeofence는 bounds에 맞춤.
    when (camera) {
        is MapCamera.FollowCart -> {
            val target = content.carts.find { it.id == camera.cartId }?.position
            // 첫 진입은 즉시(move) 해당 좌표로 — 애니메이션 없이 바로 로딩. 이후 위치 갱신만 부드럽게 추적.
            var firstFix by remember { mutableStateOf(true) }
            LaunchedEffect(target, camera.zoom) {
                if (target != null) {
                    val update = CameraUpdateFactory.newLatLngZoom(target, camera.zoom)
                    if (firstFix) {
                        cameraPositionState.move(update)
                        firstFix = false
                    } else {
                        cameraPositionState.animate(update, durationMs = FollowAnimMillis)
                    }
                }
            }
            // 확대/축소(제스처) 후 카메라가 멈추면(idle) 중심이 어긋났을 때 카트로 재중심(줌은 유지).
            LaunchedEffect(target) {
                if (target == null) return@LaunchedEffect
                snapshotFlow { cameraPositionState.isMoving }.collect { moving ->
                    if (!moving) {
                        val c = cameraPositionState.position.target
                        val off = abs(c.latitude - target.latitude) + abs(c.longitude - target.longitude)
                        if (off > CenterEpsilon) {
                            cameraPositionState.move(
                                CameraUpdateFactory.newLatLngZoom(target, cameraPositionState.position.zoom)
                            )
                        }
                    }
                }
            }
        }
        is MapCamera.FitGeofence -> {
            // 애니메이션 없이 즉시 geofence 전체에 맞춤(열 때 "화면 이동" 방지). map!=null이면 뷰 크기 확보됨.
            // zoomFactor가 1이 아니면 맞춤 후 배율만큼 추가 확대(줌 레벨은 로그 스케일이라 log2).
            val fitPaddingPx = camera.paddingDp?.let { with(density) { it.dp.roundToPx() } } ?: FitPaddingPx
            // mapSize를 키에 포함 → 컨테이너 크기가 바뀌면(배너→히트맵 오버레이) 새 크기로 다시 fit.
            LaunchedEffect(content.geofence, map, camera.zoomFactor, fitPaddingPx, mapSize) {
                if (map != null && content.geofence.size >= 3 && mapSize.width > 0 && mapSize.height > 0) {
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngBounds(
                            boundsOf(content.geofence), mapSize.width, mapSize.height, fitPaddingPx
                        )
                    )
                    if (camera.zoomFactor != 1f) {
                        cameraPositionState.move(CameraUpdateFactory.zoomBy(log2(camera.zoomFactor)))
                    }
                }
            }
        }
    }

    Box(modifier = modifier.onSizeChanged { mapSize = it }) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = uiSettings
        ) {
            MapEffect(Unit) { m -> map = m }
            mapContent()
        }

        // 오버레이: 카메라가 움직이면 position이 갱신 → projector를 새로 만들어(재투영) 다시 그린다.
        // 카트 위치 갱신은 content 변경으로 자연히 다시 그려진다.
        val liveMap = map
        val camPos = cameraPositionState.position
        if (liveMap != null) {
            val projector = remember(liveMap, camPos) {
                MapProjector { ll ->
                    val p = liveMap.projection.toScreenLocation(ll)
                    Offset(p.x.toFloat(), p.y.toFloat())
                }
            }
            GeofenceMapOverlay(content = content, projector = projector, modifier = Modifier.fillMaxSize())
        }
    }
}

private fun boundsOf(points: List<LatLng>): LatLngBounds =
    LatLngBounds.builder().apply { points.forEach(::include) }.build()

private fun centroid(points: List<LatLng>): LatLng =
    if (points.isEmpty()) LatLng(0.0, 0.0)
    else LatLng(points.map { it.latitude }.average(), points.map { it.longitude }.average())

private fun initialTarget(content: GeofenceMapContent, camera: MapCamera): LatLng = when (camera) {
    is MapCamera.FollowCart ->
        content.carts.find { it.id == camera.cartId }?.position ?: centroid(content.geofence)
    is MapCamera.FitGeofence -> centroid(content.geofence)
}

private fun initialZoom(camera: MapCamera): Float = when (camera) {
    is MapCamera.FollowCart -> camera.zoom
    is MapCamera.FitGeofence -> DefaultFitZoom // FitGeofence는 로드 후 bounds로 덮어씀
}
