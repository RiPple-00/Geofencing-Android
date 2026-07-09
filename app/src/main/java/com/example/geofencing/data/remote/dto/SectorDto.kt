package com.example.geofencing.data.remote.dto

import kotlinx.serialization.Serializable

// GET /sites/{siteId}/sectors
@Serializable
data class SectorListResponseDto(
    val sectors: List<SectorListItemDto>
)

@Serializable
data class SectorListItemDto(
    val id: Int,
    val name: String
)

// GET /sites/{siteId}/sectors/{sectorId}
@Serializable
data class SectorDetailDto(
    val id: Int,
    val name: String,
    val address: String,
    val cartSummary: CartSummaryDto
)

// GET /sites/{siteId}/sectors/search
@Serializable
data class SectorSearchResponseDto(
    val results: List<SectorSearchResultDto>
)

@Serializable
data class SectorSearchResultDto(
    val id: Int,
    val name: String
)
