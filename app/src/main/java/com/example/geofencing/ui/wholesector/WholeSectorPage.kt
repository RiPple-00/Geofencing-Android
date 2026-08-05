package com.example.geofencing.ui.wholesector

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import com.example.geofencing.ui.components.DisconnectRow
import com.example.geofencing.ui.components.ListSection
import com.example.geofencing.ui.components.SectionDivider
import com.example.geofencing.ui.components.SectorCardStatRow
import com.example.geofencing.ui.components.ViolationRow
import com.example.geofencing.ui.components.borderBox
import com.example.geofencing.ui.components.noRippleClickable
import com.example.geofencing.ui.theme.Body13
import com.example.geofencing.ui.theme.CardShape
import com.example.geofencing.ui.theme.GeofencingTheme
import com.example.geofencing.ui.theme.Header20
import com.example.geofencing.ui.theme.Header30
import com.example.geofencing.ui.theme.PageHorizontalMargin
import com.example.geofencing.ui.theme.extendedColors

// 화면 표시용 UI state. 지금은 프리뷰/임시 데이터, 나중에 ViewModel(API)이 생성.
data class ViolationEntry(val sector: String, val cart: String, val remaining: String)
data class DisconnectEntry(val sector: String, val cart: String, val elapsed: String)
data class SectorSummary(val name: String, val wholeCart: Int, val violation: Int, val disconnect: Int)

data class WholeSectorUiState(
    val totalCarts: Int,
    val compliance: Int,
    val violation: Int,
    val disconnect: Int,
    val violations: List<ViolationEntry>,
    val disconnects: List<DisconnectEntry>,
    val sectors: List<SectorSummary>
)

private val PageTopGap = 26.dp
private val PageBottomGap = 92.dp
private val TotalToStatGap = 24.dp
private val SectorCardGap = 12.dp
private val SectorCardPadding = 12.dp
private val SectorCardInnerGap = 12.dp
private val SectorThumbnailHeight = 160.dp

// Whole Sector 페이지 본문(헤더/탭바 아래). 크롬은 HomeScreen이 담당. state는 밖에서 주입.
@Composable
fun WholeSectorPage(
    state: WholeSectorUiState,
    modifier: Modifier = Modifier,
    onViolationClick: (ViolationEntry) -> Unit = {},
    onDisconnectClick: (DisconnectEntry) -> Unit = {},
    onSectorClick: (SectorSummary) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PageHorizontalMargin)
            .padding(top = PageTopGap, bottom = PageBottomGap)
    ) {
        TotalCartsSummary(state)

        SectionDivider(top = 50.dp, bottom = 52.dp)

        ListSection(title = "Violation", count = state.violations.size, unit = "Carts") {
            state.violations.forEachIndexed { i, v ->
                ViolationRow(
                    sector = v.sector,
                    cart = v.cart,
                    remaining = v.remaining,
                    onClick = { onViolationClick(v) },
                    showDivider = i < state.violations.lastIndex
                )
            }
        }

        SectionDivider(top = 58.dp, bottom = 46.5.dp)

        ListSection(title = "Disconnect", count = state.disconnects.size, unit = "Carts") {
            state.disconnects.forEachIndexed { i, d ->
                DisconnectRow(
                    sector = d.sector,
                    cart = d.cart,
                    elapsed = d.elapsed,
                    onClick = { onDisconnectClick(d) },
                    showDivider = i < state.disconnects.lastIndex
                )
            }
        }

        // TODO(측정): 구분선 상/하단 간격(구분선마다 다름).
        SectionDivider(top = 64.dp, bottom = 46.dp)

        ListSection(title = "Sector List", count = state.sectors.size, unit = "Sectors") {
            state.sectors.forEachIndexed { i, s ->
                SectorMapCard(sector = s, onClick = { onSectorClick(s) })
                if (i < state.sectors.lastIndex) {
                    Spacer(modifier = Modifier.height(SectorCardGap))
                }
            }
        }
    }
}

// 상단 통계: "67 Total Carts" + Compliance/Violation/Disconnect(Header 스타일).
@Composable
private fun TotalCartsSummary(state: WholeSectorUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TotalToStatGap)
    ) {
        Row(
            // "67" 헤더를 stats(CartStatRow Header 좌우 8dp)와 왼쪽 정렬
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = state.totalCarts.toString(),
                style = Header30,
                color = MaterialTheme.extendedColors.textPrimary
            )
            Text(
                text = "Total Carts",
                // 폰트 메트릭상 위로 떠 보여서 광학적 중앙 보정으로 살짝 내림.
                modifier = Modifier.offset(y = 1.dp),
                style = Body13,
                color = MaterialTheme.extendedColors.borderStrong
            )
        }
        CartStatRow(
            stats = listOf(
                CartStat(CartStatRole.Compliance, state.compliance),
                CartStat(CartStatRole.Violation, state.violation),
                CartStat(CartStatRole.Disconnect, state.disconnect)
            ),
            style = CartStatStyle.Header
        )
    }
}

// 섹터 카드: 제목 + 화살표 + 지도 썸네일 + 통계. Whole Sector 전용(page-private).
@Composable
private fun SectorMapCard(
    sector: SectorSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .borderBox()
            .noRippleClickable(onClick)
            .padding(SectorCardPadding)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // TODO(측정): 카드 제목 스타일. 지금은 Header20으로 추정.
            Text(
                text = sector.name,
                style = Header20,
                color = MaterialTheme.extendedColors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            Image(
                painter = painterResource(R.drawable.ic_arrow_right_20),
                contentDescription = null
            )
        }
        Spacer(modifier = Modifier.height(SectorCardInnerGap))
        SectorMapThumbnail(modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(SectorCardInnerGap))
        SectorCardStatRow(
            wholeCart = sector.wholeCart,
            violation = sector.violation,
            disconnect = sector.disconnect
        )
    }
}

// TODO: 실제 정적 지도 스냅샷으로 교체(지도 컴포넌트 완성 후). 지금은 자리표시 박스.
@Composable
private fun SectorMapThumbnail(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(SectorThumbnailHeight)
            .clip(CardShape)
            .background(MaterialTheme.extendedColors.fillPrimary)
    )
}

@Preview(name = "WholeSectorPage", showBackground = true, backgroundColor = 0xFF0F0F0F, heightDp = 1400)
@Composable
private fun WholeSectorPagePreview() {
    GeofencingTheme {
        WholeSectorPage(state = sampleWholeSectorState())
    }
}
