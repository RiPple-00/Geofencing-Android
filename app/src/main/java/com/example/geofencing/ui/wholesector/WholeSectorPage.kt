package com.example.geofencing.ui.wholesector

import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
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
import com.example.geofencing.ui.map.FixedGeofenceMap
import com.example.geofencing.ui.map.GeofenceFitMaxHeight
import com.example.geofencing.ui.map.GeofenceFitMaxWidth
import com.example.geofencing.ui.map.GeofenceMapContent
import com.example.geofencing.ui.map.SectorSnapshotPrefetcher
import com.example.geofencing.ui.map.SectorSnapshotRequest
import com.example.geofencing.ui.theme.Body13
import com.example.geofencing.ui.theme.Label18
import com.example.geofencing.ui.theme.GeofencingTheme
import com.example.geofencing.ui.theme.Header30
import com.example.geofencing.ui.theme.PageHorizontalMargin
import com.example.geofencing.ui.theme.extendedColors
import com.google.android.gms.maps.model.LatLng

// 화면 표시용 UI state. 지금은 프리뷰/임시 데이터, 나중에 ViewModel(API)이 생성.
data class ViolationEntry(val sector: String, val cart: String, val remaining: String)
data class DisconnectEntry(val sector: String, val cart: String, val elapsed: String)
// geofence: 섹터 지도 스냅샷을 그릴 경계 좌표(스냅샷 캐시 키에도 사용). 실데이터 전엔 비어있을 수 있음.
data class SectorSummary(
    val id: Int,
    val name: String,
    val wholeCart: Int,
    val violation: Int,
    val disconnect: Int,
    val geofence: List<LatLng> = emptyList()
)

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
private val SectorCardGap = 24.dp
// 섹터 카드: 지도 배경 전체 높이 + 콘텐츠(제목/통계) 내부 패딩(상단 26 / 하좌우 14).
private val SectorCardHeight = 250.dp // TODO(측정): 카드 전체 높이 튜닝.
private val SectorCardContentPaddingTop = 26.dp
private val SectorCardContentPadding = 14.dp
// 카드 썸네일의 geofence 최대 박스(208×110, 제목/통계 글자와 ~15dp 간격용).
// SectorPage 배너는 별도 fit(SectorMapGeofenceFitHeight)을 써서 서로 독립적으로 조정된다.

// Whole Sector 페이지 본문(헤더/탭바 아래). 크롬은 HomeScreen이 담당. state는 밖에서 주입.
@Composable
fun WholeSectorPage(
    state: WholeSectorUiState,
    modifier: Modifier = Modifier,
    onViolationClick: (ViolationEntry) -> Unit = {},
    onDisconnectClick: (DisconnectEntry) -> Unit = {},
    onSectorClick: (SectorSummary) -> Unit = {}
) {
    Box(modifier = modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
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

        // 섹터 썸네일을 미리 생성(오프스크린). 스크롤 밖 형제라 레이아웃/스크롤엔 영향 없음.
        // 캡처 크기 = 카드 안쪽 지도 폭(화면폭 - 페이지 여백 - 카드 패딩) × 썸네일 높이.
        val containerWidthDp = with(LocalDensity.current) {
            LocalWindowInfo.current.containerSize.width.toDp()
        }
        SectorSnapshotPrefetcher(
            requests = state.sectors.map { SectorSnapshotRequest(it.id, it.geofence) },
            width = containerWidthDp - PageHorizontalMargin * 2,
            height = SectorCardHeight,
            fitWidth = GeofenceFitMaxWidth,
            fitHeight = GeofenceFitMaxHeight
        )
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

// 섹터 카드: 지도 스냅샷을 borderBox 전체 배경으로 깔고, 그 위에 제목(상단) + 통계(하단).
// Whole Sector 전용(page-private). 스냅샷은 캐시(카트 없이 geofence만), prefetcher가 채움.
@Composable
private fun SectorMapCard(
    sector: SectorSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(SectorCardHeight)
            .borderBox()
            .noRippleClickable(onClick)
    ) {
        // 배경: 지도 스냅샷을 카드(borderBox 안쪽) 전체에 꽉 채움.
        FixedGeofenceMap(
            content = GeofenceMapContent(geofence = sector.geofence),
            sectorId = sector.id,
            modifier = Modifier.matchParentSize()
        )
        // 지도 위 콘텐츠: 제목(상단) + 통계(하단). BorderBox 내부 패딩 상단 26 / 하좌우 14.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = SectorCardContentPadding,
                    end = SectorCardContentPadding,
                    top = SectorCardContentPaddingTop,
                    bottom = SectorCardContentPadding
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = sector.name,
                    style = Label18,
                    color = MaterialTheme.extendedColors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Image(
                    painter = painterResource(R.drawable.ic_arrow_right_20),
                    contentDescription = null
                )
            }
            SectorCardStatRow(
                wholeCart = sector.wholeCart,
                violation = sector.violation,
                disconnect = sector.disconnect
            )
        }
    }
}

@Preview(name = "WholeSectorPage", showBackground = true, backgroundColor = 0xFF0F0F0F, heightDp = 1400)
@Composable
private fun WholeSectorPagePreview() {
    GeofencingTheme {
        WholeSectorPage(state = sampleWholeSectorState())
    }
}
