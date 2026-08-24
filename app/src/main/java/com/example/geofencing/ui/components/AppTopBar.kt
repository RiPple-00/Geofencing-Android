package com.example.geofencing.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.geofencing.R
import com.example.geofencing.ui.theme.GeofencingTheme
import com.example.geofencing.ui.theme.Header20
import com.example.geofencing.ui.theme.extendedColors

// 앱 상단 헤더: 제목 + [검색][알림] 버튼. 배경/statusBarsPadding은 조립하는 컨테이너가 담당.
@Composable
fun AppTopBar(
    title: String,
    onSearchClick: () -> Unit,
    onBellClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = Header20,
            color = MaterialTheme.extendedColors.textPrimary
        )
        // 우측 액션: 검색 → 알림 순.
        Row(
            modifier = Modifier.padding(end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppIconButton(
                iconRes = R.drawable.ic_search,
                contentDescription = "검색",
                onClick = onSearchClick,
                size = 24.dp
            )
            AppIconButton(
                iconRes = R.drawable.ic_notification,
                contentDescription = "알림",
                onClick = onBellClick,
                size = 24.dp
            )
        }
    }
}

@Preview(widthDp = 360, showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
private fun AppTopBarPreview() {
    GeofencingTheme {
        AppTopBar(title = "Geofence", onSearchClick = {}, onBellClick = {})
    }
}
