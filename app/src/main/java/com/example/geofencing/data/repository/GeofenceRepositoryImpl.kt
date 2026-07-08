package com.example.geofencing.data.repository

import com.example.geofencing.data.model.Geofence
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceRepositoryImpl @Inject constructor() : GeofenceRepository {

    // TODO: 실제 데이터 소스(Room, DataStore, 서버 API 등)로 교체
    private val geofences = listOf(
        Geofence(id = "1", name = "회사", latitude = 37.5665, longitude = 126.9780, radiusMeters = 150f),
        Geofence(id = "2", name = "집", latitude = 37.5013, longitude = 127.0396, radiusMeters = 100f)
    )

    override fun getGeofences(): List<Geofence> = geofences

    override fun getGeofenceById(id: String): Geofence? = geofences.find { it.id == id }
}
