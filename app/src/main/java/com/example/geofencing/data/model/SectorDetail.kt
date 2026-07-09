package com.example.geofencing.data.model

// GET /sites/{siteId}/sectors/{sectorId} 응답의 도메인 표현.
// Sector 탭의 캐러셀 카드/리스트 카드가 필요로 하는 값(이름/주소/전체·이탈 카트 수)과 1:1대응
data class SectorDetail(
    val id: Int,
    val name: String,
    val address: String,
    val cartSummary: CartSummary
)

// GET /sites/{siteId}/sectors/search 응답의 도메인 표현 (검색창 자동완성용).
data class SectorSearchResult(
    val id: Int,
    val name: String
)
