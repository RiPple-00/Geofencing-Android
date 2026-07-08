package com.example.geofencing.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.geofencing.R
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

// z-order: 1) Map(최하단) 2) sidePanel(직접 구현한 드래그 시트) 3) 검색창(최상단)
// sidePanel은 M3 BottomSheetScaffold(peek/expanded 2단 스냅) 대신 직접 만든 시트.
// 드래그를 놓은 지점에서 그대로 높이가 고정되어야 해서(스냅 없음), sheetHeightPx를
// 손가락 이동량만큼 연속적으로 갱신하고 peek~screen 범위로 clamp만 한다.
// 헤더 영역 = 드래그 핸들, 리스트 영역 = nestedScroll로 "리스트가 끝까지 스크롤된 뒤에만
// 시트가 접히고/펼쳐지는" 동작을 함께 처리.
// TODO: sidePanel 실제 콘텐츠, 검색창 스타일은 실측값 확정되면 교체하세요.
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel()
) {
    val markers by viewModel.markers.collectAsState()
    val cameraPositionState = rememberCameraPositionState {
        // TODO: 실제로는 마커 목록 bounds나 사용자 위치 기준으로 계산하도록 교체
        position = CameraPosition.fromLatLngZoom(LatLng(51.52, 0.135), 12f)
    }
    val hazeState = rememberHazeState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(SidePanelTab.SUMMARY) }
    val context = LocalContext.current
    val mapProperties = remember {
        MapProperties(mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark))
    }

    val density = LocalDensity.current
    val screenHeightPx = with(density) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val peekHeightPx = with(density) { 120.dp.toPx() }
    var sheetHeightPx by remember { mutableFloatStateOf(peekHeightPx) }

    val nestedScrollConnection = remember(peekHeightPx, screenHeightPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // 리스트를 위로 스크롤하려는 제스처(펼치기 방향)는 시트를 먼저 확장한다.
                val delta = available.y
                if (delta >= 0f) return Offset.Zero
                val previousHeight = sheetHeightPx
                sheetHeightPx = (sheetHeightPx - delta).coerceIn(peekHeightPx, screenHeightPx)
                return Offset(0f, previousHeight - sheetHeightPx)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // 리스트가 맨 위까지 스크롤된 뒤에도 남은 아래 방향 제스처는 시트를 접는다.
                val delta = available.y
                if (delta <= 0f) return Offset.Zero
                val previousHeight = sheetHeightPx
                sheetHeightPx = (sheetHeightPx - delta).coerceIn(peekHeightPx, screenHeightPx)
                return Offset(0f, previousHeight - sheetHeightPx)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
            cameraPositionState = cameraPositionState,
            properties = mapProperties
        ) {
            markers.forEach { marker ->
                MapPinMarker(marker = marker)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(with(density) { sheetHeightPx.toDp() })
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black)
                    )
                )
                .nestedScroll(nestedScrollConnection)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(peekHeightPx, screenHeightPx) {
                        detectVerticalDragGestures { change, dragAmount ->
                            change.consume()
                            sheetHeightPx = (sheetHeightPx - dragAmount).coerceIn(peekHeightPx, screenHeightPx)
                        }
                    }
            ) {
                SidePanelHeader(title = "Whole Sector", subtitle = "All Sector 16")
            }
            StateTabRow(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                when (selectedTab) {
                    SidePanelTab.SUMMARY -> {
                        item {
                            AllCartSummaryRow(modifier = Modifier.padding(16.dp))
                        }
                    }
                    SidePanelTab.SECTOR -> {
                        items(sidePanelPlaceholderItems) { item ->
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    SidePanelTab.CART -> {
                        item {
                            Text(
                                text = "Cart 탭 콘텐츠 (TODO)",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }

        MainSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onHamburgerClick = { /* TODO: 메뉴/사이드패널 동작 확정되면 연결 */ },
            hazeState = hazeState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

private val sidePanelPlaceholderItems = listOf("설명 항목 1", "설명 항목 2", "설명 항목 3")
