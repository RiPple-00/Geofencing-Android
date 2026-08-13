package com.example.geofencing.ui.sector

import com.example.geofencing.ui.components.StatusKind
import com.example.geofencing.ui.map.CartMarker
import com.example.geofencing.ui.mock.MockSector
import com.example.geofencing.ui.mock.MockSectors

// 단일 mock 소스(MockSector)에서 SectorPage 상태 파생. 지도 카트·목록·상세가 같은 carts를 봐 일치.
// ViewModel(SectorViewModel)이 Repository 데이터로 이 함수를 호출한다.
fun sectorStateFrom(sector: MockSector): SectorUiState {
    val violationCarts = sector.carts.filter { it.status == StatusKind.Violation }
    val disconnectCarts = sector.carts.filter { it.status == StatusKind.Disconnect }
    return SectorUiState(
        id = sector.id,
        name = sector.name,
        address = sector.address,
        wholeCarts = sector.totalCarts,
        violation = violationCarts.size,
        disconnect = disconnectCarts.size,
        violations = violationCarts.map { SectorViolationEntry(it.id, it.violationDuration ?: "") },
        disconnects = disconnectCarts.map { SectorDisconnectEntry(it.id, "1 Days ago") },
        allCarts = sector.carts.map { CartStateEntry(it.id, it.drivingState, it.status) },
        currentPage = 1,
        totalPages = 1, // 실제 페이지는 SectorPage가 wholeCarts로 계산
        geofence = sector.geofence,
        carts = sector.carts.map { CartMarker(it.id, it.position, it.status) },
        violationPoints = sector.violationPoints
    )
}

// 프리뷰용(첫 섹터).
fun sampleSectorState(): SectorUiState = sectorStateFrom(MockSectors.first())
