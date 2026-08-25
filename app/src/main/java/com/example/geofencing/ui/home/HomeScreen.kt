package com.example.geofencing.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.geofencing.ui.analytics.AnalyticsEvent
import com.example.geofencing.ui.analytics.LocalAnalytics
import com.example.geofencing.ui.cart.CartPage
import com.example.geofencing.ui.cart.CartViewModel
import com.example.geofencing.ui.common.LoadState
import com.example.geofencing.ui.components.AppTopBar
import com.example.geofencing.ui.components.LoadStateContent
import com.example.geofencing.ui.components.SectorTabRow
import com.example.geofencing.ui.map.GeofenceMapContent
import com.example.geofencing.ui.map.LiveGeofenceMap
import com.example.geofencing.ui.map.MapCamera
import com.example.geofencing.ui.map.ViolationHeatmapOverlay
import com.example.geofencing.ui.sector.SectorPage
import com.example.geofencing.ui.sector.SectorViewModel
import com.example.geofencing.ui.theme.extendedColors
import com.example.geofencing.ui.wholesector.WholeSectorPage
import com.example.geofencing.ui.wholesector.WholeSectorUiState

private const val APP_TITLE = "Geofence"
// 배너 라이브 지도에서 geofence 가장자리 여백(dp) — FitGeofence padding.
private const val SectorMapGeofenceMarginDp = 45
// 히트맵 오버레이 지도 추가 확대 배율.
private const val HeatmapZoomFactor = 1.1f

// 탭 셸: AppTopBar + SectorTabRow(크롬) + body. 크롬은 항상 유지되고, body는:
// - 드릴다운으로 열린 카트가 있으면 CartPage (뒤로가기로 닫음)
// - 없으면 선택 탭의 페이지(Whole Sector / Sector)
// 탭/드릴다운 선택 상태는 여기서 소유하고, 각 화면 데이터는 ViewModel(→ Repository)에서 관찰한다.
@Composable
fun HomeScreen(
    wholeSectorState: LoadState<WholeSectorUiState>,
    modifier: Modifier = Modifier,
    onWholeSectorRetry: () -> Unit = {},
    onBellClick: () -> Unit = {}
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
    val movableMap = remember {
        movableContentOf<GeofenceMapContent, MapCamera> { content, camera ->
            LiveGeofenceMap(content = content, camera = camera, modifier = Modifier.fillMaxSize())
        }
    }

    BackHandler(enabled = openCartTarget != null) { openCartTarget = null }

    val sectorViewModel: SectorViewModel = hiltViewModel()
    val cartViewModel: CartViewModel = hiltViewModel()
    val analytics = LocalAnalytics.current

    // 탭 이름은 로드된 WholeSector 상태(Supabase)의 섹터 목록에서 파생 → 실데이터와 일관.
    val sectorNames = (wholeSectorState as? LoadState.Success)?.data?.sectors?.map { it.name }.orEmpty()
    // 로딩/에러 중엔 sectorNames가 비어, 복원된 selectedIndex가 탭 수를 초과할 수 있다(탭 스크롤 크래시).
    // 읽기용 인덱스는 항상 0..sectorNames.size로 보정(원본 selectedIndex는 데이터 로드 후 그대로 복원됨).
    val safeIndex = selectedIndex.coerceIn(0, sectorNames.size)

    // 선택 탭/드릴다운을 각 ViewModel에 반영.
    val sectorName = sectorNames.getOrNull(safeIndex - 1).orEmpty()
    LaunchedEffect(sectorName) { if (sectorName.isNotEmpty()) sectorViewModel.select(sectorName) }
    LaunchedEffect(openCartTarget) { openCartTarget?.let { cartViewModel.select(it.first, it.second) } }

    val sectorState by sectorViewModel.state.collectAsState()
    val cartState by cartViewModel.state.collectAsState()

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
    // 섹터 페이지를 벗어나면(전체 탭/카트 상세) 히트맵을 닫는다.
    LaunchedEffect(safeIndex, openCartTarget) {
        if (safeIndex == 0 || openCartTarget != null) {
            showHeatmap = false
            mapInBanner = true
            closingHeatmap = false
        }
    }
    // 현재 섹터의 지도 콘텐츠(배너/히트맵 공용). Success일 때만 존재.
    val sectorMapContent = (sectorState as? LoadState.Success)?.data
        ?.let { GeofenceMapContent(geofence = it.geofence, carts = it.carts) }
    val bannerCamera = MapCamera.FitGeofence(paddingDp = SectorMapGeofenceMarginDp)
    val heatmapCamera = MapCamera.FitGeofence(zoomFactor = HeatmapZoomFactor)

    // 드릴다운 진입 지점(from)까지 기록하는 공통 경로.
    val openCart: (String, String, String) -> Unit = { sector, cart, from ->
        analytics.log(AnalyticsEvent.CartOpened(sector, cart, from))
        openCartTarget = sector to cart
    }

    // 화면 진입 로그(어느 화면을 보고 있나).
    val currentScreen = when {
        openCartTarget != null -> "CartPage"
        safeIndex == 0 -> "WholeSectorPage"
        else -> "SectorPage"
    }
    LaunchedEffect(currentScreen) { analytics.screen(currentScreen) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.extendedColors.background)
        ) {
        // 크롬(제목+벨 / 탭 바)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.extendedColors.fillHighest)
                .statusBarsPadding()
        ) {
            AppTopBar(
                title = APP_TITLE,
                onBellClick = {
                    analytics.log(AnalyticsEvent.BellClicked)
                    onBellClick()
                }
            )
            SectorTabRow(
                sectorNames = sectorNames,
                selectedIndex = safeIndex,
                // 탭 전환 시 열린 카트는 닫는다.
                onSelect = {
                    analytics.log(AnalyticsEvent.TabSelected(it))
                    selectedIndex = it
                    openCartTarget = null
                }
            )
        }

        // body — 좌우 여백은 각 페이지가 담당(지도 full-bleed 허용).
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (openCartTarget != null) {
                // 카트 상세 — 로딩 스피너/오류(재시도)/정상을 LoadStateContent가 분기.
                LoadStateContent(
                    state = cartState,
                    onRetry = {
                        analytics.log(AnalyticsEvent.RetryClicked("Cart"))
                        cartViewModel.retry()
                    }
                ) { cart ->
                    CartPage(state = cart, onBack = { openCartTarget = null })
                }
            } else {
                when (safeIndex) {
                    0 -> LoadStateContent(
                        state = wholeSectorState,
                        onRetry = {
                            analytics.log(AnalyticsEvent.RetryClicked("WholeSector"))
                            onWholeSectorRetry()
                        }
                    ) { ws ->
                        WholeSectorPage(
                            state = ws,
                            // Violation/Disconnect 카트 클릭 → 해당 섹터의 해당 카트로 드릴다운
                            onViolationClick = { openCart(it.sector, it.cart, "whole_violation") },
                            onDisconnectClick = { openCart(it.sector, it.cart, "whole_disconnect") },
                            // 섹터 카드 클릭 → 해당 섹터 탭으로 전환
                            onSectorClick = { summary ->
                                analytics.log(AnalyticsEvent.SectorOpened(summary.name))
                                val idx = sectorNames.indexOf(summary.name)
                                if (idx >= 0) selectedIndex = idx + 1
                            }
                        )
                    }
                    else -> LoadStateContent(
                        state = sectorState,
                        onRetry = {
                            analytics.log(AnalyticsEvent.RetryClicked("Sector"))
                            sectorViewModel.retry()
                        }
                    ) { sectorUi ->
                        SectorPage(
                            state = sectorUi,
                            // 지도 배너는 공유 지도를 여기서 주입(히트맵 오버레이와 같은 인스턴스).
                            bannerMap = {
                                if (mapInBanner && sectorMapContent != null) {
                                    movableMap(sectorMapContent, bannerCamera)
                                }
                            },
                            onHeatmapClick = {
                                mapInBanner = false
                                showHeatmap = true
                            },
                            // 카트 클릭(violation/disconnect/all cart) → 현재 섹터의 해당 카트로 드릴다운
                            onViolationClick = { openCart(sectorName, it.cart, "sector_violation") },
                            onDisconnectClick = { openCart(sectorName, it.cart, "sector_disconnect") },
                            onCartClick = { openCart(sectorName, it.cart, "sector_cart") }
                        )
                    }
                }
            }
        }
        }

        // Violation Heatmap — 앱 최상단 레이어(크롬 포함 전체를 덮는 스크림). SectorPage 밖(여기)에서
        // 그려야 탭바 위를 덮고, 상단 패딩이 앱 최상단 기준이 된다.
        if (showHeatmap && sectorMapContent != null) {
            ViolationHeatmapOverlay(
                onDismiss = { closingHeatmap = true },
                map = { if (!mapInBanner) movableMap(sectorMapContent, heatmapCamera) }
            )
        }
    }
}
