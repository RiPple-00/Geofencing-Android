package com.example.geofencing.ui.map.slidepanel.summary

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.geofencing.ui.theme.DarkBrandPrimary
import com.example.geofencing.ui.theme.DarkWarningPrimary

// 수치는 GET /sites/{siteId}/summary의 cartSummary.geofenceStatus에서 온다.
@Composable
fun GeoFencingStatusCard(modifier: Modifier = Modifier, compliant: Int = 0, violating: Int = 0) {
    PieChartStatusCard(
        title = "GeoFencing Status",
        segments = listOf(
            PieChartSegment(label = "Compliance", value = compliant, color = DarkBrandPrimary),
            PieChartSegment(label = "Violation", value = violating, color = DarkWarningPrimary)
        ),
        modifier = modifier
    )
}
