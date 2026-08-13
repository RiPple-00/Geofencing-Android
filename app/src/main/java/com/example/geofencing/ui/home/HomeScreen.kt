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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.geofencing.ui.cart.CartPage
import com.example.geofencing.ui.cart.CartViewModel
import com.example.geofencing.ui.components.AppTopBar
import com.example.geofencing.ui.components.SectorTabRow
import com.example.geofencing.ui.sector.SectorPage
import com.example.geofencing.ui.sector.SectorViewModel
import com.example.geofencing.ui.theme.extendedColors
import com.example.geofencing.ui.wholesector.WholeSectorPage
import com.example.geofencing.ui.wholesector.WholeSectorUiState

private const val APP_TITLE = "Geofence"

// 탭 셸: AppTopBar + SectorTabRow(크롬) + body. 크롬은 항상 유지되고, body는:
// - 드릴다운으로 열린 카트가 있으면 CartPage (뒤로가기로 닫음)
// - 없으면 선택 탭의 페이지(Whole Sector / Sector)
// 탭/드릴다운 선택 상태는 여기서 소유하고, 각 화면 데이터는 ViewModel(→ Repository)에서 관찰한다.
@Composable
fun HomeScreen(
    sectorNames: List<String>,
    wholeSectorState: WholeSectorUiState,
    modifier: Modifier = Modifier,
    onBellClick: () -> Unit = {}
) {
    // 0 = Whole Sector, 1.. = sectorNames
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    // 드릴다운 대상 카트(섹터명, 카트 id). null이면 탭 페이지. TODO: 회전 보존/정식 네비게이션.
    var openCartTarget by remember { mutableStateOf<Pair<String, String>?>(null) }

    BackHandler(enabled = openCartTarget != null) { openCartTarget = null }

    val sectorViewModel: SectorViewModel = hiltViewModel()
    val cartViewModel: CartViewModel = hiltViewModel()

    // 선택 탭/드릴다운을 각 ViewModel에 반영.
    val sectorName = sectorNames.getOrNull(selectedIndex - 1).orEmpty()
    LaunchedEffect(sectorName) { if (sectorName.isNotEmpty()) sectorViewModel.select(sectorName) }
    LaunchedEffect(openCartTarget) { openCartTarget?.let { cartViewModel.select(it.first, it.second) } }

    val sectorState by sectorViewModel.state.collectAsState()
    val cartState by cartViewModel.state.collectAsState()

    Column(
        modifier = modifier
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
            AppTopBar(title = APP_TITLE, onBellClick = onBellClick)
            SectorTabRow(
                sectorNames = sectorNames,
                selectedIndex = selectedIndex,
                // 탭 전환 시 열린 카트는 닫는다.
                onSelect = {
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
                // 카트 상세(로딩되면 표시). 뒤로가기로 닫음.
                cartState?.let { cart ->
                    CartPage(state = cart, onBack = { openCartTarget = null })
                }
            } else {
                when (selectedIndex) {
                    0 -> WholeSectorPage(
                        state = wholeSectorState,
                        // Violation/Disconnect 카트 클릭 → 해당 섹터의 해당 카트로 드릴다운
                        onViolationClick = { openCartTarget = it.sector to it.cart },
                        onDisconnectClick = { openCartTarget = it.sector to it.cart },
                        // 섹터 카드 클릭 → 해당 섹터 탭으로 전환
                        onSectorClick = { s ->
                            val idx = sectorNames.indexOf(s.name)
                            if (idx >= 0) selectedIndex = idx + 1
                        }
                    )
                    else -> sectorState?.let { s ->
                        SectorPage(
                            state = s,
                            // 카트 클릭(violation/disconnect/all cart) → 현재 섹터의 해당 카트로 드릴다운
                            onViolationClick = { openCartTarget = sectorName to it.cart },
                            onDisconnectClick = { openCartTarget = sectorName to it.cart },
                            onCartClick = { openCartTarget = sectorName to it.cart }
                        )
                    }
                }
            }
        }
    }
}
