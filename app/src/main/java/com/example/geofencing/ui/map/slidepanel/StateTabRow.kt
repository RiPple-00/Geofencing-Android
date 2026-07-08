package com.example.geofencing.ui.map.slidepanel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

// 피그마 tab_root/list/tab_item 구조. list(가로 배치)는 328dp를 3등분(106.667dp)한 값이라
// fillMaxWidth + weight(1f)로 반응형 처리(고정폭 대신).
enum class SlidePanelTab(val label: String) {
    SUMMARY("Summary"),
    SECTOR("Sector"),
    CART("Cart")
}

@Composable
fun StateTabRow(
    selectedTab: SlidePanelTab,
    onTabSelected: (SlidePanelTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                color = MaterialTheme.extendedColors.fillSecondary,
                shape = RoundedCornerShape(RoundedMd)
            )
            .padding(4.dp)
    ) {
        SlidePanelTab.entries.forEach { tab ->
            StateTabItem(
                tab = tab,
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StateTabItem(
    tab: SlidePanelTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .background(
                color = if (selected) MaterialTheme.extendedColors.fillPrimary else Color.Transparent,
                shape = RoundedCornerShape(RoundedMd)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tab.label,
            style = Label14,
            color = if (selected) {
                MaterialTheme.extendedColors.textPrimary
            } else {
                MaterialTheme.extendedColors.textDisabled
            }
        )
    }
}
