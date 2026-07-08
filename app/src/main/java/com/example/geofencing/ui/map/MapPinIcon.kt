package com.example.geofencing.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import com.example.geofencing.ui.theme.DarkFillPrimary

// 피그마 "Map pin" 실측 SVG(11x17 viewBox) 그대로: 원(circle) + 하단 stem(round cap 선).
// fill(#2D3137, DarkFillPrimary)은 원에만 적용되는 고정값. 원의 stroke와 stem 선 색은
// 둘 다 상태(critical 등)에 따라 같이 바뀐다.
//
// 주의: path/line 좌표는 SVG 원본 단위(raw unit) 그대로 쓰고, scale(density)로 한 번에
// 픽셀로 변환한다. 예전엔 line만 .dp.toPx()로 밀도를 적용해서 원과 선이 서로 다른
// 스케일로 그려져 어긋나 보였음 — 반드시 같은 좌표계(raw unit)에서 그린 뒤 한 번에 스케일할 것.
private const val CIRCLE_PATH_DATA =
    "M5.40886 0.695673C8.01206 0.695673 10.1229 2.80569 10.1231 5.40886" +
        "C10.1231 7.86029 8.25092 9.87553 5.859 10.1016L5.40886 10.1435L4.95974 10.1016" +
        "C2.56776 9.87559 0.695673 7.86034 0.695673 5.40886C0.695822 2.80579 2.80579 0.695822 5.40886 0.695673Z"

private const val PIN_STROKE_WIDTH = 1.32772f
private const val PIN_CENTER_X = 5.40886f
private const val STEM_START_Y = 10.15f
private const val STEM_END_Y = 16.3f

@Composable
fun MapPinIcon(
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    val circlePath = remember { PathParser().parsePathString(CIRCLE_PATH_DATA).toPath() }

    Canvas(modifier = modifier.size(width = 11.dp, height = 17.dp)) {
        scale(scale = density, pivot = Offset.Zero) {
            drawPath(path = circlePath, color = DarkFillPrimary)
            drawPath(
                path = circlePath,
                color = borderColor,
                style = Stroke(width = PIN_STROKE_WIDTH)
            )
            drawLine(
                color = borderColor,
                start = Offset(x = PIN_CENTER_X, y = STEM_START_Y),
                end = Offset(x = PIN_CENTER_X, y = STEM_END_Y),
                strokeWidth = PIN_STROKE_WIDTH,
                cap = StrokeCap.Round
            )
        }
    }
}
