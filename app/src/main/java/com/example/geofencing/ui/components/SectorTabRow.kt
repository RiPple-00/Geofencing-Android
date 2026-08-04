package com.example.geofencing.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.geofencing.ui.theme.GeofencingTheme
import com.example.geofencing.ui.theme.Label14
import com.example.geofencing.ui.theme.Px3
import com.example.geofencing.ui.theme.PyMd
import com.example.geofencing.ui.theme.extendedColors

private const val WHOLE_SECTOR_LABEL = "Whole Sector"

// 섹터 탭 바(controlled): 선택 상태는 밖(HomeScreen 등)이 소유하고 여기선 selectedIndex를 받아 그린다.
// 인덱스 0은 항상 "Whole Sector", 이후가 sectorNames. 선택 탭은 자동으로 가운데로 스크롤된다.
@Composable
fun SectorTabRow(
    sectorNames: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = remember(sectorNames) { listOf(WHOLE_SECTOR_LABEL) + sectorNames }
    val tabBarBorderColor = MaterialTheme.extendedColors.borderDefault

    val listState = rememberLazyListState()
    // 선택 탭을 가운데로
    LaunchedEffect(selectedIndex) {
        // 화면 밖이면 화면 안으로 끌고옴
        if (listState.layoutInfo.visibleItemsInfo.none { it.index == selectedIndex }) {
            listState.scrollToItem(selectedIndex)
        }
        val info = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == selectedIndex }
            ?: return@LaunchedEffect
        val viewport = listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset
        // 탭 중심을 화면 중앙에 맞춤
        listState.animateScrollBy((info.offset - (viewport - info.size) / 2).toFloat())
    }

    LazyRow(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val stroke = 1.dp.toPx()
                drawLine(
                    color = tabBarBorderColor,
                    start = Offset(0f, size.height - stroke / 2f),
                    end = Offset(size.width, size.height - stroke / 2f),
                    strokeWidth = stroke
                )
            }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(tabs) { index, label ->
            SectorTab(
                label = label,
                selected = index == selectedIndex,
                onClick = { onSelect(index) }
            )
        }
    }
}

@Composable
private fun SectorTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .defaultMinSize(minWidth = 24.dp, minHeight = 24.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            // 세로/가로 여백
            .padding(horizontal = Px3, vertical = PyMd),
        horizontalArrangement = Arrangement.spacedBy(6.dp), // 탭 간격
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = Label14,
            color = if (selected) {
                MaterialTheme.extendedColors.textPrimary
            } else {
                MaterialTheme.extendedColors.textDisabled
            }
        )
    }
}

@Preview(name = "SectorTabRow - Whole Sector", widthDp = 360, showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
private fun SectorTabRowWholeSectorPreview() {
    GeofencingTheme {
        SectorTabRow(
            sectorNames = listOf("Sector#1", "Sector#2", "Sector#3", "Sector#4"),
            selectedIndex = 0,
            onSelect = {}
        )
    }
}

@Preview(name = "SectorTabRow - Sector#3 selected", widthDp = 360, showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
private fun SectorTabRowSelectedPreview() {
    GeofencingTheme {
        SectorTabRow(
            sectorNames = listOf("Sector#1", "Sector#2", "Sector#3", "Sector#4"),
            selectedIndex = 3,
            onSelect = {}
        )
    }
}
