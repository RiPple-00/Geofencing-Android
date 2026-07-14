package com.example.geofencing.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// GET /sites/{siteId}/carts
@Serializable
data class CartListResponseDto(
    val carts: List<CartListItemDto>,
    val pagination: PaginationDto
)

@Serializable
data class CartListItemDto(
    val id: Int,
    val name: String,
    val geofenceStatus: CartGeofenceStatusDto
)

@Serializable
enum class CartGeofenceStatusDto {
    @SerialName("violating") VIOLATING,
    @SerialName("compliant") COMPLIANT
}

@Serializable
data class PaginationDto(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int
)
