package com.example.geofencing.ui.cart

import com.example.geofencing.ui.components.StatusKind
import com.example.geofencing.ui.dashboard.DashboardSector
import com.example.geofencing.ui.map.CartMarker

fun cartUiStateFrom(sector: DashboardSector, cartId: String): CartUiState {
    val cart = sector.carts.find { it.id == cartId } ?: sector.carts.first()
    return CartUiState(
        sectorName = sector.name,
        cartName = cart.id,
        registeredId = cart.registeredId,
        operationStatus = if (cart.status == StatusKind.Disconnect) "Disconnect" else cart.drivingState,
        geofenceStatus = cart.status,
        timestamp = cart.timestamp,
        address = sector.address,
        violation = cart.violationDuration?.let {
            ViolationDetail(
                duration = it,
                maxSpeed = cart.maxSpeed ?: "",
                atTime = cart.violationAtTime ?: "",
                atAddress = cart.violationAtAddress ?: ""
            )
        },
        geofence = sector.geofence,
        carts = sector.carts.map { CartMarker(it.id, it.position, it.status) }
    )
}
