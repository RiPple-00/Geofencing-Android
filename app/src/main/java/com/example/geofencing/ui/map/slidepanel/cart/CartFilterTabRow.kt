package com.example.geofencing.ui.map.slidepanel.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.geofencing.R
import com.example.geofencing.ui.theme.Label14
import com.example.geofencing.ui.theme.RoundedMd
import com.example.geofencing.ui.theme.extendedColors

// StateTabRow(Summary/Sector/Cart)와 같은 캡슐 배경 + 선택 시 채워진 필 디자인이지만, Cart
// 리스트 전용 필터라 버튼 패딩이 다르다(상하좌우 균등 4dp, 높이는 고정 36dp). 폭은 254dp로
// 고정하지 않고 IntrinsicSize.Max + weight(1f)로 가장 넓은 라벨(Compliance) 기준 3등분해서,
// 짧은 라벨(All/Violation)이 억지로 넓어지거나 긴 라벨이 잘리는 일이 없게 한다.
enum class CartFilter(val label: String) {
    ALL("All"),
    VIOLATION("Violation"),
    COMPLIANCE("Compliance")
}

@Composable
fun CartFilterTabRow(
    selectedFilter: CartFilter,
    onFilterSelected: (CartFilter) -> Unit,
    modifier: Modifier = Modifier,
    hasUnseenViolation: Boolean = false
) {
    Row(
        modifier = modifier
            .width(IntrinsicSize.Max)
            .height(36.dp)
            .background(
                color = MaterialTheme.extendedColors.fillSecondary,
                shape = RoundedCornerShape(RoundedMd)
            )
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CartFilter.entries.forEach { filter ->
            CartFilterTabItem(
                filter = filter,
                selected = filter == selectedFilter,
                showWarningBadge = filter == CartFilter.VIOLATION && hasUnseenViolation,
                onClick = { onFilterSelected(filter) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CartFilterTabItem(
    filter: CartFilter,
    selected: Boolean,
    showWarningBadge: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (selected) MaterialTheme.extendedColors.fillPrimary else Color.Transparent,
                    shape = RoundedCornerShape(RoundedMd)
                )
                .clickable(onClick = onClick)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = filter.label,
                style = Label14,
                color = if (selected) {
                    MaterialTheme.extendedColors.textPrimary
                } else {
                    MaterialTheme.extendedColors.textDisabled
                }
            )
        }
        if (showWarningBadge) {
            // 미확인 violation 배지 - 피그마 실측값(아이콘 절대좌표 top 648/left 242, 버튼
            // 절대좌표 top 656/right 258 기준, 같은 360x800 아트보드)에서 역산한 TopEnd
            // 기준 상대 오프셋: x = -4.33dp(버튼 오른쪽 끝보다 안쪽), y = -8dp(버튼 위로 돌출).
            Image(
                painter = painterResource(R.drawable.ic_warning),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4.33).dp, y = (-8).dp)
                    .size(11.667.dp)
            )
        }
    }
}
