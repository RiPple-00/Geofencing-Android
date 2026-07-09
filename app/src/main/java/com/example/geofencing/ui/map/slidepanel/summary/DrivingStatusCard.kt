package com.example.geofencing.ui.map.slidepanel.summary

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.geofencing.ui.theme.DarkBorderStrong
import com.example.geofencing.ui.theme.DarkBrandPrimary

// 수치는 GET /sites/{siteId}/summary의 cartSummary.drivingStatus에서 온다.
@Composable
fun DrivingStatusCard(modifier: Modifier = Modifier, driving: Int = 0, idle: Int = 0) {
    PieChartStatusCard(
        title = "Driving Status",
        segments = listOf(
            PieChartSegment(label = "Driving", value = driving, color = DarkBrandPrimary),
            PieChartSegment(label = "Idle", value = idle, color = DarkBorderStrong)
        ),
        modifier = modifier
    )
}
