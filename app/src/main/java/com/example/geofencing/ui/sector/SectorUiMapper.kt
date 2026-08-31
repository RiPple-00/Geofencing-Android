package com.example.geofencing.ui.sector

import com.example.geofencing.ui.components.StatusKind
import com.example.geofencing.ui.dashboard.DashboardSector
import com.example.geofencing.ui.map.CartMarker

fun sectorUiStateFrom(sector: DashboardSector): SectorUiState {
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
        totalPages = 1,
        geofence = sector.geofence,
        carts = sector.carts.map { CartMarker(it.id, it.position, it.status) },
        violationPoints = sector.violationPoints
    )
}
