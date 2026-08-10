package com.example.geofencing.ui.map

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.example.geofencing.ui.components.StatusKind
import com.example.geofencing.ui.theme.DarkBorderStrong
import com.example.geofencing.ui.theme.DarkBrandTertiary
import com.example.geofencing.ui.theme.DarkCriticalTertiary
import com.example.geofencing.ui.theme.DarkTextDisabled
import com.example.geofencing.ui.theme.DarkTextPrimary
import com.example.geofencing.ui.theme.GeofencingTheme
import com.example.geofencing.ui.theme.extendedColors
import com.google.android.gms.maps.model.LatLng

// content(그릴 요소) + projector(LatLng→픽셀)만 있으면 그리는 Canvas 오버레이.
// 배경(캐시 Bitmap / 라이브 MapView) 위에 겹쳐 놓는다. 고정·라이브 화면이 공유.
//
// 스케일 규칙(확정): 카트 점·glow는 실제 반경(m)을 현재 projection으로 px 환산 → 줌 따라 스케일.
// geofence 선은 좌표 고정(꼭짓점 투영)이지만 두께만 화면 px 고정.
@Composable
fun GeofenceMapOverlay(
    content: GeofenceMapContent,
    projector: MapProjector,
    modifier: Modifier = Modifier,
    scrimColor: Color = GeofenceOutsideScrimColor,
    // TODO(측정): 실제 디자인 값으로 확정.
    boundaryColor: Color = MaterialTheme.extendedColors.textPrimary,
    boundaryWidth: Dp = 1.dp
) {
    // clipToBounds: Canvas는 기본적으로 영역 밖으로도 그려서(경계선/글로우가 지도 밖으로 삐져나옴),
    // 타이트 줌(FollowCart)에서 geofence가 뷰를 넘칠 때 잘리도록 자기 영역으로 클립.
    Canvas(modifier = modifier.clipToBounds()) {
        val geofence = content.geofence
        if (geofence.size >= 3) {
            val poly = geofence.map { projector.project(it) }

            // 1) 외부 어둡게 + geofence 구멍: (전체 Rect + 폴리곤)을 EvenOdd로 채우면 안쪽만 뚫린다.
            val mask = Path().apply {
                addRect(Rect(Offset.Zero, size))
                polyPath(poly)
                fillType = PathFillType.EvenOdd
            }
            drawPath(mask, scrimColor)

            // 2) 경계선: 두께만 화면 px 고정.
            val outline = Path().apply { polyPath(poly) }
            drawPath(outline, boundaryColor, style = Stroke(width = boundaryWidth.toPx()))
        }

        // 3) 카트: 섹터에 속한 카트 수만큼(1:n). 각 카트 = 하이라이트(아래) + 마커(위), 둘 다 카트 위치 중심.
        content.carts.forEach { cart ->
            drawCart(
                center = projector.project(cart.position),
                markerRadius = projector.geoRadiusToPx(cart.position, MarkerRadiusMeters),
                glowRadius = projector.geoRadiusToPx(cart.position, GlowRadiusMeters),
                kind = cart.kind,
                highlighted = content.isHighlighted(cart)
            )
        }
    }
}

// --- 스타일 상수(TODO 측정: 실제 반경/두께는 디자인 확정 시 교체) ---
private const val GlowRadiusMeters = 48.0
private const val MarkerRadiusMeters = 8.0
// 마커 테두리/반경 비율: 스펙 3종 공통(compliance 2.58/22.725, violation·disconnect 1.125/9.9 ≈ 0.1135).
private const val MarkerBorderRatio = 0.1135f
// glow의 stroke/blur를 반경 비율로 두어 줌 스케일 시 함께 커지게 함(drawable 비율에서 유도).
private const val GlowFillAlpha = 0.30f
private const val GlowStrokeAlpha = 0.50f
private const val GlowStrokeRatio = 0.065f // cart_highlight_*.xml: 2.985 / 45.975(radius)
private const val GlowBlurRatio = 0.026f   // cart_highlight_*.xml: 1.19 / 45.975(radius)

// glow 전용 색(디자인 hex, 마커 색보다 밝음). fill .30 / stroke .50.
private fun glowColor(kind: StatusKind): Color = when (kind) {
    StatusKind.Compliance -> Color(0xFF93C5FD)
    StatusKind.Violation -> Color(0xFFEF4444)
    StatusKind.Disconnect -> Color(0xFF9DA4AF)
}

// 카트 마커 색(상태별 테두리 + 채움). 지도는 항상 다크라 Dark* 토큰 직접 사용.
private data class MarkerColors(val border: Color, val fill: Color)
private fun markerColors(kind: StatusKind): MarkerColors = when (kind) {
    StatusKind.Compliance -> MarkerColors(border = DarkTextPrimary, fill = DarkBrandTertiary)
    StatusKind.Violation -> MarkerColors(border = DarkTextPrimary, fill = DarkCriticalTertiary)
    StatusKind.Disconnect -> MarkerColors(border = DarkBorderStrong, fill = DarkTextDisabled)
}

// 지도 위 카트 하나: highlighted면 하이라이트(glow)를 카트 아래에 먼저, 그 위에 마커(채움+테두리).
// glow·마커 모두 center(카트 위치) 기준. highlighted 판정은 호출부(GeofenceMapContent.isHighlighted).
private fun DrawScope.drawCart(
    center: Offset,
    markerRadius: Float,
    glowRadius: Float,
    kind: StatusKind,
    highlighted: Boolean
) {
    if (highlighted) drawGlow(center, glowRadius, glowColor(kind))
    val mc = markerColors(kind)
    drawCircle(mc.fill, radius = markerRadius, center = center)
    drawCircle(
        mc.border,
        radius = markerRadius,
        center = center,
        style = Stroke(width = markerRadius * MarkerBorderRatio)
    )
}

// 실제 반경(m) → 현재 projection의 px. 중심에서 북쪽으로 meters만큼 떨어진 점을 투영해 거리 측정.
private fun MapProjector.geoRadiusToPx(at: LatLng, meters: Double): Float {
    val center = project(at)
    val north = project(LatLng(at.latitude + meters / 111_320.0, at.longitude))
    return (center - north).getDistance()
}

// blur가 들어간 glow(fill + stroke). 반경에 비례한 stroke/blur로 스케일 유지.
private fun DrawScope.drawGlow(center: Offset, radius: Float, color: Color) {
    if (radius <= 0f) return
    drawIntoCanvas { canvas ->
        val paint = Paint()
        val fw = paint.asFrameworkPaint()
        fw.isAntiAlias = true
        fw.maskFilter = BlurMaskFilter(radius * GlowBlurRatio, BlurMaskFilter.Blur.NORMAL)
        fw.style = android.graphics.Paint.Style.FILL
        fw.color = color.copy(alpha = GlowFillAlpha).toArgb()
        canvas.nativeCanvas.drawCircle(center.x, center.y, radius, fw)
        fw.style = android.graphics.Paint.Style.STROKE
        fw.strokeWidth = radius * GlowStrokeRatio
        fw.color = color.copy(alpha = GlowStrokeAlpha).toArgb()
        canvas.nativeCanvas.drawCircle(center.x, center.y, radius, fw)
    }
}

// 폴리곤 점들을 이어 닫는 서브패스.
private fun Path.polyPath(points: List<Offset>) {
    if (points.isEmpty()) return
    moveTo(points[0].x, points[0].y)
    for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
    close()
}

// 라이브 맵 없이 렌더러만 확인하는 프리뷰. projector는 작은 LatLng 영역을 캔버스에 선형 매핑(임시).
@Preview(showBackground = true, backgroundColor = 0xFF0F0F0F, widthDp = 320, heightDp = 320)
@Composable
private fun GeofenceMapOverlayPreview() {
    val minLat = 37.560; val maxLat = 37.575
    val minLng = 126.972; val maxLng = 126.990
    val w = 320f; val h = 320f
    val projector = MapProjector { ll ->
        val fx = ((ll.longitude - minLng) / (maxLng - minLng)).toFloat()
        val fy = ((maxLat - ll.latitude) / (maxLat - minLat)).toFloat()
        Offset(fx * w, fy * h)
    }
    val content = GeofenceMapContent(
        geofence = listOf(
            LatLng(37.5730, 126.9760), LatLng(37.5725, 126.9865),
            LatLng(37.5640, 126.9880), LatLng(37.5620, 126.9800), LatLng(37.5660, 126.9740)
        ),
        carts = listOf(
            CartMarker("c1", LatLng(37.5690, 126.9800), StatusKind.Compliance), // 미선택 → 점만
            CartMarker("c2", LatLng(37.5670, 126.9840), StatusKind.Violation),   // 항상 glow
            CartMarker("c3", LatLng(37.5650, 126.9790), StatusKind.Disconnect)   // 선택 → glow + 흰 링
        ),
        selectedCartId = "c3"
    )
    GeofencingTheme {
        GeofenceMapOverlay(content = content, projector = projector, modifier = Modifier.size(320.dp))
    }
}
