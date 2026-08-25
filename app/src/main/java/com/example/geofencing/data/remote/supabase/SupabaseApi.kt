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

// id/FK는 스키마가 bigint이므로 Long으로 받는다(Int면 큰 값 역직렬화 실패/오버플로).
@Serializable
data class SbSector(
    val id: Long,
    val name: String,
    val address: String,
    val geofence: GeoPolygon
)

@Serializable
data class SbCart(
    val id: Long,
    @SerialName("sector_id") val sectorId: Long,
    val name: String,
    @SerialName("geofence_status") val geofenceStatus: String,
    @SerialName("driving_status") val drivingStatus: String,
    val lat: Double? = null,
    val lng: Double? = null
)

@Serializable
data class SbEvent(
    val id: Long,
    @SerialName("cart_id") val cartId: Long,
    @SerialName("sector_id") val sectorId: Long,
    @SerialName("occurred_at") val occurredAt: String,
    val location: GeoPoint,
    @SerialName("max_speed") val maxSpeed: String? = null,
    val address: String? = null
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
