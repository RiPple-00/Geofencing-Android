package com.example.geofencing.data.remote.supabase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

// PostgREST(Supabase Data API) 응답 DTO — 테이블 행을 그대로 받는다.
// 좌표는 GeoJSON(jsonb): [lng, lat] 순서. Polygon은 링 배열(외곽 링 = coordinates[0]).
@Serializable
data class GeoPolygon(val type: String, val coordinates: List<List<List<Double>>>)

@Serializable
data class GeoPoint(val type: String, val coordinates: List<Double>)

@Serializable
data class SbSector(
    val id: Int,
    val name: String,
    val address: String,
    val geofence: GeoPolygon
)

@Serializable
data class SbCart(
    val id: Int,
    @SerialName("sector_id") val sectorId: Int,
    val name: String,
    @SerialName("geofence_status") val geofenceStatus: String,
    @SerialName("driving_status") val drivingStatus: String
)

@Serializable
data class SbEvent(
    val id: Int,
    @SerialName("cart_id") val cartId: Int,
    @SerialName("sector_id") val sectorId: Int,
    @SerialName("occurred_at") val occurredAt: String,
    val location: GeoPoint
)

// Supabase PostgREST 엔드포인트. 인증 헤더(apikey/Authorization)는 OkHttp 인터셉터가 붙인다.
interface SupabaseApi {
    @GET("rest/v1/sectors")
    suspend fun getSectors(
        @Query("select") select: String = "*",
        @Query("order") order: String = "id"
    ): List<SbSector>

    @GET("rest/v1/carts")
    suspend fun getCarts(
        @Query("select") select: String = "*",
        @Query("order") order: String = "id"
    ): List<SbCart>

    @GET("rest/v1/geofence_events")
    suspend fun getEvents(
        @Query("select") select: String = "*"
    ): List<SbEvent>
}
