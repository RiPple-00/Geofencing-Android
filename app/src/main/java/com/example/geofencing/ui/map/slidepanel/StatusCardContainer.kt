package com.example.geofencing.ui.map.slidepanel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.geofencing.ui.theme.CardShape
import com.example.geofencing.ui.theme.DarkBorderFocus
import com.example.geofencing.ui.theme.DarkFillSecondary
import com.example.geofencing.ui.theme.DarkTextSecondary
import com.example.geofencing.ui.theme.Pretendard

// Figma에 이름 없이 리터럴로 온 스타일(fontSize 16/lineHeight 19.2/weight 500)
// - 기존 Label16(16sp/16sp)과 lineHeight 비율이 달라 별도 토큰으로 둠.
private val StatusCardTitleStyle = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 19.2.sp,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both
    )
)

// Summary/Sector/Cart 탭 카드가 공유하는 배경+헤더. width는 100% + 좌우 16dp 여백.
// height는 컨텐츠에 맞춰 늘어나며, 필요하면 modifier로 호출부에서 고정할 수 있음
// (예: EventCodeSection의 200dp).
@Composable
fun StatusCardContainer(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(width = 1.dp, color = DarkBorderFocus, shape = CardShape)
            .background(color = DarkFillSecondary, shape = CardShape)
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 헤더 텍스트는 한 줄로만 쓰여서 width는 중요하지 않음 - 컨테이너 폭에 맞춰 배치.
        Text(
            text = title,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(start = 12.dp, top = 12.5.dp, end = 8.dp, bottom = 12.5.dp),
            style = StatusCardTitleStyle,
            color = DarkTextSecondary
        )
        content()
    }
}
