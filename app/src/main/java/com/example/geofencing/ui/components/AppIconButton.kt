package com.example.geofencing.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.geofencing.ui.theme.extendedColors

// 아이콘 버튼 코어: 아이콘 + 최소 터치타깃(48dp) + 리플 없는 클릭.
// size/tint만 가변, 테두리는 코어 파라미터가 아니라 외부에서 조립(borderBox 등).
// 색은 tint로 입혀 테마 대응(애셋의 baked 색은 fallback).
@Composable
fun AppIconButton(
    iconRes: Int,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.extendedColors.textPrimary
) {
    Box(
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .noRippleClickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(tint),
            modifier = Modifier.size(size)
        )
    }
}
