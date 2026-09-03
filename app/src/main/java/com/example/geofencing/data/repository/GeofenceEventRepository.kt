package com.example.geofencing.data.repository

import com.example.geofencing.data.model.GeofenceEventInfo

fun interface GeofenceEventRepository {
    // GET /sites/{siteId}/geofence-events - 최근 1시간 이내 이탈 이벤트.
    suspend fun getRecentEvents(siteId: Int): List<GeofenceEventInfo>
}
