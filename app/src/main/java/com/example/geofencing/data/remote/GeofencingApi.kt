package com.example.geofencing.data.remote

import com.example.geofencing.data.remote.dto.CartListResponseDto
import com.example.geofencing.data.remote.dto.GeofenceEventListResponseDto
import com.example.geofencing.data.remote.dto.SectorDetailDto
import com.example.geofencing.data.remote.dto.SectorListResponseDto
import com.example.geofencing.data.remote.dto.SectorSearchResponseDto
import com.example.geofencing.data.remote.dto.SiteSummaryDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GeofencingApi {

    @GET("sites/{siteId}/summary")
    suspend fun getSiteSummary(
        @Path("siteId") siteId: Int
    ): ApiResponse<SiteSummaryDto>

    @GET("sites/{siteId}/sectors")
    suspend fun getSectors(
        @Path("siteId") siteId: Int
    ): ApiResponse<SectorListResponseDto>

    @GET("sites/{siteId}/sectors/{sectorId}")
    suspend fun getSectorDetail(
        @Path("siteId") siteId: Int,
        @Path("sectorId") sectorId: Int
    ): ApiResponse<SectorDetailDto>

    @GET("sites/{siteId}/sectors/search")
    suspend fun searchSectors(
        @Path("siteId") siteId: Int,
        @Query("q") query: String,
        @Query("limit") limit: Int? = null
    ): ApiResponse<SectorSearchResponseDto>

    @GET("sites/{siteId}/carts")
    suspend fun getCarts(
        @Path("siteId") siteId: Int,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): ApiResponse<CartListResponseDto>

    @GET("sites/{siteId}/geofence-events")
    suspend fun getGeofenceEvents(
        @Path("siteId") siteId: Int
    ): ApiResponse<GeofenceEventListResponseDto>
}
