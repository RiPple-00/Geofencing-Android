package com.example.geofencing.ui.map.slidepanel.summary

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.geofencing.ui.theme.DarkBorderStrong
import com.example.geofencing.ui.theme.DarkBrandPrimary

// TODO: Driving/Idle 수치는 domain/repository 확정 전까지 상수로 채움.
@Composable
fun DrivingStatusCard(modifier: Modifier = Modifier) {
    PieChartStatusCard(
        title = "Driving Status",
        segments = listOf(
            PieChartSegment(label = "Driving", value = 4, color = DarkBrandPrimary),
            PieChartSegment(label = "Idle", value = 1, color = DarkBorderStrong)
        ),
        modifier = modifier
    )
}
