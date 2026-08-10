package com.example.geofencing.ui.wholesector

import com.example.geofencing.ui.components.StatusKind
import com.example.geofencing.ui.mock.MockSectors

// 단일 mock 소스(MockSectors)에서 파생. 실데이터 붙으면 삭제.
val SampleSectorNames = MockSectors.map { it.name }

fun sampleWholeSectorState(): WholeSectorUiState {
    val totalCarts = MockSectors.sumOf { it.totalCarts }
    val violation = MockSectors.sumOf { s -> s.carts.count { it.status == StatusKind.Violation } }
    val disconnect = MockSectors.sumOf { s -> s.carts.count { it.status == StatusKind.Disconnect } }
    val compliance = totalCarts - violation - disconnect

    // 전 섹터의 violation/disconnect 카트를 목록으로.
    val violations = MockSectors.flatMap { s ->
        s.carts.filter { it.status == StatusKind.Violation }
            .map { ViolationEntry(s.name, it.id, it.violationDuration ?: "") }
    }
    val disconnects = MockSectors.flatMap { s ->
        s.carts.filter { it.status == StatusKind.Disconnect }
            .map { DisconnectEntry(s.name, it.id, "1 Days ago") }
    }
    val sectors = MockSectors.map { s ->
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
        sectors = sectors
    )
}
