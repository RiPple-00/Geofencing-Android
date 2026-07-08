package com.example.geofencing.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.geofencing.data.model.MapMarkerInfo
import com.example.geofencing.ui.theme.Body10
import com.example.geofencing.ui.theme.DarkBorderStrong
import com.example.geofencing.ui.theme.DarkCriticalPrimary
import com.example.geofencing.ui.theme.DarkFillPrimary
import com.example.geofencing.ui.theme.DarkTextPrimary
import com.example.geofencing.ui.theme.DarkTextSecondary
import com.example.geofencing.ui.theme.Label10
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberUpdatedMarkerState

// Sector든 Cart든 MapMarkerInfo만 구현하면 그대로 렌더링되는 공용 핀.
// 카드 + 하단 Map pin 아이콘을 하나로 묶어서 anchor(0.5, 1)를 핀 끝 중심에 맞춘다.
// 카드 border 기본값은 text/primary, 핀 border 기본값은 text/secondary(SVG 실측값) — 서로 다름에 유의.
// 둘 다 isCritical일 때만 critical/primary로 분기. 상태 종류가 늘어나면 Boolean 대신 enum으로 교체.
private val CardCornerRadius = 5.90097.dp

@Composable
fun MapPinMarker(marker: MapMarkerInfo) {
    val cardBorderColor = if (marker.isCritical) DarkCriticalPrimary else DarkTextPrimary
    val pinBorderColor = if (marker.isCritical) DarkCriticalPrimary else DarkTextSecondary

    MarkerComposable(
        keys = arrayOf<Any>(marker.id, marker.isCritical, marker.title, marker.subtitle),
        state = rememberUpdatedMarkerState(position = marker.position),
        anchor = Offset(0.5f, 1f)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Column(
                modifier = Modifier
                    .shadow(
                        elevation = 23.603885650634766.dp,
                        shape = RoundedCornerShape(CardCornerRadius),
                        ambientColor = Color(0x4D000000),
                        spotColor = Color(0x4D000000)
                    )
                    .border(
                        width = 0.73762.dp,
                        color = cardBorderColor,
                        shape = RoundedCornerShape(CardCornerRadius)
                    )
                    .background(
                        color = DarkFillPrimary.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(CardCornerRadius)
                    )
                    .padding(horizontal = 14.75243.dp, vertical = 7.37621.dp),
                verticalArrangement = Arrangement.spacedBy(2.9504857.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = marker.title, style = Label10, color = DarkTextPrimary)
                Text(text = marker.subtitle, style = Body10, color = DarkBorderStrong)
            }
            Spacer(modifier = Modifier.height(4.dp))
            MapPinIcon(borderColor = pinBorderColor)
        }
    }
}
