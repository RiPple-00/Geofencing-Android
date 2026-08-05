package com.example.geofencing.ui.wholesector

// 임시 샘플 데이터: 프리뷰 + ViewModel 연결 전 실기기 확인용. 실제 데이터(API/ViewModel) 붙으면 삭제.
val SampleSectorNames = listOf("Sector #1", "Sector #2", "Sector #3")

fun sampleWholeSectorState() = WholeSectorUiState(
    totalCarts = 67,
    compliance = 62,
    violation = 3,
    disconnect = 2,
    violations = listOf(
        ViolationEntry("Sector #3", "Cart #5", "1h 19m 23s"),
        ViolationEntry("Sector #1", "Cart #2", "8m 45s"),
        ViolationEntry("Sector #3", "Cart #7", "2m 6s")
    ),
    disconnects = listOf(
        DisconnectEntry("Sector #1", "Cart #4", "1 Days ago"),
        DisconnectEntry("Sector #2", "Cart #6", "5 hours ago")
    ),
    sectors = listOf(
        SectorSummary("Sector #1", 47, 1, 0),
        SectorSummary("Sector #2", 8, 0, 0),
        SectorSummary("Sector #3", 12, 2, 0)
    )
)
