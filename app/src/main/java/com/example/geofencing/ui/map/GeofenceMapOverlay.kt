package com.example.geofencing.ui.map

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.example.geofencing.ui.components.StatusKind
import com.example.geofencing.ui.theme.DarkBorderStrong
import com.example.geofencing.ui.theme.DarkBrandTertiary
import com.example.geofencing.ui.theme.DarkCriticalTertiary
import com.example.geofencing.ui.theme.DarkTextDisabled
import com.example.geofencing.ui.theme.DarkTextPrimary
import com.example.geofencing.ui.theme.GeofencingTheme
import com.example.geofencing.ui.theme.Label14
import com.example.geofencing.ui.theme.extendedColors
import com.google.android.gms.maps.model.LatLng
import kotlin.math.roundToInt

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
    boundaryColor: Color = MaterialTheme.extendedColors.textPrimary,
    boundaryWidth: Dp = 1.dp
) {
    val labelColor = MaterialTheme.extendedColors.textSecondary
    // 라벨은 마커 지름이 이 크기 이상일 때만(=60% 이상 확대) 표시 → 축소/배너에선 숨겨 혼잡 방지.
    val minLabelMarkerDiameterPx = with(LocalDensity.current) { CartLabelMinMarkerSize.toPx() }
    // clipToBounds: Canvas는 기본적으로 영역 밖으로도 그려서(경계선/글로우가 지도 밖으로 삐져나옴),
    // 타이트 줌(FollowCart)에서 geofence가 뷰를 넘칠 때 잘리도록 자기 영역으로 클립.
    Box(modifier = modifier.clipToBounds()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
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
            // 마커/glow 크기를 [배너 최소, 60% 최대]로 클램프 → 축소해도 안 작아지고, 확대해도 60% 사이즈가 최대.
            // 마커: 외곽 지름 = 반경×(2+테두리비율). glow: 지름 = 반경×2. glow 최대는 마커와 같은 줌에서 멈추게 비례(48m/8m).
            val minMarkerRadiusPx = (CartMarkerMinDiameter / (2f + MarkerBorderRatio)).toPx()
            val maxMarkerRadiusPx = (CartMarkerMaxDiameter / (2f + MarkerBorderRatio)).toPx()
            val minGlowRadiusPx = (CartGlowMinDiameter / 2f).toPx()
            val maxGlowRadiusPx = maxMarkerRadiusPx * (GlowRadiusMeters / MarkerRadiusMeters).toFloat()
            // 선택된 카트는 맨 나중에 그려 glow·마커가 다른 마커 위(최상위)에 오게 한다. 선택 안 된 카트는 disabled.
            val selectedId = content.selectedCartId
            content.carts.sortedBy { it.id == selectedId }.forEach { cart ->
                drawCart(
                    center = projector.project(cart.position),
                    markerRadius = projector.geoRadiusToPx(cart.position, MarkerRadiusMeters)
                        .coerceIn(minMarkerRadiusPx, maxMarkerRadiusPx),
                    glowRadius = projector.geoRadiusToPx(cart.position, GlowRadiusMeters)
                        .coerceIn(minGlowRadiusPx, maxGlowRadiusPx),
                    kind = cart.kind,
                    highlighted = content.isHighlighted(cart),
                    disabled = selectedId != null && cart.id != selectedId
                )
            }
        }

        // 4) 카트 이름 라벨: 마커가 충분히 커진 카트만, 마커 위 CartLabelGap(6dp)·가로 중앙. Label14/text-secondary.
        //    graphicsLayer로 텍스트 크기만큼 이동(가로 중앙 = -width/2, 하단이 마커 위 6dp = -height).
        content.carts.forEach { cart ->
            val markerRadiusPx = projector.geoRadiusToPx(cart.position, MarkerRadiusMeters)
            // 마커 바깥 지름(테두리 포함) = 반경×(2 + 테두리비율). 테두리는 중앙 정렬이라 반경 밖으로 절반 나감.
            // Figma의 마커 크기는 border 포함값이므로 이 바깥 지름으로 비교(60%부터 라벨).
            if (shouldShowCartLabel(
                    hasSelectedCart = content.selectedCartId != null,
                    markerOuterDiameterPx = markerRadiusPx * (2f + MarkerBorderRatio),
                    minLabelMarkerDiameterPx = minLabelMarkerDiameterPx
                )
            ) {
                val pos = projector.project(cart.position)
                Text(
                    text = cart.id,
                    style = Label14,
                    color = labelColor,
                    maxLines = 1,
                    modifier = Modifier
                        .offset {
                            // 라벨이 뜨는 시점엔 마커가 60% 캡 크기라, 그려지는(캡된) 반경 위에 정확히 올린다.
                            val cappedRadiusPx =
                                markerRadiusPx.coerceAtMost((CartMarkerMaxDiameter / (2f + MarkerBorderRatio)).toPx())
                            IntOffset(
                                pos.x.roundToInt(),
                                (pos.y - cappedRadiusPx - CartLabelGap.toPx()).roundToInt()
                            )
                        }
                        .graphicsLayer {
                            translationX = -size.width / 2f
                            translationY = -size.height
                        }
                )
            }
        }
    }
}

// --- 스타일 상수 ---
private const val GlowRadiusMeters = 48.0
private const val MarkerRadiusMeters = 8.0
// 마커 외곽 지름(테두리 포함) 하한/상한. 배너(축소)에선 하한, 60% 확대에선 상한으로 멈춘다.
// Figma: 배너 7.793px, 60% 30.2px.
private val CartMarkerMinDiameter = 7.793.dp
private val CartMarkerMaxDiameter = 30.2.dp
// 위반(붉은) 마커 뒤 glow 최소 지름. 배너에서의 하한. Figma: 31.533px.
private val CartGlowMinDiameter = 31.533.dp
// 카트 이름 라벨과 마커 사이 간격(디자인 확정값).
private val CartLabelGap = 6.dp
// 카트 이름 라벨을 표시하기 시작하는 마커 지름(화면 px 환산 dp).
// Figma: 줌 20%/40%/60% → 마커 30.2px, 라벨은 60%(=30.2px)부터 표시.
private val CartLabelMinMarkerSize = 30.2.dp
// 마커 테두리/반경 비율: 스펙 3종 공통(compliance 2.58/22.725, violation·disconnect 1.125/9.9 ≈ 0.1135).
private const val MarkerBorderRatio = 0.1135f

// Cart Page는 selectedCartId로 추적 대상을 지정한다. 같은 오버레이를 쓰더라도 이 화면에서는 카트 이름을 그리지 않는다.
internal fun shouldShowCartLabel(
    hasSelectedCart: Boolean,
    markerOuterDiameterPx: Float,
    minLabelMarkerDiameterPx: Float
): Boolean = !hasSelectedCart && markerOuterDiameterPx >= minLabelMarkerDiameterPx
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
// disabled(카트 페이지에서 선택 안 된 카트)면 테두리를 text/disabled로 흐리게(fill은 상태 색 유지).
private fun markerColors(kind: StatusKind, disabled: Boolean): MarkerColors {
    val base = when (kind) {
        StatusKind.Compliance -> MarkerColors(border = DarkTextPrimary, fill = DarkBrandTertiary)
        StatusKind.Violation -> MarkerColors(border = DarkTextPrimary, fill = DarkCriticalTertiary)
        StatusKind.Disconnect -> MarkerColors(border = DarkBorderStrong, fill = DarkTextDisabled)
    }
    return if (disabled) base.copy(border = DarkTextDisabled) else base
}

// 지도 위 카트 하나: highlighted면 하이라이트(glow)를 카트 아래에 먼저, 그 위에 마커(채움+테두리).
// glow·마커 모두 center(카트 위치) 기준. disabled면 테두리만 흐리게(선택 안 된 카트).
private fun DrawScope.drawCart(
    center: Offset,
    markerRadius: Float,
    glowRadius: Float,
    kind: StatusKind,
    highlighted: Boolean,
    disabled: Boolean
) {
    if (highlighted) drawGlow(center, glowRadius, glowColor(kind))
    val mc = markerColors(kind, disabled)
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
    // 라벨은 마커 지름(px) 기준이라, 마커가 임계값(30.2dp)을 넘도록 좁은 영역(~144m)을 캔버스에 매핑한다.
    // projector를 실제 캔버스 px(=320dp×density)에 맞춰 density와 무관하게 라벨이 보이게 한다.
    val minLat = 37.5683; val maxLat = 37.5696 // ~144m → 마커 지름 > 30.2px(라벨 표시)
    val minLng = 126.9789; val maxLng = 126.9811
    val density = LocalDensity.current.density
    val w = 320f * density; val h = 320f * density
    val projector = MapProjector { ll ->
        val fx = ((ll.longitude - minLng) / (maxLng - minLng)).toFloat()
        val fy = ((maxLat - ll.latitude) / (maxLat - minLat)).toFloat()
        Offset(fx * w, fy * h)
    }
    val content = GeofenceMapContent(
        geofence = listOf(
            LatLng(37.5696, 126.9793), LatLng(37.5695, 126.9807),
            LatLng(37.5686, 126.9809), LatLng(37.5683, 126.9797), LatLng(37.5688, 126.9791)
        ),
        carts = listOf(
            CartMarker("Cart #1", LatLng(37.5692, 126.9800), StatusKind.Compliance), // 미선택 → 점만
            CartMarker("Cart #2", LatLng(37.5689, 126.9804), StatusKind.Violation),   // 항상 glow
            CartMarker("Cart #3", LatLng(37.5687, 126.9796), StatusKind.Disconnect)   // 선택 → glow + 흰 링
        ),
        selectedCartId = "Cart #3"
    )
    GeofencingTheme {
        GeofenceMapOverlay(content = content, projector = projector, modifier = Modifier.size(320.dp))
    }
}
