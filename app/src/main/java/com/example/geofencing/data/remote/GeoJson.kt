package com.example.geofencing.data.remote

import kotlinx.serialization.Serializable

// 본 API는 GeoJSON(RFC 7946) 중 Point/Polygon 2종류만 사용한다.
// coordinates 순서는 [lng, lat] - 위경도 순서(lat, lng)와 반대이니 매핑 시 주의.

@Serializable
data class GeoJsonPoint(
    val type: String = "Point",
    val coordinates: List<Double>
) {
    val longitude: Double get() = coordinates[0]
    val latitude: Double get() = coordinates[1]
}

@Serializable
data class GeoJsonPolygon(
    val type: String = "Polygon",
    // coordinates[0] = 외곽 링. 링의 첫 점 = 마지막 점(닫힘)이며, 본 API는 구멍 없는
    // 외곽 링 1개만 사용한다.
    val coordinates: List<List<List<Double>>>
) {
    // 닫힘점(첫 점과 동일한 마지막 점)을 제거한 외곽 링. Google Maps SDK가 자동으로
    // 닫아주므로 렌더링 시에는 닫힘점이 없는 형태를 사용한다.
    val outerRing: List<List<Double>>
        get() = coordinates.firstOrNull()?.dropLast(1) ?: emptyList()
}
