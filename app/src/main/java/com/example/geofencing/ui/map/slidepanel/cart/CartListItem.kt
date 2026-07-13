package com.example.geofencing.ui.map.slidepanel.cart

// TODO: 개별 카트 목록 API(예: GET /sites/{siteId}/carts)가 확정되면 실제 응답 매핑으로 교체.
// 그 전까지는 CartList를 더미 데이터로 채워 UI/필터링만 먼저 구현한다.
data class CartListItem(
    val id: Int,
    val name: String,
    val violating: Boolean
)
