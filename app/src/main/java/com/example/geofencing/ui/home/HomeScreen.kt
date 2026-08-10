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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.geofencing.ui.cart.CartPage
import com.example.geofencing.ui.cart.CartUiState
import com.example.geofencing.ui.cart.cartStateFor
import com.example.geofencing.ui.components.AppTopBar
import com.example.geofencing.ui.components.SectorTabRow
import com.example.geofencing.ui.mock.mockSectorByName
import com.example.geofencing.ui.sector.SectorPage
import com.example.geofencing.ui.sector.sampleSectorState
import com.example.geofencing.ui.theme.GeofencingTheme
import com.example.geofencing.ui.theme.extendedColors
import com.example.geofencing.ui.wholesector.SampleSectorNames
import com.example.geofencing.ui.wholesector.WholeSectorPage
import com.example.geofencing.ui.wholesector.WholeSectorUiState
import com.example.geofencing.ui.wholesector.sampleWholeSectorState

private const val APP_TITLE = "Geofence"

// 탭 셸: AppTopBar + SectorTabRow(크롬) + body. 크롬은 항상 유지되고, body는:
// - 드릴다운으로 열린 카트가 있으면 CartPage (뒤로가기로 닫음)
// - 없으면 선택 탭의 페이지(Whole Sector / Sector)
// 탭 선택/열린 카트 상태를 여기서 소유. 데이터는 밖(추후 ViewModel)에서 주입, 지금은 샘플.
@Composable
fun HomeScreen(
    sectorNames: List<String>,
    wholeSectorState: WholeSectorUiState,
    modifier: Modifier = Modifier,
    onBellClick: () -> Unit = {}
) {
    // 0 = Whole Sector, 1.. = sectorNames
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    // 드릴다운으로 열린 카트(없으면 탭 페이지). TODO: 회전 보존/정식 네비게이션.
    var openCart by remember { mutableStateOf<CartUiState?>(null) }

    // 카트가 열려 있으면 하드웨어 백으로 카트를 닫는다(앱 종료 대신).
    BackHandler(enabled = openCart != null) { openCart = null }

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
                    openCart = null
                }
            )
        }

        // body — 좌우 여백은 각 페이지가 담당(지도 full-bleed 허용).
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val cart = openCart
            if (cart != null) {
                CartPage(state = cart, onBack = { openCart = null })
            } else {
                when (selectedIndex) {
                    0 -> WholeSectorPage(
                        state = wholeSectorState,
                        // Violation/Disconnect 카트 클릭 → 해당 섹터의 해당 카트로 드릴다운
                        onViolationClick = { openCart = cartStateFor(it.sector, it.cart) },
                        onDisconnectClick = { openCart = cartStateFor(it.sector, it.cart) },
                        // 섹터 카드 클릭 → 해당 섹터 탭으로 전환
                        onSectorClick = { s ->
                            val idx = sectorNames.indexOf(s.name)
                            if (idx >= 0) selectedIndex = idx + 1
                        }
                    )
                    else -> {
                        // 선택 탭의 섹터를 단일 mock 소스에서 조회해 페이지를 구성(지도/목록/상세가 일치).
                        val sectorName = sectorNames.getOrNull(selectedIndex - 1).orEmpty()
                        val sectorState = mockSectorByName(sectorName)?.let { sampleSectorState(it) }
                            ?: sampleSectorState()
                        SectorPage(
                            state = sectorState,
                            // 카트 클릭(violation/disconnect/all cart) → 현재 섹터의 해당 카트로 드릴다운
                            onViolationClick = { openCart = cartStateFor(sectorName, it.cart) },
                            onDisconnectClick = { openCart = cartStateFor(sectorName, it.cart) },
                            onCartClick = { openCart = cartStateFor(sectorName, it.cart) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "HomeScreen", showBackground = true, backgroundColor = 0xFF0F0F0F, heightDp = 1500)
@Composable
private fun HomeScreenPreview() {
    GeofencingTheme {
        HomeScreen(
            sectorNames = SampleSectorNames,
            wholeSectorState = sampleWholeSectorState()
        )
    }
}
