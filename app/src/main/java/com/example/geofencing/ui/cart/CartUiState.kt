package com.example.geofencing.ui.cart

import com.example.geofencing.ui.components.StatusKind
import com.example.geofencing.ui.map.CartMarker
import com.google.android.gms.maps.model.LatLng

data class ViolationDetail(
    val duration: String,
    val maxSpeed: String,
    val atTime: String,
    val atAddress: String
)

data class CartUiState(
    val sectorName: String,
    val cartName: String,
    val registeredId: String,
    val operationStatus: String,
    val geofenceStatus: StatusKind,
    val timestamp: String,
    val address: String,
    val violation: ViolationDetail? = null,
    val geofence: List<LatLng> = emptyList(),
    val carts: List<CartMarker> = emptyList()
)
