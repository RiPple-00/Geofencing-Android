package com.example.geofencing.ui.map.slidepanel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.geofencing.ui.theme.Body14
import com.example.geofencing.ui.theme.DarkTextPrimary
import com.example.geofencing.ui.theme.DarkTextSecondary
import com.example.geofencing.ui.theme.Header20

// 피그마 실측값 그대로: title=header/20, subtitle=body/14. width는 컨테이너 폭에 맞춰 유동적으로.
@Composable
fun SlidePanelHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            // 상단 50dp(패널 최상단과의 간격), 하단 32dp(다음 StateTabRow와의 간격) 실측값.
            .padding(start = 16.dp, top = 50.dp, end = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = title, style = Header20, color = DarkTextPrimary)
        Text(text = subtitle, style = Body14, color = DarkTextSecondary)
    }
}
