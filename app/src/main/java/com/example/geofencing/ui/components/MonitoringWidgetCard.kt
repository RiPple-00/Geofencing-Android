package com.example.geofencing.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.geofencing.ui.theme.CardContentPaddingEnd
import com.example.geofencing.ui.theme.CardContentPaddingStart
import com.example.geofencing.ui.theme.CardContentPaddingTop
import com.example.geofencing.ui.theme.CardShape
import com.example.geofencing.ui.theme.DarkBorderFocus
import com.example.geofencing.ui.theme.DarkFillSecondary
import com.example.geofencing.ui.theme.DarkTextPrimary
import com.example.geofencing.ui.theme.GeofencingTheme
import com.example.geofencing.ui.theme.Header20

@Composable
fun MonitoringWidgetCard(
    title: String,
    eventCode: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .width(328.dp)
            .height(200.dp)
            .border(width = 1.dp, color = DarkBorderFocus, shape = CardShape)
            .background(color = DarkFillSecondary, shape = CardShape)
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$title ($eventCode)",
            style = Header20,
            color = DarkTextPrimary,
            modifier = Modifier.padding(
                start = CardContentPaddingStart,
                top = CardContentPaddingTop,
                end = CardContentPaddingEnd
            )
        )
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun MonitoringWidgetCardPreview() {
    GeofencingTheme {
        MonitoringWidgetCard(title = "Sensor A", eventCode = "EVT-001") {
            Text(text = "본문 내용", color = DarkTextPrimary)
        }
    }
}
