package com.example.geofencing.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.geofencing.ui.components.AppTopBar
import com.example.geofencing.ui.components.SectorTabRow
import com.example.geofencing.ui.theme.GeofencingTheme
import com.example.geofencing.ui.theme.extendedColors

// 지도 화면(구 구조)의 상단 고정 헤더. 이제 공용 AppTopBar + SectorTabRow를 조립하는 얇은
// stateful wrapper로, 탭 선택 상태만 내부에서 들고 있는다. (신규 페이지 구조에서는 HomeScreen이
// 이 조립과 선택 상태를 직접 소유하게 되고, 이 wrapper는 MapScreen이 남아있는 동안만 유지.)
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
    var selectedIndex by remember { mutableIntStateOf(initialSelectedIndex) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.extendedColors.fillHighest)
            .statusBarsPadding()
    ) {
        AppTopBar(title = title, onBellClick = onActionClick)
        SectorTabRow(
            sectorNames = sectorNames,
            selectedIndex = selectedIndex,
            onSelect = {
                selectedIndex = it
                onSelectIndex(it)
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
