package com.example.geofencing.data.model

import com.google.android.gms.maps.model.LatLng

// GET /sites/{siteId}/summary 응답의 도메인 표현.
data class SiteSummary(
    val siteId: Int,
    val siteName: String,
    val sectorCount: Int,
    val sectors: List<SectorOverview>,
    val cartSummary: CartSummary
)

// summary의 sectors[] 항목 - 카트 집계는 없고 지오펜스 경계만
data class SectorOverview(
    val id: Int,
    val name: String,
    val address: String,
    // 외곽 링, 닫힘점(첫 점=마지막 점) 제거된 상태
    val geofence: List<LatLng>
)
