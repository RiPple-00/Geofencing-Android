package com.example.geofencing.data.model

import com.google.android.gms.maps.model.LatLng

// 지도 핀(MapMarkerInfo)용 표현. position은 지오펜스 외곽 링의 중심 근사값,
// cartCount/violatingCount는 해당 섹터의 상세(SectorDetail) 조회 결과에서 채움
data class Sector(
    override val id: String,
    val name: String,
    val address: String,
    val cartCount: Int,
    val violatingCount: Int,
    override val position: LatLng,
    override val isCritical: Boolean = violatingCount > 0
) : MapMarkerInfo {
    override val title: String get() = name
    override val subtitle: String get() = "$cartCount Carts"
}
