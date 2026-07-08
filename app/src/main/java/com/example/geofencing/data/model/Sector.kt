package com.example.geofencing.data.model

import com.google.android.gms.maps.model.LatLng

data class Sector(
    override val id: String,
    val name: String,
    val cartCount: Int,
    override val position: LatLng,
    override val isCritical: Boolean = false
) : MapMarkerInfo {
    override val title: String get() = name
    override val subtitle: String get() = "$cartCount Carts"
}
