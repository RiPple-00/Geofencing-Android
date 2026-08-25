package com.example.geofencing.ui.map

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.geofencing.ui.components.CloseButton
import com.example.geofencing.ui.components.noRippleClickable
import com.example.geofencing.ui.theme.Label18
import com.example.geofencing.ui.theme.extendedColors

// 팝업 박스: 상하 102 / 좌우 16 인셋, 8dp 라운드, border-focus 1dp, fill-secondary 배경.
private val HeatmapPopupVerticalMargin = 102.dp
private val HeatmapPopupHorizontalMargin = 16.dp
private val HeatmapPopupCorner = 8.dp
private val HeatmapHeaderPadding = 16.dp
// 뒤 화면을 어둡게 덮는 스크림(Dialog 딤과 유사).
private val HeatmapScrimColor = Color(0x99000000)

// Sector의 "Violation Heatmap" 오버레이. Dialog가 아니라 화면 내 오버레이 —
// 지도는 배너에서 이미 로드된 것을 movableContentOf로 옮겨 받으므로(map 슬롯), 열 때 재생성/검은 플래시가 없다.
// 스크림 탭 / 뒤로가기 / 닫기 버튼으로 dismiss.
@Composable
fun ViolationHeatmapOverlay(
    onDismiss: () -> Unit,
    map: @Composable () -> Unit
) {
    val colors = MaterialTheme.extendedColors
    BackHandler(onBack = onDismiss)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HeatmapScrimColor)
            .noRippleClickable(onDismiss) // 바깥 탭으로 닫기
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
                    // 팝업 내부 탭 소비(스크림 dismiss 방지). clickable이 아니라 접근성 트리에 클릭 롤을 안 남김.
                    .pointerInput(Unit) { detectTapGestures {} }
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

                // 지도(남은 공간) — 배너에서 옮겨온 이미 로드된 지도.
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    map()
                }
            }
        }
    }
}
