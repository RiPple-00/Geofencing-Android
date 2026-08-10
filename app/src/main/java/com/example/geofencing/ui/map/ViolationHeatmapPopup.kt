package com.example.geofencing.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.geofencing.ui.components.CloseButton
import com.example.geofencing.ui.theme.Label18
import com.example.geofencing.ui.theme.extendedColors
import com.google.android.gms.maps.model.LatLng
// CartMarker는 같은 패키지(ui.map)라 import 불필요

// 팝업 박스: 상하 102 / 좌우 16 인셋, 8dp 라운드, border-focus 1dp, fill-secondary 배경.
private val HeatmapPopupVerticalMargin = 102.dp
private val HeatmapPopupHorizontalMargin = 16.dp
private val HeatmapPopupCorner = 8.dp
// 헤더(타이틀/닫기) 패딩.
private val HeatmapHeaderPadding = 16.dp

// Sector의 "Violation Heatmap" 진입 시 뜨는 팝업. 타이틀 헤더 + 섹터 전체가 보이는 고정 지도(FitGeofence)
// + geofence 경계 + 위반 발생 위치 히트맵. 닫기 버튼으로 dismiss.
@Composable
fun ViolationHeatmapPopup(
    geofence: List<LatLng>,
    carts: List<CartMarker>,
    violationPoints: List<LatLng>,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.extendedColors
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = HeatmapPopupHorizontalMargin,
                    vertical = HeatmapPopupVerticalMargin
                )
        ) {
            val shape = RoundedCornerShape(HeatmapPopupCorner)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(colors.fillSecondary)
                    .border(1.dp, colors.borderFocus, shape)
            ) {
                // 헤더: 타이틀(상하좌 16) + 닫기(우 16).
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Violation Heatmap",
                        style = Label18,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(
                            start = HeatmapHeaderPadding,
                            top = HeatmapHeaderPadding,
                            bottom = HeatmapHeaderPadding
                        )
                    )
                    CloseButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = HeatmapHeaderPadding)
                    )
                }

                // 지도(남은 공간). TODO: 위반 히트맵은 기본 green→red 그라디언트가 흉해서 제거.
                // 재도입 시 앱 톤에 맞는 단색(red) 반투명 그라디언트로 스타일링 + 실데이터 필요.
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    LiveGeofenceMap(
                        content = GeofenceMapContent(geofence = geofence, carts = carts),
                        camera = MapCamera.FitGeofence(zoomFactor = 1.3f),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
