package com.example.geofencing.ui.sector

import com.example.geofencing.ui.components.StatusKind
import com.example.geofencing.ui.map.CartMarker
import com.google.android.gms.maps.model.LatLng

data class SectorViolationEntry(val cart: String, val remaining: String)

data class SectorDisconnectEntry(val cart: String, val elapsed: String)

data class CartStateEntry(val cart: String, val drivingState: String, val kind: StatusKind)

data class SectorUiState(
    val id: Int,
    val name: String,
    val address: String,
    val wholeCarts: Int,
    val violation: Int,
    val disconnect: Int,
    val violations: List<SectorViolationEntry>,
    val disconnects: List<SectorDisconnectEntry>,
    val allCarts: List<CartStateEntry>,
    val currentPage: Int,
    val totalPages: Int,
    val geofence: List<LatLng> = emptyList(),
    val carts: List<CartMarker> = emptyList(),
    val violationPoints: List<LatLng> = emptyList()
)
