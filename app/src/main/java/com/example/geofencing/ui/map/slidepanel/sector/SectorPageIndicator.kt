package com.example.geofencing.ui.map.slidepanel.sector

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// SVG 실측값: 원 지름 8dp(r=4), 중심 간격 20.25dp → 간격(gap) = 20.25 - 8 = 12.25dp.
// 카드 개수만큼 동적으로 그려야 해서 정적 XML이 아니라 Row+CircleShape로 직접 그린다.
private val DotColor = Color(0xFFD9D9D9)

@Composable
fun SectorPageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.25.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val color = if (index == currentPage) DotColor else DotColor.copy(alpha = 0.4f)
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color = color, shape = CircleShape)
            )
        }
    }
}
