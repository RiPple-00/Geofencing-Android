package com.example.geofencing.ui.map

import androidx.compose.ui.geometry.Offset
import com.example.geofencing.ui.components.StatusKind
import com.google.android.gms.maps.model.LatLng

// 지도에 그릴 요소 = "무엇을 그릴지"(content). "어디를 볼지"(MapCamera)와 분리해, 고정/라이브
// 화면이 동일한 Canvas 렌더러를 공유하게 한다. 전부 LatLng 기반(렌더링 방식과 무관).

// 카트 마커. position은 실시간 갱신(가변).
data class CartMarker(
    val id: String,
    val position: LatLng,
    val kind: StatusKind
)

data class GeofenceMapContent(
    val geofence: List<LatLng>,
    val carts: List<CartMarker> = emptyList(),
    // 클릭으로 선택된 카트(compliance/disconnect를 강조할 때만 의미 있음).
    val selectedCartId: String? = null
)

// highlight 규칙: Violation은 항상, Compliance/Disconnect는 선택됐을 때만.
fun GeofenceMapContent.isHighlighted(cart: CartMarker): Boolean =
    cart.kind == StatusKind.Violation || cart.id == selectedCartId

// 카메라(어디를 볼지). content와 분리.
sealed interface MapCamera {
    // geofence 전체가 보이는 고정 프레임(썸네일 · Sector). zoomFactor로 맞춤 후 추가 확대(1.3=1.3배).
    data class FitGeofence(val zoomFactor: Float = 1f) : MapCamera
    // 특정 카트를 중앙에 두고 추적(Cart). zoom만 inline/fullscreen에서 다름.
    data class FollowCart(val cartId: String, val zoom: Float) : MapCamera
}

// LatLng → 화면 픽셀. 두 구현이 이 지점에서 갈린다:
// - 저장형: 스냅샷 시점 카메라로 고정(배경 = 캐시 Bitmap). 카메라 고정이라 계속 유효.
// - 라이브형: map.projection을 카메라 프레임마다 갱신(배경 = 라이브 MapView).
fun interface MapProjector {
    fun project(latLng: LatLng): Offset
}
