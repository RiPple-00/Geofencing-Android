package com.example.geofencing.ui.map.slidepanel.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.geofencing.ui.theme.Label14
import com.example.geofencing.ui.theme.RoundedMd
import com.example.geofencing.ui.theme.extendedColors

// StateTabRow(Summary/Sector/Cart)와 같은 캡슐 배경 + 선택 시 채워진 필 디자인이지만, Cart
// 리스트 전용 필터라 크기가 고정값(254x36)이고 버튼 패딩도 다르다(상하좌우 균등 4dp, 높이는
// 고정하지 않고 텍스트 높이 + 8dp로 자연스럽게 결정).
enum class CartFilter(val label: String) {
    ALL("All"),
    VIOLATION("Violation"),
    COMPLIANCE("Compliance")
}

@Composable
fun CartFilterTabRow(
    selectedFilter: CartFilter,
    onFilterSelected: (CartFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .width(254.dp)
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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
}
