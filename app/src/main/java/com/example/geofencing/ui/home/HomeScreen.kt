package com.example.geofencing.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.geofencing.ui.analytics.AnalyticsEvent
import com.example.geofencing.ui.analytics.LocalAnalytics
import com.example.geofencing.ui.cart.CartPage
import com.example.geofencing.ui.cart.CartUiState
import com.example.geofencing.ui.common.LoadState
import com.example.geofencing.ui.components.AppTopBar
import com.example.geofencing.ui.components.LoadStateContent
import com.example.geofencing.ui.components.MapReduceButton
import com.example.geofencing.ui.components.SectorTabRow
import com.example.geofencing.ui.map.GeofenceMapContent
import com.example.geofencing.ui.map.HeatmapMinZoom
import com.example.geofencing.ui.map.LiveGeofenceMap
import com.example.geofencing.ui.map.MapCamera
import com.example.geofencing.ui.map.HeatmapMaxZoom
import com.example.geofencing.ui.map.ViolationHeatmapOverlay
import com.example.geofencing.ui.sector.SectorPage
import com.example.geofencing.ui.sector.SectorPageActions
import com.example.geofencing.ui.sector.SectorUiState
import com.example.geofencing.ui.theme.extendedColors
import com.example.geofencing.ui.wholesector.WholeSectorPage
import com.example.geofencing.ui.wholesector.WholeSectorUiState

private const val APP_TITLE = "Geofence"
// 배너 라이브 지도에서 geofence 가장자리 여백(dp) — FitGeofence padding.
private const val SectorMapGeofenceMarginDp = 45
// 히트맵 지도에서 geofence 가장자리 최소 여백(dp) — FitGeofence padding. 배너(45)보다 좁아 더 크게 보임.
private const val HeatmapGeofenceMarginDp = 15

// 제스처(확대 지도)일 때만 줌 한계 적용, 아니면 null(지도 기본).
private fun zoomIfGestures(gestures: Boolean, zoom: Float): Float? = if (gestures) zoom else null

// 화면 진입 로그용 이름.
private fun currentScreenName(cartOpen: Boolean, safeIndex: Int): String = when {
    cartOpen -> "CartPage"
    safeIndex == 0 -> "WholeSectorPage"
    else -> "SectorPage"
}

// 탭 셸: AppTopBar + SectorTabRow(크롬) + body. 크롬은 항상 유지되고, body는:
// - 드릴다운으로 열린 카트가 있으면 CartPage (뒤로가기로 닫음)
// - 없으면 선택 탭의 페이지(Whole Sector / Sector)
// 탭/드릴다운 선택 상태는 여기서 소유하고, 각 화면 데이터는 ViewModel(→ Repository)에서 관찰한다.
// HomeScreen 콜백 묶음 — 파라미터 수를 줄이고(S107) route 배선을 단순화한다.
data class HomeActions(
    val onSelectSector: (String) -> Unit = {},
    val onSelectCart: (String, String) -> Unit = { _, _ -> },
    val onWholeSectorRetry: () -> Unit = {},
    val onSectorRetry: () -> Unit = {},
    val onCartRetry: () -> Unit = {},
    val onBellClick: () -> Unit = {}
)

@Composable
fun HomeScreen(
    wholeSectorState: LoadState<WholeSectorUiState>,
    sectorState: LoadState<SectorUiState>,
    cartState: LoadState<CartUiState>,
    modifier: Modifier = Modifier,
    actions: HomeActions = HomeActions()
) {
    // 0 = Whole Sector, 1.. = sectorNames
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    // 드릴다운 대상 카트(섹터명, 카트 id). null이면 탭 페이지.
    // rememberSaveable: selectedIndex와 동일하게 구성 변경(회전 등)·프로세스 종료에도 보존.
    // Pair<String,String>은 Serializable이라 별도 Saver 없이 저장됨.
    var openCartTarget by rememberSaveable { mutableStateOf<Pair<String, String>?>(null) }

    // Violation Heatmap 오버레이 — 앱 최상단 레이어(크롬 위)로 띄우기 위해 SectorPage가 아닌 여기서 소유한다.
    // 지도는 배너와 공유(movableContentOf)해 열 때 검은 플래시가 없다. showHeatmap=오버레이 표시,
    // mapInBanner=지도 위치(배너↔오버레이)를 분리해, 닫을 때 배너 재-fit을 스크림 뒤에 숨긴다.
    var showHeatmap by rememberSaveable { mutableStateOf(false) }
    var mapInBanner by remember { mutableStateOf(!showHeatmap) }
    var closingHeatmap by remember { mutableStateOf(false) }
    // 확대(⤢) 버튼 → body를 꽉 채우는 줌 지도(cart식). 라벨 탭 팝업(showHeatmap)과 별개 상태.
    var mapExpanded by rememberSaveable { mutableStateOf(false) }
    val movableMap = remember {
        movableContentOf<GeofenceMapContent, MapCamera, Boolean> { content, camera, gestures ->
            LiveGeofenceMap(
                content = content,
                camera = camera,
                modifier = Modifier.fillMaxSize(),
                gesturesEnabled = gestures,
                // 확대 지도만 15~19 줌 범위로 제한. 배너/팝업은 제스처가 없어 무관.
                minZoom = zoomIfGestures(gestures, HeatmapMinZoom),
                maxZoom = zoomIfGestures(gestures, HeatmapMaxZoom)
            )
        }
    }

    BackHandler(enabled = openCartTarget != null) { openCartTarget = null }
    BackHandler(enabled = mapExpanded) { mapExpanded = false }

    val analytics = LocalAnalytics.current

    // 탭 이름은 로드된 WholeSector 상태(Supabase)의 섹터 목록에서 파생 → 실데이터와 일관.
    val sectorNames = (wholeSectorState as? LoadState.Success)?.data?.sectors?.map { it.name }.orEmpty()
    // 로딩/에러 중엔 sectorNames가 비어, 복원된 selectedIndex가 탭 수를 초과할 수 있다(탭 스크롤 크래시).
    // 읽기용 인덱스는 항상 0..sectorNames.size로 보정(원본 selectedIndex는 데이터 로드 후 그대로 복원됨).
    val safeIndex = selectedIndex.coerceIn(0, sectorNames.size)

    // 선택 탭/드릴다운을 각 ViewModel에 반영.
    val sectorName = sectorNames.getOrNull(safeIndex - 1).orEmpty()
    LaunchedEffect(sectorName) { if (sectorName.isNotEmpty()) actions.onSelectSector(sectorName) }
    LaunchedEffect(openCartTarget) { openCartTarget?.let { actions.onSelectCart(it.first, it.second) } }

    // 히트맵 닫기: 오버레이가 아직 떠 있는 동안 지도를 배너로 되돌려(배너 카메라로 재-fit) 그 프레임을 스크림
    // 뒤에 숨긴 뒤, 다음 프레임에 오버레이를 제거 → 배너에 1.1x가 잠깐 비치지 않는다.
    LaunchedEffect(closingHeatmap) {
        if (closingHeatmap) {
            mapInBanner = true
            withFrameNanos {}
            withFrameNanos {}
            showHeatmap = false
            closingHeatmap = false
        }
    }
    // 선택 탭이 바뀌면(전체/다른 섹터) 또는 카트 상세로 들어가면 히트맵을 닫는다.
    // safeIndex 변화 = 다른 섹터로 전환도 포함 → 스크림 아래 stale 상태(빈 배너/재등장) 방지.
    LaunchedEffect(safeIndex, openCartTarget) {
        showHeatmap = false
        mapInBanner = true
        closingHeatmap = false
        mapExpanded = false
    }
    // 현재 섹터의 지도 콘텐츠(배너/히트맵 공용). Success일 때만 존재.
    val sectorMapContent = (sectorState as? LoadState.Success)?.data
        ?.let { GeofenceMapContent(geofence = it.geofence, carts = it.carts) }
    val bannerCamera = MapCamera.FitGeofence(paddingDp = SectorMapGeofenceMarginDp)
    val heatmapCamera = MapCamera.FitGeofence(paddingDp = HeatmapGeofenceMarginDp)

    // 드릴다운 진입 지점(from)까지 기록하는 공통 경로.
    val openCart: (String, String, String) -> Unit = { sector, cart, from ->
        analytics.log(AnalyticsEvent.CartOpened(sector, cart, from))
        openCartTarget = sector to cart
    }
    // 섹터 카드 클릭 → 해당 섹터 탭으로 전환.
    val openSectorTab: (String) -> Unit = { name ->
        analytics.log(AnalyticsEvent.SectorOpened(name))
        val idx = sectorNames.indexOf(name)
        if (idx >= 0) selectedIndex = idx + 1
    }

    // 화면 진입 로그(어느 화면을 보고 있나).
    val currentScreen = currentScreenName(openCartTarget != null, safeIndex)
    LaunchedEffect(currentScreen) { analytics.screen(currentScreen) }

    // 배너/확대/히트맵이 공유하는 지도 상태·핸들 묶음(파라미터 수 축소용).
    val sectorMap = SectorMap(
        content = sectorMapContent,
        inBanner = mapInBanner,
        expanded = mapExpanded,
        bannerCamera = bannerCamera,
        heatmapCamera = heatmapCamera,
        movable = movableMap
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.extendedColors.background)
        ) {
            HomeChrome(
                sectorNames = sectorNames,
                selectedIndex = safeIndex,
                onBellClick = {
                    analytics.log(AnalyticsEvent.BellClicked)
                    actions.onBellClick()
                },
                // 탭 전환 시 열린 카트는 닫는다.
                onSelectTab = {
                    analytics.log(AnalyticsEvent.TabSelected(it))
                    selectedIndex = it
                    openCartTarget = null
                }
            )

            // body — 카트 상세 / Whole Sector / Sector 라우팅 + 확대(⤢) 줌 지도 오버레이.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    openCartTarget != null -> CartDetail(
                        state = cartState,
                        onRetry = {
                            analytics.log(AnalyticsEvent.RetryClicked("Cart"))
                            actions.onCartRetry()
                        },
                        onBack = { openCartTarget = null }
                    )
                    safeIndex == 0 -> WholeSectorTab(
                        state = wholeSectorState,
                        onRetry = {
                            analytics.log(AnalyticsEvent.RetryClicked("WholeSector"))
                            actions.onWholeSectorRetry()
                        },
                        onOpenCart = openCart,
                        onOpenSectorTab = openSectorTab
                    )
                    else -> SectorTab(
                        state = sectorState,
                        onRetry = {
                            analytics.log(AnalyticsEvent.RetryClicked("Sector"))
                            actions.onSectorRetry()
                        },
                        sectorName = sectorName,
                        onOpenCart = openCart,
                        onHeatmapClick = {
                            mapInBanner = false
                            showHeatmap = true
                        },
                        onExpandMap = { mapExpanded = true },
                        map = sectorMap
                    )
                }
                ExpandedMapOverlay(map = sectorMap, onReduce = { mapExpanded = false })
            }
        }

        // "Violation Heatmap" 라벨 탭 → 팝업(정적, 스크림). 앱 최상단 레이어(크롬 위)를 덮는다.
        HeatmapHost(
            visible = showHeatmap,
            map = sectorMap,
            onDismiss = { closingHeatmap = true }
        )
    }
}

// 크롬: 제목+벨(AppTopBar) / 섹터 탭(SectorTabRow).
@Composable
private fun HomeChrome(
    sectorNames: List<String>,
    selectedIndex: Int,
    onBellClick: () -> Unit,
    onSelectTab: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.extendedColors.fillHighest)
            .statusBarsPadding()
    ) {
        AppTopBar(title = APP_TITLE, onBellClick = onBellClick)
        SectorTabRow(
            sectorNames = sectorNames,
            selectedIndex = selectedIndex,
            onSelect = onSelectTab
        )
    }
}

// 배너/확대/히트맵이 공유하는 지도 상태·핸들. movable은 movableContentOf 지도(같은 인스턴스를 이동).
private data class SectorMap(
    val content: GeofenceMapContent?,
    val inBanner: Boolean,
    val expanded: Boolean,
    val bannerCamera: MapCamera,
    val heatmapCamera: MapCamera,
    val movable: @Composable (GeofenceMapContent, MapCamera, Boolean) -> Unit
)

// 확대(⤢): body(탭바 아래)를 꽉 채우는 줌 지도. 핀치 줌/이동, 축소(⌟)/뒤로로 닫기.
@Composable
private fun ExpandedMapOverlay(map: SectorMap, onReduce: () -> Unit) {
    val content = map.content
    if (!map.expanded || content == null) return
    Box(modifier = Modifier.fillMaxSize()) {
        map.movable(content, map.heatmapCamera, true)
        MapReduceButton(
            onClick = onReduce,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        )
    }
}

@Composable
private fun CartDetail(
    state: LoadState<CartUiState>,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    // 카트 상세 — 로딩 스피너/오류(재시도)/정상을 LoadStateContent가 분기.
    LoadStateContent(state = state, onRetry = onRetry) { cart ->
        CartPage(state = cart, onBack = onBack)
    }
}

@Composable
private fun WholeSectorTab(
    state: LoadState<WholeSectorUiState>,
    onRetry: () -> Unit,
    onOpenCart: (String, String, String) -> Unit,
    onOpenSectorTab: (String) -> Unit
) {
    LoadStateContent(state = state, onRetry = onRetry) { ws ->
        WholeSectorPage(
            state = ws,
            // Violation/Disconnect 카트 클릭 → 해당 섹터의 해당 카트로 드릴다운
            onViolationClick = { onOpenCart(it.sector, it.cart, "whole_violation") },
            onDisconnectClick = { onOpenCart(it.sector, it.cart, "whole_disconnect") },
            onSectorClick = { onOpenSectorTab(it.name) }
        )
    }
}

@Composable
private fun SectorTab(
    state: LoadState<SectorUiState>,
    onRetry: () -> Unit,
    sectorName: String,
    onOpenCart: (String, String, String) -> Unit,
    onHeatmapClick: () -> Unit,
    onExpandMap: () -> Unit,
    map: SectorMap
) {
    val bannerContent = map.content
    LoadStateContent(state = state, onRetry = onRetry) { sectorUi ->
        SectorPage(
            state = sectorUi,
            // 지도 배너는 공유 지도를 여기서 주입(팝업·body 줌 지도와 같은 인스턴스). 배너는 제스처 off.
            bannerMap = {
                if (map.inBanner && !map.expanded && bannerContent != null) {
                    map.movable(bannerContent, map.bannerCamera, false)
                }
            },
            actions = SectorPageActions(
                onHeatmapClick = onHeatmapClick,
                onExpandMap = onExpandMap,
                // 카트 클릭(violation/disconnect/all cart) → 현재 섹터의 해당 카트로 드릴다운
                onViolationClick = { onOpenCart(sectorName, it.cart, "sector_violation") },
                onDisconnectClick = { onOpenCart(sectorName, it.cart, "sector_disconnect") },
                onCartClick = { onOpenCart(sectorName, it.cart, "sector_cart") }
            )
        )
    }
}

// "Violation Heatmap" 팝업 호스트. 지도는 배너와 공유해 열 때 검은 플래시가 없다.
@Composable
private fun HeatmapHost(
    visible: Boolean,
    map: SectorMap,
    onDismiss: () -> Unit
) {
    val content = map.content
    if (!visible || content == null) return
    ViolationHeatmapOverlay(
        onDismiss = onDismiss,
        // 팝업은 geofence 경계를 자세히 보기 위한 화면 → 카트 마커는 숨긴다(carts 제거).
        map = {
            if (!map.inBanner) {
                map.movable(content.copy(carts = emptyList()), map.heatmapCamera, false)
            }
        }
    )
}
