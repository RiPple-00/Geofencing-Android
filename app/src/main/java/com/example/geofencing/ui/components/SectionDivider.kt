package com.example.geofencing.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.geofencing.ui.theme.extendedColors

// 모든 페이지 공용 섹션 구분선. 라인(full-width, 0.5dp/border-focus)은 고정이고,
// 라인 기준 상/하단 간격은 사용처마다 달라(소수 포함) 매번 입력한다.
@Composable
fun SectionDivider(
    top: Dp,
    bottom: Dp,
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        modifier = modifier.padding(top = top, bottom = bottom),
        thickness = 0.5.dp,
        color = MaterialTheme.extendedColors.borderFocus
    )
}
