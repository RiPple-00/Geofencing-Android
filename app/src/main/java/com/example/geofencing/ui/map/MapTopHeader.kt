package com.example.geofencing.ui.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.geofencing.R
import com.example.geofencing.ui.theme.GeofencingTheme
import com.example.geofencing.ui.theme.Header20
import com.example.geofencing.ui.theme.Label14
import com.example.geofencing.ui.theme.Px3
import com.example.geofencing.ui.theme.PyMd
import com.example.geofencing.ui.theme.extendedColors

// 지도 화면의 상단 고정 헤더
// 구성요소 - Title, Notification Button, Sector Tab Bar
private const val WHOLE_SECTOR_LABEL = "Whole Sector"

@Composable
fun MapTopHeader(
    title: String,
    sectorNames: List<String>,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    initialSelectedIndex: Int = 0,
    // 탭 선택 시 호출
    onSelectIndex: (Int) -> Unit = {}
) {
    // 인덱스 0은 Whole Sector 고정
    val tabs = remember(sectorNames) { listOf(WHOLE_SECTOR_LABEL) + sectorNames }
    val tabBarBorderColor = MaterialTheme.extendedColors.borderDefault

    var selectedIndex by remember { mutableIntStateOf(initialSelectedIndex) }

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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.extendedColors.fillHighest)
            .statusBarsPadding()
    ) {
        // Title + Notification Button
        // 제목과 알림 버튼을 양 끝에 배치
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 23.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = Header20,
                color = MaterialTheme.extendedColors.textPrimary
            )
            Image(
                painter = painterResource(R.drawable.ic_notification),
                contentDescription = "알림",
                modifier = Modifier
                    .padding(end = 10.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onActionClick
                    )
                    .size(24.dp)
            )
        }

        // Sector Tab Bar
        LazyRow(
            state = listState,
            modifier = Modifier
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
                    onClick = {
                        selectedIndex = index
                        onSelectIndex(index)
                    }
                )
            }
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

// 전체 섹터 선택 기본값
@Preview(name = "Header - Whole Sector", widthDp = 360, showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
private fun MapTopHeaderWholeSectorPreview() {
    GeofencingTheme {
        MapTopHeader(
            title = "Geofence",
            sectorNames = listOf("Sector#1", "Sector#2", "Sector#3", "Sector#4"),
            onActionClick = {}
        )
    }
}

// 특정 섹터 선택
@Preview(name = "Header - Sector#3 selected", widthDp = 360, showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
private fun MapTopHeaderSectorSelectedPreview() {
    GeofencingTheme {
        MapTopHeader(
            title = "Geofence",
            sectorNames = listOf("Sector#1", "Sector#2", "Sector#3", "Sector#4"),
            initialSelectedIndex = 3,
            onActionClick = {}
        )
    }
}

// 마지막 섹터 선택
@Preview(name = "Header - Last Sector selected", widthDp = 360, showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
private fun MapTopHeaderLastSectorSelectedPreview() {
    GeofencingTheme {
        MapTopHeader(
            title = "Geofence",
            sectorNames = listOf("Sector#1", "Sector#2", "Sector#3", "Sector#4"),
            initialSelectedIndex = 4,
            onActionClick = {}
        )
    }
}
