package com.example.geofencing.data.repository

import com.example.geofencing.data.model.Geofence

interface GeofenceRepository {
    fun getGeofences(): List<Geofence>
    fun getGeofenceById(id: String): Geofence?
}
