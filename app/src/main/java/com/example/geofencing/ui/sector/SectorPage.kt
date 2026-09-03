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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.geofencing.ui.components.MapExpandButton
import com.example.geofencing.ui.components.SectionDivider
import com.example.geofencing.ui.components.ViolationRow
import com.example.geofencing.ui.components.noRippleClickable
import com.example.geofencing.ui.theme.Body14
import com.example.geofencing.ui.theme.GeofencingTheme
import com.example.geofencing.ui.theme.Header20
import com.example.geofencing.ui.theme.Label14
import com.example.geofencing.ui.theme.PageHorizontalMargin
import com.example.geofencing.ui.theme.RoundedMd
import com.example.geofencing.ui.theme.extendedColors

private val SectorMapHeight = 240.dp
private val MapToTitleGap = 42.dp
private val TitleToAddressGap = 14.dp
private val AddressToStatGap = 42.dp
private val PageBottomGap = 86.dp
private val PaginationGap = 20.dp

// Sector 페이지 본문(헤더/탭바 아래). 크롬은 HomeScreen이 담당. state는 밖에서 주입.
// SectorPage 콜백 묶음. 라벨 탭 → 팝업(onHeatmapClick), 확대(⤢) → 줌 지도(onExpandMap),
// 행 클릭(violation/disconnect/cart), 페이지 전환(onPageSelect).
data class SectorPageActions(
    val onHeatmapClick: () -> Unit = {},
    val onExpandMap: () -> Unit = {},
    val onViolationClick: (SectorViolationEntry) -> Unit = {},
    val onDisconnectClick: (SectorDisconnectEntry) -> Unit = {},
    val onCartClick: (CartStateEntry) -> Unit = {},
    val onPageSelect: (Int) -> Unit = {}
)

@Composable
fun SectorPage(
    state: SectorUiState,
    // 지도 배너 슬롯. HomeScreen이 전체화면 히트맵과 공유하는 지도를 주입한다(배너에 표시할 때만).
    bannerMap: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    actions: SectorPageActions = SectorPageActions()
) {
    // 페이지네이션 현재 페이지(로컬). totalPages는 All Cart List 수량에서 자동 계산.
    var currentPage by remember { mutableIntStateOf(1) }
    val totalPages = ((state.wholeCarts + CartsPerPage - 1) / CartsPerPage).coerceAtLeast(1)
    // 데이터 축소 등으로 currentPage가 범위를 벗어나도 안전하게 보정(슬라이스·페이지 표시 공용).
    val effectivePage = currentPage.coerceIn(1, totalPages)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = PageBottomGap)
    ) {
        // 지도 배너는 full-bleed. 지도(bannerMap)는 HomeScreen이 전체화면 히트맵과 공유해 주입한다.
        SectorMapBanner(
            map = bannerMap,
            onLabelClick = actions.onHeatmapClick,
            onExpandClick = actions.onExpandMap
        )

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
                        onClick = { actions.onViolationClick(v) },
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
                        onClick = { actions.onDisconnectClick(d) },
                        showDivider = i < state.disconnects.lastIndex
                    )
                }
            }

            SectionDivider(top = 56.dp, bottom = 46.dp)

            ListSection(title = "All Cart List", count = state.wholeCarts, unit = "Carts") {
                // 현재 페이지에 해당하는 CartsPerPage개만 표시.
                val pageCarts = state.allCarts
                    .drop((effectivePage - 1) * CartsPerPage)
                    .take(CartsPerPage)
                pageCarts.forEachIndexed { i, c ->
                    CartStateRow(
                        cart = c.cart,
                        drivingState = c.drivingState,
                        kind = c.kind,
                        onClick = { actions.onCartClick(c) },
                        showDivider = i < pageCarts.lastIndex
                    )
                }
            }

            Spacer(modifier = Modifier.height(PaginationGap))
            Pagination(
                currentPage = effectivePage,
                totalPages = totalPages,
                onPageSelect = { page ->
                    currentPage = page
                    actions.onPageSelect(page)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// 지도 배너(라이브, geofence 전체가 보이게 고정 프레임) + "Violation Heatmap >" 진입.
@Composable
private fun SectorMapBanner(
    map: @Composable () -> Unit,
    onLabelClick: () -> Unit,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(SectorMapHeight),
        contentAlignment = Alignment.BottomCenter
    ) {
        map()
        // "Violation Heatmap >" 라벨(원본) — 탭 시 팝업.
        Row(
            modifier = Modifier
                .noRippleClickable(onLabelClick)
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
        // 우상단 확대(⤢) 버튼 → body 꽉 채우는 줌 지도.
        MapExpandButton(
            onClick = onExpandClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        )
    }
}

// All Cart List 한 페이지에 표시할 카트 수. totalPages = ceil(wholeCarts / 이 값).
private const val CartsPerPage = 8

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
        paginationItems(currentPage, totalPages).forEach { item ->
            when (item) {
                is PageItem.Number -> {
                    val page = item.page
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
                PageItem.Ellipsis -> Box(
                    modifier = Modifier.size(PaginationItemSize),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "...", style = Label14, color = colors.textDisabled)
                }
            }
        }
    }
}

// 페이지네이션 아이템: 숫자 또는 생략(...). (internal: 단위 테스트에서 접근)
internal sealed interface PageItem {
    data class Number(val page: Int) : PageItem
    data object Ellipsis : PageItem
}

// totalPages<=5면 전부, 초과면 [1 … (현재±1) … 마지막] 형태로 축약. (internal: 단위 테스트 대상)
internal fun paginationItems(current: Int, total: Int): List<PageItem> {
    if (total <= 5) return (1..total).map { PageItem.Number(it) }
    val items = mutableListOf<PageItem>()
    items += PageItem.Number(1)
    val start = maxOf(2, current - 1)
    val end = minOf(total - 1, current + 1)
    if (start > 2) items += PageItem.Ellipsis
    for (p in start..end) items += PageItem.Number(p)
    if (end < total - 1) items += PageItem.Ellipsis
    items += PageItem.Number(total)
    return items
}

@Preview(name = "SectorPage", showBackground = true, backgroundColor = 0xFF0F0F0F, heightDp = 1600)
@Composable
private fun SectorPagePreview() {
    GeofencingTheme {
        SectorPage(state = sampleSectorState(), bannerMap = {})
    }
}
