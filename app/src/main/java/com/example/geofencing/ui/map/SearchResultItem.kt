package com.example.geofencing.ui.map

// 검색 드롭다운에 섹터/카트 결과를 함께 보여주기 위한 표시 전용 타입 - 섹터는 API 검색
// (SectorRepository.searchSectors), 카트는 이미 로드된 cartItems를 이름으로 클라이언트에서
// 걸러서 만든다(카트 전용 검색 API가 없음).
sealed interface SearchResultItem {
    val name: String
    val hasViolation: Boolean

    data class Sector(val id: Int, override val name: String, override val hasViolation: Boolean) : SearchResultItem

    data class Cart(val id: Int, override val name: String, override val hasViolation: Boolean) : SearchResultItem
}
