package com.example.geofencing.ui.dashboard

import com.example.geofencing.ui.components.StatusKind
import com.google.android.gms.maps.model.LatLng

data class DashboardCart(
    val id: String,
    val position: LatLng,
    val status: StatusKind,
    val drivingState: String,
    val registeredId: String,
    val timestamp: String,
    val violationDuration: String? = null,
    val maxSpeed: String? = null,
    val violationAtTime: String? = null,
    val violationAtAddress: String? = null
)

data class DashboardSector(
    val id: Int,
    val name: String,
    val address: String,
    val totalCarts: Int,
    val geofence: List<LatLng>,
    val carts: List<DashboardCart>,
    val violationPoints: List<LatLng>
)
