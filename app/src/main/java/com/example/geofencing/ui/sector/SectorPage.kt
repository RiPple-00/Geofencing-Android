package com.example.geofencing.ui.sector

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.geofencing.R
import com.example.geofencing.ui.components.CartStat
import com.example.geofencing.ui.components.CartStatRole
import com.example.geofencing.ui.components.CartStatRow
import com.example.geofencing.ui.components.CartStatStyle
import com.example.geofencing.ui.components.CartStateRow
import com.example.geofencing.ui.components.DisconnectRow
import com.example.geofencing.ui.components.ListSection
import com.example.geofencing.ui.components.SectionDivider
import com.example.geofencing.ui.components.StatusKind
import com.example.geofencing.ui.components.ViolationRow
import com.example.geofencing.ui.components.noRippleClickable
import com.example.geofencing.ui.theme.Body14
import com.example.geofencing.ui.theme.GeofencingTheme
import com.example.geofencing.ui.theme.Header20
import com.example.geofencing.ui.theme.Label14
import com.example.geofencing.ui.theme.PageHorizontalMargin
import com.example.geofencing.ui.theme.RoundedMd
import com.example.geofencing.ui.theme.extendedColors

// 화면 표시용 UI state. 지금은 프리뷰/임시 데이터, 나중에 ViewModel(API)이 생성.
// Violation/Disconnect는 섹터가 정해져 있어 카트 단독(섹터 컬럼 없음).
data class SectorViolationEntry(val cart: String, val remaining: String)
data class SectorDisconnectEntry(val cart: String, val elapsed: String)
data class CartStateEntry(val cart: String, val drivingState: String, val kind: StatusKind)

data class SectorUiState(
    val name: String,
    val address: String,
    val wholeCarts: Int,
    val violation: Int,
    val disconnect: Int,
    val violations: List<SectorViolationEntry>,
    val disconnects: List<SectorDisconnectEntry>,
    val allCarts: List<CartStateEntry>,
    val currentPage: Int,
    val totalPages: Int
)

// TODO(측정): 페이지 레벨 실측값. 지금은 임시 추정치.
private val SectorMapHeight = 240.dp
private val MapToTitleGap = 42.dp
private val TitleToAddressGap = 14.dp
private val AddressToStatGap = 42.dp
private val PageBottomGap = 86.dp
private val PaginationGap = 20.dp

// Sector 페이지 본문(헤더/탭바 아래). 크롬은 HomeScreen이 담당. state는 밖에서 주입.
@Composable
fun SectorPage(
    state: SectorUiState,
    modifier: Modifier = Modifier,
    onHeatmapClick: () -> Unit = {},
    onViolationClick: (SectorViolationEntry) -> Unit = {},
    onDisconnectClick: (SectorDisconnectEntry) -> Unit = {},
    onCartClick: (CartStateEntry) -> Unit = {},
    onPageSelect: (Int) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = PageBottomGap)
    ) {
        // 지도 배너는 full-bleed(좌우 여백 없이 가장자리까지).
        SectorMapBanner(onHeatmapClick = onHeatmapClick)

        // 나머지 콘텐츠는 좌우 공통 여백 적용.
        Column(modifier = Modifier.padding(horizontal = PageHorizontalMargin)) {
            Spacer(modifier = Modifier.height(MapToTitleGap))

            // 제목 + 주소
            Text(
                text = state.name,
                style = Header20,
                color = MaterialTheme.extendedColors.textPrimary
            )
            Spacer(modifier = Modifier.height(TitleToAddressGap))
            Text(
                text = state.address,
                style = Body14,
                color = MaterialTheme.extendedColors.textSecondary
            )

            Spacer(modifier = Modifier.height(AddressToStatGap))

            CartStatRow(
                stats = listOf(
                    CartStat(CartStatRole.WholeCarts, state.wholeCarts),
                    CartStat(CartStatRole.Violation, state.violation),
                    CartStat(CartStatRole.Disconnect, state.disconnect)
                ),
                style = CartStatStyle.Header
            )

            SectionDivider(top = 50.dp, bottom = 46.dp)

            ListSection(title = "Violation", count = state.violations.size, unit = "Carts") {
                state.violations.forEachIndexed { i, v ->
                    ViolationRow(
                        cart = v.cart,
                        remaining = v.remaining,
                        onClick = { onViolationClick(v) },
                        showDivider = i < state.violations.lastIndex
                    )
                }
            }

            SectionDivider(top = 56.dp, bottom = 46.dp)

            ListSection(title = "Disconnect", count = state.disconnects.size, unit = "Carts") {
                state.disconnects.forEachIndexed { i, d ->
                    DisconnectRow(
                        cart = d.cart,
                        elapsed = d.elapsed,
                        onClick = { onDisconnectClick(d) },
                        showDivider = i < state.disconnects.lastIndex
                    )
                }
            }

            SectionDivider(top = 56.dp, bottom = 46.dp)

            ListSection(title = "All Cart List", count = state.wholeCarts, unit = "Carts") {
                state.allCarts.forEachIndexed { i, c ->
                    CartStateRow(
                        cart = c.cart,
                        drivingState = c.drivingState,
                        kind = c.kind,
                        onClick = { onCartClick(c) },
                        showDivider = i < state.allCarts.lastIndex
                    )
                }
            }

            Spacer(modifier = Modifier.height(PaginationGap))
            Pagination(
                currentPage = state.currentPage,
                totalPages = state.totalPages,
                onPageSelect = onPageSelect,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// 지도 배너 + "Violation Heatmap >" 진입. TODO: 라이브 지도(GeofenceMap)로 교체 + full-bleed.
@Composable
private fun SectorMapBanner(onHeatmapClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(SectorMapHeight)
            .background(MaterialTheme.extendedColors.fillPrimary),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .noRippleClickable(onHeatmapClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Violation Heatmap",
                style = Label14,
                color = MaterialTheme.extendedColors.textPrimary
            )
            Image(
                painter = painterResource(R.drawable.ic_arrow_right_16),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// 페이지네이션 컨테이너 padding 10 / 아이템 간격 10 / 아이템 40×40 고정 / 선택 테두리 1.5dp.
private val PaginationPadding = 10.dp
private val PaginationItemGap = 10.dp
private val PaginationItemSize = 40.dp
private val PaginationSelectedBorder = 1.5.dp

// All Cart List 페이지네이션. 선택 아이템만 6dp 둥근 테두리(border-default)+fill-primary 배경. 숫자는 전부 text-primary.
@Composable
private fun Pagination(
    currentPage: Int,
    totalPages: Int,
    onPageSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.extendedColors
    val selectedShape = RoundedCornerShape(RoundedMd)
    Row(
        modifier = modifier.padding(PaginationPadding),
        horizontalArrangement = Arrangement.spacedBy(PaginationItemGap, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..totalPages).forEach { page ->
            val selected = page == currentPage
            Box(
                modifier = Modifier
                    .size(PaginationItemSize)
                    .then(
                        if (selected) {
                            Modifier
                                .clip(selectedShape)
                                .background(colors.fillPrimary)
                                .border(PaginationSelectedBorder, colors.borderDefault, selectedShape)
                        } else {
                            Modifier
                        }
                    )
                    .noRippleClickable { onPageSelect(page) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = page.toString(), style = Label14, color = colors.textPrimary)
            }
        }
    }
}

@Preview(name = "SectorPage", showBackground = true, backgroundColor = 0xFF0F0F0F, heightDp = 1600)
@Composable
private fun SectorPagePreview() {
    GeofencingTheme {
        SectorPage(state = sampleSectorState())
    }
}
