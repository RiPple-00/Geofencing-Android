package com.example.geofencing.data.remote.dto

import com.example.geofencing.data.remote.GeoJsonPoint
import kotlinx.serialization.Serializable

// GET /sites/{siteId}/geofence-events
@Serializable
data class GeofenceEventListResponseDto(
    val events: List<GeofenceEventDto>
)

@Serializable
data class GeofenceEventDto(
    val id: Int,
    val cartId: Int,
    val sectorId: Int,
    // ISO 8601, UTC (예: "2026-07-07T05:12:33Z"). KST 변환은 App(도메인 매핑)이 담당.
    val occurredAt: String,
    val location: GeoJsonPoint
)
