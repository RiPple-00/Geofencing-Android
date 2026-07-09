package com.example.geofencing.data.remote.dto

import com.example.geofencing.data.remote.GeoJsonPolygon
import kotlinx.serialization.Serializable

// GET /sites/{siteId}/summary

@Serializable
data class SiteSummaryDto(
    val site: SiteDto,
    val sectorCount: Int,
    val sectors: List<SectorSummaryItemDto>,
    val cartSummary: CartSummaryDto
)

@Serializable
data class SiteDto(
    val id: Int,
    val name: String
)

@Serializable
data class SectorSummaryItemDto(
    val id: Int,
    val name: String,
    val address: String,
    val geofence: GeoJsonPolygon
)

@Serializable
data class CartSummaryDto(
    val total: Int,
    val geofenceStatus: GeofenceStatusDto,
    // 섹터 상세(/sectors/{sectorId}) 응답에는 drivingStatus가 없어서 nullable로 공유한다.
    val drivingStatus: DrivingStatusDto? = null
)

@Serializable
data class GeofenceStatusDto(
    val violating: Int,
    val compliant: Int
)

@Serializable
data class DrivingStatusDto(
    val driving: Int,
    val idle: Int
)
