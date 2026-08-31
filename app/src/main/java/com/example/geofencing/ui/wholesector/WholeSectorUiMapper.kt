package com.example.geofencing.ui.wholesector

import com.example.geofencing.ui.components.StatusKind
import com.example.geofencing.ui.dashboard.DashboardSector

fun wholeSectorUiStateFrom(sectors: List<DashboardSector>): WholeSectorUiState {
    val totalCarts = sectors.sumOf { it.totalCarts }
    val violation = sectors.sumOf { sector ->
        sector.carts.count { it.status == StatusKind.Violation }
    }
    val disconnect = sectors.sumOf { sector ->
        sector.carts.count { it.status == StatusKind.Disconnect }
    }
    val compliance = totalCarts - violation - disconnect

    val violations = sectors.flatMap { sector ->
        sector.carts.filter { it.status == StatusKind.Violation }
            .map { ViolationEntry(sector.name, it.id, it.violationDuration ?: "") }
    }
    val disconnects = sectors.flatMap { sector ->
        sector.carts.filter { it.status == StatusKind.Disconnect }
            .map { DisconnectEntry(sector.name, it.id, "1 Days ago") }
    }
    val sectorSummaries = sectors.map { sector ->
        SectorSummary(
            id = sector.id,
            name = sector.name,
            wholeCart = sector.totalCarts,
            violation = sector.carts.count { it.status == StatusKind.Violation },
            disconnect = sector.carts.count { it.status == StatusKind.Disconnect },
            geofence = sector.geofence
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
