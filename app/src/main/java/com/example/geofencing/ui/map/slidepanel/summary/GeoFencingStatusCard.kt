package com.example.geofencing.ui.map.slidepanel.summary

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.geofencing.ui.theme.DarkBrandPrimary
import com.example.geofencing.ui.theme.DarkWarningPrimary

// TODO: Compliance/Violation 수치는 domain/repository 확정 전까지 상수로 채움.
@Composable
fun GeoFencingStatusCard(modifier: Modifier = Modifier) {
    PieChartStatusCard(
        title = "GeoFencing Status",
        segments = listOf(
            PieChartSegment(label = "Compliance", value = 9, color = DarkBrandPrimary),
            PieChartSegment(label = "Violation", value = 3, color = DarkWarningPrimary)
        ),
        modifier = modifier
    )
}
