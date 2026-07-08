package com.example.geofencing.ui.map.slidepanel.summary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.geofencing.R
import com.example.geofencing.ui.map.slidepanel.StatusCardContainer
import com.example.geofencing.ui.theme.Body14
import com.example.geofencing.ui.theme.DarkFillPrimary
import com.example.geofencing.ui.theme.DarkTextPrimary
import com.example.geofencing.ui.theme.DarkTextSecondary
import com.example.geofencing.ui.theme.Label18

// 파이차트를 쓰는 Summary 탭 카드(Driving Status, GeoFencing Status 등)가 공유하는 양식.
// 왼쪽에 68x68 도넛 차트, 오른쪽에 세그먼트별 범례(체크 아이콘+라벨+값)가 세로로 쌓임.
data class PieChartSegment(
    val label: String,
    val value: Int,
    val color: Color
)

@Composable
fun PieChartStatusCard(
    title: String,
    segments: List<PieChartSegment>,
    modifier: Modifier = Modifier
) {
    StatusCardContainer(title = title, modifier = modifier) {
        // 31(왼쪽) + 68(차트) + 57(간격) + 140(범례) + 32(오른쪽) = 328dp로 카드 폭에
        // 딱 맞아떨어짐 - fillMaxWidth로 실제 카드 폭을 채워야 우측 32dp가 정확히 나온다.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
                .padding(start = 31.dp, end = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(57.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DonutChart(segments = segments)
            Column(verticalArrangement = Arrangement.spacedBy(34.dp)) {
                segments.forEach { segment ->
                    LegendRow(
                        iconColor = segment.color,
                        label = segment.label,
                        value = segment.value.toString()
                    )
                }
            }
        }
    }
}

// 링 두께(4dp)의 stroke를 세그먼트 값 비율만큼 나눠 그린 도넛 차트. 중앙의 10dp 원은
// 기능 없는 순수 장식 요소(실측값: elevation 6dp, 0x40000000 그림자, fillPrimary 배경).
@Composable
private fun DonutChart(segments: List<PieChartSegment>, modifier: Modifier = Modifier) {
    val total = segments.sumOf { it.value }

    Box(
        modifier = modifier.size(68.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(68.dp)) {
            val strokeWidthPx = 4.dp.toPx()
            val diameter = size.minDimension - strokeWidthPx
            val topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f)
            val arcSize = Size(diameter, diameter)
            val stroke = Stroke(width = strokeWidthPx)

            // drawArc의 양수 sweepAngle은 시계방향이라, 반시계방향으로 그리려면
            // 음수 sweepAngle을 쓰고 startAngle도 그만큼 반대로 누적해야 한다.
            var startAngle = -90f
            segments.forEach { segment ->
                val sweep = 360f * segment.value / total
                drawArc(
                    color = segment.color,
                    startAngle = startAngle,
                    sweepAngle = -sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke
                )
                startAngle -= sweep
            }
        }

        Box(
            modifier = Modifier
                .size(10.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = CircleShape,
                    spotColor = Color(0x40000000),
                    ambientColor = Color(0x40000000)
                )
                .background(color = DarkFillPrimary, shape = CircleShape)
        )
    }
}

@Composable
private fun LegendRow(
    iconColor: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.width(140.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_ok),
                contentDescription = null,
                colorFilter = ColorFilter.tint(iconColor),
                modifier = Modifier
                    .padding(1.dp)
                    .size(12.dp)
            )
            Text(
                text = label,
                style = Body14,
                color = DarkTextSecondary
            )
        }

        Text(
            text = value,
            style = Label18,
            color = DarkTextPrimary
        )
    }
}
