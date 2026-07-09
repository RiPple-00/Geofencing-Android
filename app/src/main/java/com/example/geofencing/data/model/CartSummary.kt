package com.example.geofencing.data.model

// 카트 현황 집계. driving/idle은 사이트 전체 summary에만 있고, 섹터 상세에는 없어 기본값 0으로 둔다.
data class CartSummary(
    val total: Int,
    val violating: Int,
    val compliant: Int,
    val driving: Int = 0,
    val idle: Int = 0
)
