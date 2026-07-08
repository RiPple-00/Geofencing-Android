package com.example.geofencing.ui.map.slidepanel.summary

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

// TODO: 마지막 갱신 시각은 domain/repository 확정 전까지 상수로 채움.
@Composable
fun RefreshStatusRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "2026.07.06 12:24",
            modifier = Modifier
                .width(103.dp)
                .height(14.dp),
            style = Label13,
            color = DarkTextSecondary
        )
        Spacer(modifier = Modifier.width(8.dp))
        RefreshButton(onClick = { /* TODO: 새로고침 동작 연결 */ })
    }
}

@Composable
private fun RefreshButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(32.dp)
            .border(width = 1.dp, color = DarkBorderDefault, shape = RoundedCornerShape(RoundedMd))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_refresh),
            contentDescription = "새로고침",
            modifier = Modifier.size(16.dp)
        )
    }
}
