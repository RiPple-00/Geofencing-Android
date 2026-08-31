package com.example.geofencing.ui.wholesector

import com.google.android.gms.maps.model.LatLng

data class ViolationEntry(val sector: String, val cart: String, val remaining: String)

data class DisconnectEntry(val sector: String, val cart: String, val elapsed: String)

data class SectorSummary(
    val id: Int,
    val name: String,
    val wholeCart: Int,
    val violation: Int,
    val disconnect: Int,
    val geofence: List<LatLng> = emptyList()
)

data class WholeSectorUiState(
    val totalCarts: Int,
    val compliance: Int,
    val violation: Int,
    val disconnect: Int,
    val violations: List<ViolationEntry>,
    val disconnects: List<DisconnectEntry>,
    val sectors: List<SectorSummary>
)
