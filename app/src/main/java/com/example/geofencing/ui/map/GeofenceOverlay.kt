package com.example.geofencing.ui.map

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.example.geofencing.ui.theme.extendedColors
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.Polygon

// 지도 전체를 덮는 외곽 링. Web Mercator 한계라 위도 ±85로 제한.
// (혹시 fill이 반대로(내부가 어둡게) 나오면 이 좌표 순서를 뒤집으면 된다.)
private val WorldBounds = listOf(
    LatLng(-85.0, -180.0),
    LatLng(-85.0, 180.0),
    LatLng(85.0, 180.0),
    LatLng(85.0, -180.0)
)

// 지오펜스 외부를 어둡게 덮는 스크림 색. 검정 반투명(지도 자체 색은 API 스타일에서 조절).
// TODO(튜닝): alpha는 실기기에서 맞춰야 함(0.4~0.6 근처). 네이비를 원하면 색만 교체.
val GeofenceOutsideScrimColor = Color(0xFF000000).copy(alpha = 0.5f)

// 지오펜스 경계 기준 "외부를 어둡게". 세계 전체 폴리곤에 각 지오펜스를 구멍(hole)으로 뚫어 안쪽만 비운다.
// GoogleMap content 안에서 호출하되, 지오펜스 fill/마커보다 먼저(아래에) 두어야 경계선·마커가 위로 보인다.
// geofences: 섹터별 지오펜스 좌표(여러 개면 각각 구멍). 모든 지도 화면에서 재사용.
@Composable
fun GeofenceOutsideScrim(
    geofences: List<List<LatLng>>,
    scrimColor: Color = GeofenceOutsideScrimColor
) {
    val holes = geofences.filter { it.size >= 3 }
    if (holes.isEmpty()) return
    Polygon(
        points = WorldBounds,
        holes = holes,
        fillColor = scrimColor,
        strokeWidth = 0f,
        clickable = false
    )
}

// 폴리곤을 그릴 때 PolyUtil.simplify로 다듬는 허용 오차(미터).
private const val GeofenceSimplifyToleranceMeters = 5.0
// 경계선 두께(px) / 채움 알파. TODO(측정): 새 디자인 값으로 교체될 수 있음.
private const val GeofenceStrokeWidth = 4f
private const val GeofenceFillAlpha = 0.12f

// 한 섹터의 지오펜스 영역. hasViolation이면 경계/채움을 위반 색으로.
data class GeofenceArea(
    val points: List<LatLng>,
    val hasViolation: Boolean = false
)

// 모든 지도 화면의 유일한 공통 요소: 지오펜스 "경계선"(+옅은 채움). 나머지(스크림/마커/카메라/
// 정적·라이브 등)는 페이지마다 다르게 가공하므로, 각 페이지가 자기 GoogleMap content 안에 이걸 떨궈 쓴다.
@Composable
fun GeofenceBoundary(areas: List<GeofenceArea>) {
    val simplified = remember(areas) {
        areas.map { it.copy(points = PolyUtil.simplify(it.points, GeofenceSimplifyToleranceMeters)) }
    }
    val colors = MaterialTheme.extendedColors
    simplified.forEach { area ->
        val color = if (area.hasViolation) colors.criticalPrimary else colors.brandPrimary
        Polygon(
            points = area.points,
            strokeColor = color,
            strokeWidth = GeofenceStrokeWidth,
            fillColor = color.copy(alpha = GeofenceFillAlpha)
        )
    }
}

// 편의 조합: 외부 어둡게(스크림) + 경계선. "경계 + 외부 어둡게"를 함께 쓰는 페이지용.
// 스크림 구멍과 경계가 같은 tolerance로 심플리파이돼 가장자리가 어긋나지 않는다.
// 스크림 없이 경계선만 필요한 페이지는 GeofenceBoundary만 호출.
@Composable
fun GeofenceLayer(areas: List<GeofenceArea>) {
    val holes = remember(areas) {
        areas.map { PolyUtil.simplify(it.points, GeofenceSimplifyToleranceMeters) }
    }
    GeofenceOutsideScrim(geofences = holes)  // 경계/마커보다 먼저(아래).
    GeofenceBoundary(areas = areas)
}
