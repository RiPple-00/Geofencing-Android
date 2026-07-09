package com.example.geofencing.data.repository

import com.example.geofencing.data.model.GeofenceEventInfo
import com.example.geofencing.data.remote.GeofencingApi
import com.example.geofencing.data.remote.toDomain
import com.example.geofencing.data.remote.unwrap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceEventRepositoryImpl @Inject constructor(
    private val api: GeofencingApi
) : GeofenceEventRepository {

    override suspend fun getRecentEvents(siteId: Int): List<GeofenceEventInfo> =
        api.getGeofenceEvents(siteId).unwrap().events.map { it.toDomain() }
}
