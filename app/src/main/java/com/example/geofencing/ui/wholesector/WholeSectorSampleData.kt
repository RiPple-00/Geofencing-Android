package com.example.geofencing.ui.wholesector

import com.example.geofencing.ui.components.StatusKind
import com.example.geofencing.ui.mock.MockSector
import com.example.geofencing.ui.mock.MockSectors

// 단일 mock 소스(MockSectors)에서 파생. 실데이터 붙으면 삭제.
val SampleSectorNames = MockSectors.map { it.name }

// 프리뷰/기본용. ViewModel은 wholeSectorStateFrom(sectors)를 Repository 데이터로 호출.
fun sampleWholeSectorState(): WholeSectorUiState = wholeSectorStateFrom(MockSectors)

// 섹터 목록(도메인) → WholeSector UI state.
fun wholeSectorStateFrom(sectors: List<MockSector>): WholeSectorUiState {
    val totalCarts = sectors.sumOf { it.totalCarts }
    val violation = sectors.sumOf { s -> s.carts.count { it.status == StatusKind.Violation } }
    val disconnect = sectors.sumOf { s -> s.carts.count { it.status == StatusKind.Disconnect } }
    val compliance = totalCarts - violation - disconnect

    // 전 섹터의 violation/disconnect 카트를 목록으로.
    val violations = sectors.flatMap { s ->
        s.carts.filter { it.status == StatusKind.Violation }
            .map { ViolationEntry(s.name, it.id, it.violationDuration ?: "") }
    }
    val disconnects = sectors.flatMap { s ->
        s.carts.filter { it.status == StatusKind.Disconnect }
            .map { DisconnectEntry(s.name, it.id, "1 Days ago") }
    }
    val sectorSummaries = sectors.map { s ->
        SectorSummary(
            id = s.id,
            name = s.name,
            wholeCart = s.totalCarts,
            violation = s.carts.count { it.status == StatusKind.Violation },
            disconnect = s.carts.count { it.status == StatusKind.Disconnect },
            geofence = s.geofence
        )
    }
    return WholeSectorUiState(
        totalCarts = totalCarts,
        compliance = compliance,
        violation = violation,
        disconnect = disconnect,
        violations = violations,
        disconnects = disconnects,
        sectors = sectorSummaries
    )
}
