package com.example.geofencing.data.model

import com.google.android.gms.maps.model.LatLng

// 지도 핀에 표시할 정보의 공통 인터페이스. 지금은 Sector만 구현하지만,
// 추후 Cart 등 다른 종류의 핀이 추가돼도 MapPinMarker 렌더링 코드는 그대로 재사용된다.
interface MapMarkerInfo {
    val id: String
    val position: LatLng
    val title: String
    val subtitle: String
    val isCritical: Boolean
}
