package com.example.geofencing.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.geofencing.R
import com.example.geofencing.ui.theme.CardShape
import com.example.geofencing.ui.theme.extendedColors

// AppIconButton(코어)의 의미 래퍼. 애셋+크기+색 조합을 여기서 고정.

// 뒤로가기 (breadcrumb 좌측). 전용 좌측 화살표 애셋이라 뒤집기 불필요.
@Composable
fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) =
    AppIconButton(
        iconRes = R.drawable.ic_arrow_left_24,
        contentDescription = "뒤로",
        onClick = onClick,
        modifier = modifier,
        size = 24.dp,
        tint = MaterialTheme.extendedColors.borderStrong
    )

// 팝업 닫기 (X).
@Composable
fun CloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) =
    AppIconButton(
        iconRes = R.drawable.ic_close,
        contentDescription = "닫기",
        onClick = onClick,
        modifier = modifier,
        size = 24.dp,
        tint = MaterialTheme.extendedColors.textSecondary
    )

// 지도 확대(⤢) / 축소(⌟) — 테두리+배경 있는 모서리 버튼. 박스 전체가 클릭 영역.
@Composable
fun MapExpandButton(onClick: () -> Unit, modifier: Modifier = Modifier) =
    MapCornerButton(R.drawable.ic_expand, "확대", onClick, modifier)

@Composable
fun MapReduceButton(onClick: () -> Unit, modifier: Modifier = Modifier) =
    MapCornerButton(R.drawable.ic_reduction, "축소", onClick, modifier)

// TODO(측정): 박스 패딩/배경 fill 실측값 필요. 지금은 추정치(패딩 8dp, fill=fillSecondary).
private val MapCornerButtonPadding = 8.dp
private val MapCornerIconSize = 21.dp

@Composable
private fun MapCornerButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clip(CardShape)
            .background(MaterialTheme.extendedColors.fillSecondary)
            .border(1.dp, MaterialTheme.extendedColors.borderDefault, CardShape)
            .noRippleClickable(onClick)
            .padding(MapCornerButtonPadding),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(MaterialTheme.extendedColors.textSecondary),
            modifier = Modifier.size(MapCornerIconSize)
        )
    }
}
