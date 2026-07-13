package com.example.geofencing.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.geofencing.R
import com.example.geofencing.ui.map.slidepanel.SlidePanelHeader
import com.example.geofencing.ui.map.slidepanel.StateTabRow
import com.example.geofencing.ui.map.slidepanel.SlidePanelTab
import com.example.geofencing.ui.map.slidepanel.cart.CartFilter
import com.example.geofencing.ui.map.slidepanel.cart.CartFilterTabRow
import com.example.geofencing.ui.map.slidepanel.cart.CartListItem
import com.example.geofencing.ui.map.slidepanel.cart.CartListItemCard
import com.example.geofencing.ui.map.slidepanel.cart.CartListPageSize
import com.example.geofencing.ui.map.slidepanel.cart.CartPaginationRow
import com.example.geofencing.ui.map.slidepanel.cart.CartSummaryRow
import com.example.geofencing.ui.map.slidepanel.sector.SectorDetailCard
import com.example.geofencing.ui.map.slidepanel.sector.SectorListItemCard
import com.example.geofencing.ui.map.slidepanel.sector.SectorPageIndicator
import com.example.geofencing.ui.map.slidepanel.summary.AllCartSummaryRow
import com.example.geofencing.ui.map.slidepanel.summary.DrivingStatusCard
import com.example.geofencing.ui.map.slidepanel.summary.EventCodeSection
import com.example.geofencing.ui.map.slidepanel.summary.GeoFencingStatusCard
import com.example.geofencing.ui.map.slidepanel.summary.RefreshStatusRow
import com.example.geofencing.ui.theme.Body14
import com.example.geofencing.ui.theme.DarkBackground
import com.example.geofencing.ui.theme.DarkBrandPrimary
import com.example.geofencing.ui.theme.DarkCriticalPrimary
import com.example.geofencing.ui.theme.extendedColors
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

// 지오펜스 폴리곤을 그릴 때 PolyUtil.simplify로 다듬는 허용 오차(미터) - 점이 너무
// 많은 경계선도 시각적으로 거의 동일하게, 더 가볍게 그리기 위함.
private const val GEOFENCE_SIMPLIFY_TOLERANCE_METERS = 5.0

// MainSearchBar 자체 높이(72dp, statusBarsPadding 별도) - 지도 contentPadding에 반영해서
// 카메라 프레이밍이 검색바에 가려지는 영역을 정중앙으로 착각하지 않게 한다.
private val SEARCH_BAR_HEIGHT_DP = 72.dp

// TODO: slidePanel 실제 콘텐츠, 검색창 스타일은 실측값 확정되면 교체하세요.
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel()
) {
    val markers by viewModel.markers.collectAsState()
    val siteCartSummary by viewModel.siteCartSummary.collectAsState()
    val sectorDetails by viewModel.sectorDetails.collectAsState()
    val sectorOverviews by viewModel.sectorOverviews.collectAsState()
    val geofenceEvents by viewModel.geofenceEvents.collectAsState()
    val hasUnseenViolation by viewModel.hasUnseenViolation.collectAsState()
    val lastRefreshedAt by viewModel.lastRefreshedAt.collectAsState()
    val initialCameraBounds by viewModel.initialCameraBounds.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val density = LocalDensity.current
    val cameraPositionState = rememberCameraPositionState {
        // 실제 위치는 initialCameraBounds가 도착하는 즉시(LaunchedEffect) 모든 섹터가
        // 보이도록 다시 맞춰진다. 그 전 짧은 로딩 구간에서만 보이는 임시값.
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 2f)
    }
    // 맨 처음 데이터가 들어왔을 때 모든 섹터를 한 화면에 담도록 카메라를 맞춘다.
    // initialCameraBounds는 ViewModel에서 최초 1회만 채워지므로, 이후 폴링으로 사용자가
    // 보고 있는 지도 시점이 리셋되지는 않는다.
    LaunchedEffect(initialCameraBounds) {
        val bounds = initialCameraBounds ?: return@LaunchedEffect
        val paddingPx = with(density) { 48.dp.toPx().toInt() }
        runCatching {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, paddingPx))
        }
    }
    val hazeState = rememberHazeState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var searchQuery by remember { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(SlidePanelTab.SUMMARY) }
    var selectedCartFilter by remember { mutableStateOf(CartFilter.ALL) }
    var cartPage by remember { mutableStateOf(0) }
    // 필터가 바뀌면 목록 길이가 달라져 이전 페이지 번호가 더 이상 유효하지 않을 수 있어 첫 페이지로 리셋.
    // Violation 필터를 실제로 열어본 시점을 "확인함"으로 기록해서 warning 배지를 내린다.
    LaunchedEffect(selectedCartFilter) {
        cartPage = 0
        if (selectedCartFilter == CartFilter.VIOLATION) {
            viewModel.acknowledgeViolations()
        }
    }
    // TODO: 카트 목록 API가 생기면 더미 데이터를 실제 응답으로 교체.
    val dummyCartItems = remember {
        List(10) { index -> CartListItem(id = index + 1, name = "Cart #${index + 1}", violating = index % 3 == 0) }
    }
    val context = LocalContext.current
    val mapProperties = remember {
        MapProperties(mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark))
    }
    // 확대/축소 +/- 버튼 숨김. Google 로고는 Maps Platform 이용약관상 가릴 수 없어 그대로 둠.
    val mapUiSettings = remember { MapUiSettings(zoomControlsEnabled = false) }
    val sectorPagerState = rememberPagerState(pageCount = { sectorDetails.size })
    val listState = rememberLazyListState()

    val configurationScreenHeightDp = LocalConfiguration.current.screenHeightDp
    // 초기값은 LocalConfiguration 추정치(첫 프레임 coerceIn 안전용) - 측정 즉시 정확한 값으로 교체.
    var screenHeightPx by remember {
        mutableFloatStateOf(with(density) { configurationScreenHeightDp.dp.toPx() })
    }
    // 패널 상단 fade(투명→검정) 구간의 절대 크기. 원래 CSS 비율(0.1424)은 1600px(=dp) 대비 계산된 값
    // 1600 * 0.1424 = 227.84dp라는 고정 dp 값으로 확정
    val fadeHeightPx = with(density) { 227.84.dp.toPx() }
    // 기기별/내비게이션 모드별(제스처 vs 3버튼)로 높이가 다르고 런타임에도 바뀔 수 있어서
    // 고정값 대신 WindowInsets로 실측. 디자인은 이 여백을 고려하지 않았으므로 peek
    // 높이/리스트 하단 여백에 더해줘서 콘텐츠가 시스템 내비게이션 바에 가려지지 않게 한다.
    val navigationBarBottomDp = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val navigationBarBottomPx = with(density) { navigationBarBottomDp.toPx() }
    // Summary는 접힌 기본 상태에서도 "All Cart 50 / Violation 3 Unit >" 구간까지 보여야 함. 279는 figma 실측값
    val summaryPeekHeightPx = with(density) { 279.dp.toPx() } + navigationBarBottomPx
    // Sector는 접힌 기본 상태에서도 캐러셀 카드+네비게이션 circle까지 보여야 함. 384는 figma 실측값
    val sectorPeekHeightPx = with(density) { 384.dp.toPx() } + navigationBarBottomPx
    // Cart는 접힌 기본 상태에서 필터 버튼 + 첫 리스트 아이템까지 보여야 함. 피그마 기준
    // 아트보드 높이 800dp에서 상단 패널과의 간격 368dp를 뺀 432dp가 실측값.
    val cartPeekHeightPx = with(density) { 432.dp.toPx() } + navigationBarBottomPx
    val peekHeightPx = when (selectedTab) {
        SlidePanelTab.SUMMARY -> summaryPeekHeightPx
        SlidePanelTab.SECTOR -> sectorPeekHeightPx
        SlidePanelTab.CART -> cartPeekHeightPx
    }
    var sheetHeightPx by remember { mutableFloatStateOf(peekHeightPx) }
    // 탭마다 최소(peek) 높이가 달라서(Summary 279dp, Sector 384dp, Cart 432dp), 낮게 접힌
    // 상태에서 탭을 전환하면 새 탭의 최소 높이보다 낮게 남아있을 수 있다 - 그 경우에만 최소
    // 높이로 올려준다.
    LaunchedEffect(selectedTab) {
        sheetHeightPx = sheetHeightPx.coerceAtLeast(peekHeightPx)
    }

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates -> screenHeightPx = coordinates.size.height.toFloat() }
    ) {
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings,
            // 상단 검색바/상태바, 하단 슬라이드 패널이 가리는 영역을 지도에 알려줘야 카메라
            // 프레이밍(중심/줌 맞춤)이 실제로 "보이는" 지도 영역 기준으로 계산된다. 이게
            // 없으면 전체 뷰 기준으로 중앙 정렬되어 패널이 클수록 위로 쏠려 보인다.
            contentPadding = PaddingValues(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + SEARCH_BAR_HEIGHT_DP,
                bottom = with(density) { sheetHeightPx.toDp() }
            )
        ) {
            // TODO: 줌 레벨에 따라 점(마커만)/폴리곤(경계선)으로 다르게 렌더링해야 하는데,
            // 디자인이 아직 없어서 지금은 줌 레벨과 무관하게 항상 폴리곤으로 그린다.
            sectorOverviews.forEach { overview ->
                val hasViolation = sectorDetails.find { it.id == overview.id }?.cartSummary?.violating?.let { it > 0 } ?: false
                val simplifiedPoints = remember(overview.geofence) {
                    PolyUtil.simplify(overview.geofence, GEOFENCE_SIMPLIFY_TOLERANCE_METERS)
                }
                Polygon(
                    points = simplifiedPoints,
                    strokeColor = if (hasViolation) DarkCriticalPrimary else DarkBrandPrimary,
                    strokeWidth = 4f,
                    fillColor = (if (hasViolation) DarkCriticalPrimary else DarkBrandPrimary).copy(alpha = 0.12f)
                )
            }
            markers.forEach { marker ->
                MapPinMarker(
                    marker = marker,
                    onClick = {
                        // 마커 = Sector 탭 캐러셀의 카드 하나와 1:1 대응(같은 sectorId).
                        // 탭을 Sector로 전환하고, 해당 카드로 캐러셀을 넘긴 뒤, 리스트를
                        // 맨 위로 올려서 캐러셀이 바로 보이게 한다.
                        val targetPage = sectorDetails.indexOfFirst { it.id.toString() == marker.id }
                        if (targetPage >= 0) {
                            selectedTab = SlidePanelTab.SECTOR
                            coroutineScope.launch {
                                listState.scrollToItem(0)
                                sectorPagerState.animateScrollToPage(targetPage)
                            }
                        }
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(with(density) { sheetHeightPx.toDp() })
                .hazeSource(state = hazeState, zIndex = 1f)
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .drawWithContent {
                    drawRect(DarkBackground)
                    val firstItem = listState.layoutInfo.visibleItemsInfo.firstOrNull()
                    if (firstItem != null && firstItem.index == 0) {
                        // fadeHeightPx(고정 227.84dp)만큼만 투명→검정으로 전환하고, 그
                        // 이후로는 verticalGradient 기본 TileMode.Clamp가 마지막 색상
                        // (검정)을 그대로 이어간다 - 화면 높이 비율이 아닌 절대 거리 기준.
                        val topPx = firstItem.offset.toFloat()
                        if (topPx < fadeHeightPx) {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, DarkBackground),
                                    startY = topPx,
                                    endY = topPx + fadeHeightPx
                                ),
                                blendMode = BlendMode.DstIn
                            )
                        }
                    }
                    // 콘텐츠는 항상 완전 불투명하게, 구멍과 무관하게 그 위에 올림
                    drawContent()
                }
                .nestedScroll(nestedScrollConnection)
        ) {
            // 탭 상관없이 마지막 카드와 패널 하단 사이 간격은 항상 68dp - 각 탭 마지막
            // item에 개별로 bottom padding을 주지 않고 contentPadding으로 통일
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(bottom = 68.dp + navigationBarBottomDp)
            ) {
                item {
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
                        SlidePanelHeader(title = "Whole Sector", subtitle = "All Sector ${sectorDetails.size}")
                    }
                }
                item {
                    // Summary/Sector/Cart 버튼과 다음에 오는 아이템 사이 간격은 항상 24dp -
                    // 버튼 자신에게 bottom padding으로 줘서, 탭별 첫 아이템에 top padding을
                    // 매번 챙겨줄 필요가 없도록 한다.
                    StateTabRow(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp),
                        hasUnseenViolation = hasUnseenViolation
                    )
                }
                when (selectedTab) {
                    SlidePanelTab.SUMMARY -> {
                        item {
                            // 위(StateTabRow)와의 24dp는 StateTabRow 자체 bottom padding이 담당.
                            // 아래(새로고침)와 22dp 실측값.
                            AllCartSummaryRow(
                                modifier = Modifier.padding(bottom = 22.dp),
                                allCartTotal = siteCartSummary?.total ?: 0,
                                violationCount = siteCartSummary?.violating ?: 0,
                                complianceCount = siteCartSummary?.compliant ?: 0
                            )
                        }
                        item {
                            RefreshStatusRow(
                                lastRefreshedAt = lastRefreshedAt?.let(::formatLocalTimestamp) ?: "-",
                                onRefreshClick = viewModel::refresh
                            )
                        }
                        item {
                            // 새로고침과의 간격 12dp 실측값.
                            EventCodeSection(
                                modifier = Modifier.padding(top = 12.dp),
                                events = geofenceEvents.map { event ->
                                    "Cart #${event.cartId} GPS Violation (Sector #${event.sectorId})"
                                }
                            )
                        }
                        item {
                            DrivingStatusCard(
                                modifier = Modifier.padding(top = 16.dp),
                                driving = siteCartSummary?.driving ?: 0,
                                idle = siteCartSummary?.idle ?: 0
                            )
                        }
                        item {
                            // 하단 여백은 LazyColumn의 contentPadding(68dp)이 담당.
                            GeoFencingStatusCard(
                                modifier = Modifier.padding(top = 16.dp),
                                compliant = siteCartSummary?.compliant ?: 0,
                                violating = siteCartSummary?.violating ?: 0
                            )
                        }
                    }
                    SlidePanelTab.SECTOR -> {
                        item {
                            // 위(StateTabRow)와의 24dp는 StateTabRow 자체 bottom padding이 담당.
                            HorizontalPager(
                                state = sectorPagerState,
                                modifier = Modifier.height(154.dp)
                            ) { page ->
                                val sector = sectorDetails[page]
                                SectorDetailCard(
                                    name = sector.name,
                                    address = sector.address,
                                    allCartCount = sector.cartSummary.total,
                                    violationCount = sector.cartSummary.violating,
                                    complianceCount = sector.cartSummary.compliant
                                )
                            }
                        }
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                SectorPageIndicator(
                                    pageCount = sectorDetails.size,
                                    currentPage = sectorPagerState.currentPage
                                )
                            }
                        }
                        // 카드를 item list 형식으로 나타낼 때 간격은 항상 16dp.
                        // border가 빨간(이상이 있는) 섹터가 위로 오도록 정렬 - 캐러셀(위
                        // HorizontalPager)은 마커 클릭 시 페이지 인덱스로 찾아가야 해서
                        // sectorDetails 원본 순서를 그대로 유지하고, 이 리스트만 별도로 정렬.
                        val sortedSectorDetails = sectorDetails.sortedByDescending { it.cartSummary.violating > 0 }
                        items(sortedSectorDetails, key = { it.id }) { sector ->
                            SectorListItemCard(
                                name = sector.name,
                                hasViolation = sector.cartSummary.violating > 0,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                    SlidePanelTab.CART -> {
                        item {
                            // 위(StateTabRow)와의 24dp는 StateTabRow 자체 bottom padding이 담당.
                            CartSummaryRow(
                                allCartTotal = siteCartSummary?.total ?: 0,
                                violationCount = siteCartSummary?.violating ?: 0,
                                complianceCount = siteCartSummary?.compliant ?: 0
                            )
                        }
                        item {
                            // 제목은 왼쪽 끝, 필터 버튼은 오른쪽 끝에 붙인다(간격 고정 대신 양끝 정렬).
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 26.dp, end = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${siteCartSummary?.total ?: 0} Cart",
                                    modifier = Modifier
                                        .width(48.dp)
                                        .height(20.dp),
                                    style = Body14,
                                    color = MaterialTheme.extendedColors.textSecondary
                                )
                                CartFilterTabRow(
                                    selectedFilter = selectedCartFilter,
                                    onFilterSelected = { selectedCartFilter = it },
                                    hasUnseenViolation = hasUnseenViolation
                                )
                            }
                        }
                        val filteredCartItems = when (selectedCartFilter) {
                            CartFilter.ALL -> dummyCartItems
                            CartFilter.VIOLATION -> dummyCartItems.filter { it.violating }
                            CartFilter.COMPLIANCE -> dummyCartItems.filter { !it.violating }
                        }
                        // 한 페이지 최대 CartListPageSize(7)개, 그 이상은 CartPaginationRow로 분할.
                        val cartPages = filteredCartItems.chunked(CartListPageSize)
                        val cartPageCount = cartPages.size.coerceAtLeast(1)
                        val pagedCartItems = cartPages.getOrElse(cartPage) { emptyList() }
                        items(pagedCartItems, key = { it.id }) { cart ->
                            CartListItemCard(
                                name = cart.name,
                                hasViolation = cart.violating,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                        if (cartPageCount > 1) {
                            item {
                                CartPaginationRow(
                                    pageCount = cartPageCount,
                                    currentPage = cartPage,
                                    onPageSelected = { cartPage = it },
                                    modifier = Modifier.padding(top = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 검색창이 포커스되면 화면 전체를 덮는 딤 처리 - z-index는 검색창보다 아래(지도/패널
        // 위, 검색창 아래)라 검색창 자신은 딤에 가려지지 않는다. 딤을 탭하면 포커스를
        // 해제한다(입력했던 텍스트는 그대로 유지).
        if (isSearchFocused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.70f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { focusManager.clearFocus() }
                    )
            )
        }

        MainSearchBar(
            query = searchQuery,
            onQueryChange = { query ->
                searchQuery = query
                viewModel.onSearchQueryChanged(query)
            },
            onHamburgerClick = { /* TODO: 메뉴/사이드패널 동작 확정되면 연결 */ },
            hazeState = hazeState,
            onFocusChanged = { focused -> isSearchFocused = focused },
            searchResults = searchResults,
            resultHasViolation = { result ->
                sectorDetails.find { it.id == result.id }?.cartSummary?.violating?.let { it > 0 } ?: false
            },
            onResultClick = { result ->
                // 탭 전환 없이 지도 카메라만 해당 섹터로 이동 - Sector 탭으로는 안 넘어간다.
                // 패널은 검색 클릭마다 항상 현재 탭의 기본(peek) 높이로 되돌린다. 그래야 위
                // contentPadding 계산에 쓰이는 패널 높이가 카메라 이동 시점과 어긋나지 않는다
                // (펼쳐진 패널 높이로 프레이밍한 뒤 패널만 접히면 다시 안 맞아 보임).
                sheetHeightPx = peekHeightPx
                val bounds = sectorOverviews.find { it.id == result.id }?.geofence?.let(::buildBoundsOrNull)
                if (bounds != null) {
                    val paddingPx = with(density) { 48.dp.toPx().toInt() }
                    coroutineScope.launch {
                        runCatching {
                            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, paddingPx))
                        }
                    }
                }
                focusManager.clearFocus()
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // 하단 내비게이션 바(뒤로가기/홈/최근 앱) 영역의 배경 - 슬라이드 패널 높이 계산과
        // 무관하게 항상 이 영역을 덮도록, 검색창처럼 Box의 마지막 자식(최상단 z-index)으로 고정.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(navigationBarBottomDp)
                .background(DarkBackground)
        )
    }
}

// 섹터 하나의 지오펜스 꼭짓점을 모두 포함하는 경계 - 폴리곤 크기/모양과 무관하게 화면에
// 딱 맞춰 확대하기 위해, 대표점(Pole of Inaccessibility) 대신 전체 좌표를 사용한다.
private fun buildBoundsOrNull(points: List<LatLng>): LatLngBounds? {
    if (points.isEmpty()) return null
    val builder = LatLngBounds.Builder()
    points.forEach { builder.include(it) }
    return runCatching { builder.build() }.getOrNull()
}

// BE는 UTC ISO 8601만 주고 로컬 타임존(KST 등) 변환/표시는 App이 담당한다는 API 명세에
// 따른 포맷팅. 기기의 시스템 기본 타임존을 그대로 사용한다.
private fun formatLocalTimestamp(instant: Instant): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm").withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}
