package com.example.geofencing.ui.cart

import com.example.geofencing.ui.components.StatusKind

// 임시 샘플: 지오펜스 상태별로 데이터/색이 달라지는 3케이스. 실제 데이터 붙으면 삭제.
private const val SAMPLE_ADDRESS = "1776 Terminal Dr, Richland, WA 99354"

// Compliance: 정상 라이브. 주소 brand색, 새로고침 카운트다운 O, 추가 데이터 없음.
fun sampleCartComplianceState() = CartUiState(
    sectorName = "Sector #1",
    cartName = "Cart #1",
    registeredId = "DeltaX-T01",
    operationStatus = "Driving",
    geofenceStatus = StatusKind.Compliance,
    timestamp = "2026.07.15 15:05:00",
    address = SAMPLE_ADDRESS
)

// Violation: 주소 critical색 + 위반 전용 상세(Duration/MaxSpeed/at Time·Address).
fun sampleCartViolationState() = CartUiState(
    sectorName = "Sector #1",
    cartName = "Cart #2",
    registeredId = "DeltaX-T01",
    operationStatus = "Driving",
    geofenceStatus = StatusKind.Violation,
    timestamp = "2026.07.15 15:05:00",
    address = SAMPLE_ADDRESS,
    violation = ViolationDetail(
        duration = "8m 45s",
        maxSpeed = "16 Km/h",
        atTime = "2026.07.20 15:02:30",
        atAddress = SAMPLE_ADDRESS
    )
)

// Disconnect: 라이브 데이터 없음 → 새로고침 카운트다운 없음, Operation도 Disconnect 배지, 주소 회색.
fun sampleCartDisconnectState() = CartUiState(
    sectorName = "Sector #1",
    cartName = "Cart #4",
    registeredId = "DeltaX-T01",
    operationStatus = "Disconnect",
    geofenceStatus = StatusKind.Disconnect,
    timestamp = "2026.07.15 15:05:00",
    address = SAMPLE_ADDRESS
)

// (sector, cart) → 카트 상세 상태. 우선 Sector #1의 Cart #1~4만 실제 데이터, 나머지는 Compliance 폴백.
// 실제 데이터(ViewModel/API) 붙으면 이 조회를 대체.
fun cartStateFor(sectorName: String, cartName: String): CartUiState {
    val base = if (sectorName == "Sector #1") {
        when (cartName) {
            "Cart #2" -> sampleCartViolationState()
            "Cart #4" -> sampleCartDisconnectState()
            else -> sampleCartComplianceState() // Cart #1, #3
        }
    } else {
        sampleCartComplianceState()
    }
    return base.copy(sectorName = sectorName, cartName = cartName)
}
