package com.example.geofencing.ui.mock

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// geoJsonRingToLatLng: GeoJSON [lng,lat] → LatLng(lat,lng) 순서 뒤집기 + 닫힘점 제거.
class GeoJsonRingTest {

    // Polygon 좌표: 링 배열 → 점 배열 → [lng, lat].
    private fun polygon(vararg ring: Pair<Double, Double>): List<List<List<Double>>> =
        listOf(ring.map { (lng, lat) -> listOf(lng, lat) })

    @Test
    fun `lng-lat가 LatLng의 lat-lng로 뒤집힌다`() {
        val result = geoJsonRingToLatLng(polygon(126.97 to 37.56))
        assertEquals(LatLng(37.56, 126.97), result.single())
    }

    @Test
    fun `닫힘점(첫=마지막)은 제거된다`() {
        // 사각형 + 닫힘점 → 4개 꼭짓점만 남는다.
        val result = geoJsonRingToLatLng(
            polygon(
                0.0 to 0.0,
                1.0 to 0.0,
                1.0 to 1.0,
                0.0 to 1.0,
                0.0 to 0.0 // 닫힘점
            )
        )
        assertEquals(4, result.size)
        assertEquals(LatLng(0.0, 0.0), result.first())
        assertEquals(LatLng(1.0, 0.0), result.last())
    }

    @Test
    fun `닫히지 않은 링은 그대로 유지된다`() {
        val result = geoJsonRingToLatLng(polygon(0.0 to 0.0, 1.0 to 0.0, 1.0 to 1.0))
        assertEquals(3, result.size)
    }

    @Test
    fun `빈 좌표는 빈 리스트가 된다`() {
        assertTrue(geoJsonRingToLatLng(emptyList()).isEmpty())
    }
}
