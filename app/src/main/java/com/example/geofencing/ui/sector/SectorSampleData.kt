package com.example.geofencing.ui.sector

import com.example.geofencing.ui.components.StatusKind

// 임시 샘플 데이터: 프리뷰 + ViewModel 연결 전 실기기 확인용. 실제 데이터 붙으면 삭제.
fun sampleSectorState() = SectorUiState(
    name = "Sector #1",
    address = "1776 Terminal Dr, Richland, WA 99354",
    wholeCarts = 47,
    violation = 1,
    disconnect = 1,
    violations = listOf(
        SectorViolationEntry("Cart #2", "8m 45s")
    ),
    disconnects = listOf(
        SectorDisconnectEntry("Cart #4", "1 Days ago")
    ),
    allCarts = listOf(
        CartStateEntry("Cart #1", "Driving", StatusKind.Compliance),
        CartStateEntry("Cart #2", "Driving", StatusKind.Violation),
        CartStateEntry("Cart #3", "Driving", StatusKind.Compliance),
        CartStateEntry("Cart #4", "", StatusKind.Disconnect),
        CartStateEntry("Cart #5", "Driving", StatusKind.Compliance),
        CartStateEntry("Cart #6", "Driving", StatusKind.Compliance),
        CartStateEntry("Cart #7", "Driving", StatusKind.Compliance),
        CartStateEntry("Cart #8", "Driving", StatusKind.Compliance)
    ),
    currentPage = 1,
    totalPages = 5
)
