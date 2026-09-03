package com.example.geofencing.ui.cart

import com.example.geofencing.ui.dashboard.SampleDashboardSectors
import com.example.geofencing.ui.dashboard.sampleDashboardSectorByName

// 이름으로 조회하는 편의 함수(프리뷰/폴백용). 실 흐름은 CartViewModel이 Repository로 처리.
fun cartStateFor(sectorName: String, cartName: String): CartUiState =
    cartUiStateFrom(sampleDashboardSectorByName(sectorName) ?: SampleDashboardSectors.first(), cartName)
        ?: error("preview sample: cart '$cartName' not found in '$sectorName'")

// 프리뷰용 3케이스(Sector #1: Cart #1=violation, #2=disconnect, #3=compliance).
private const val SampleSectorName = "Sector #1"
fun sampleCartComplianceState() = cartStateFor(SampleSectorName, "Cart #3")
fun sampleCartViolationState() = cartStateFor(SampleSectorName, "Cart #1")
fun sampleCartDisconnectState() = cartStateFor(SampleSectorName, "Cart #2")
