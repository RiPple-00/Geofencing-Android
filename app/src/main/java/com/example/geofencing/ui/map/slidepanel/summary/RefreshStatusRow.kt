package com.example.geofencing.ui.map.slidepanel.summary

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.geofencing.R
import com.example.geofencing.ui.theme.DarkBorderDefault
import com.example.geofencing.ui.theme.DarkTextSecondary
import com.example.geofencing.ui.theme.Label13
import com.example.geofencing.ui.theme.RoundedMd

// lastRefreshedAt은 MapScreen에서 KST 등 로컬 타임존으로 이미 포맷된 문자열을 전달받는다
// (BE는 UTC ISO 8601만 주고, 로컬 타임존 변환/표시는 App 담당이라는 API 명세를 따름).
@Composable
fun RefreshStatusRow(
    modifier: Modifier = Modifier,
    lastRefreshedAt: String = "-",
    isRefreshing: Boolean = false,
    onRefreshClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = lastRefreshedAt,
            modifier = Modifier.width(103.dp),
            style = Label13,
            color = DarkTextSecondary
        )
        Spacer(modifier = Modifier.width(8.dp))
        // 진행 중에는 비활성화해서 중복 새로고침 요청을 막는다.
        RefreshButton(enabled = !isRefreshing, onClick = onRefreshClick)
    }
}

@Composable
private fun RefreshButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Box(
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .border(width = 1.dp, color = DarkBorderDefault, shape = RoundedCornerShape(RoundedMd)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_refresh),
                contentDescription = "새로고침",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
