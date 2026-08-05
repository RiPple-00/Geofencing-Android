package com.example.geofencing.ui.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.geofencing.R
import com.example.geofencing.ui.components.BackButton
import com.example.geofencing.ui.components.MapExpandButton
import com.example.geofencing.ui.components.SectionDivider
import com.example.geofencing.ui.components.StatusBadge
import com.example.geofencing.ui.components.StatusKind
import com.example.geofencing.ui.components.borderBox
import com.example.geofencing.ui.components.color
import com.example.geofencing.ui.theme.Body13
import com.example.geofencing.ui.theme.Body14
import com.example.geofencing.ui.theme.GeofencingTheme
import com.example.geofencing.ui.theme.Header20
import com.example.geofencing.ui.theme.Label14
import com.example.geofencing.ui.theme.Label18
import com.example.geofencing.ui.theme.PageHorizontalMargin
import com.example.geofencing.ui.theme.Title16
import com.example.geofencing.ui.theme.extendedColors
import kotlinx.coroutines.delay

// 위반 카트 전용 상세(위반일 때만 non-null).
data class ViolationDetail(
    val duration: String,   // "8m 45s"
    val maxSpeed: String,   // "16 Km/h"
    val atTime: String,     // "2026.07.20 15:02:30"
    val atAddress: String
)

// 화면 표시용 UI state. 지오펜스 상태에 따라 렌더가 갈린다:
// - Disconnect: 라이브 데이터 없음 → 새로고침 카운트다운 없음, Operation도 Disconnect 배지.
// - Violation: violation 상세 카드 + Violation at Time/Address 표시.
// - 주소 색 = 지오펜스 상태 색(Compliance=brand / Violation=critical / Disconnect=border-strong).
data class CartUiState(
    val sectorName: String,
    val cartName: String,
    val registeredId: String,
    val operationStatus: String,
    val geofenceStatus: StatusKind,
    val timestamp: String,
    val address: String,
    val violation: ViolationDetail? = null
)

// TODO(측정): 페이지 레벨 실측값. 지금은 임시 추정치.
private val CartMapHeight = 273.dp
private val MapToTitleGap = 32.dp
private val TitleToIdGap = 14.dp
private val SectionGap = 32.dp
private val StatusToRefreshGap = 52.dp
private val RefreshToCardGap = 15.dp
private val StatusRowGap = 4.dp
private val ViolationRowGap = 20.dp
private val StatusLabelWidth = 175.dp
// Operation/Geofence Status, Violation at Time/Address 행의 라벨·값 셀 공통 패딩(상하좌우 8).
private val InfoCellPadding = 8.dp
private val CardHorizontalPadding = 12.dp
private val CardVerticalPadding = 12.5.dp
// Violation Duration / Max Speed 카드의 라벨 글자 자체에 주는 상하 패딩(두 카드 공통).
private val ViolationDetailLabelPadding = 6.dp
private val CardGap = 12.dp
private val PageBottomGap = 80.dp
private val BreadcrumbArrowPadding = 4.dp

// Cart 상세 페이지. 크롬(탭바)은 HomeScreen이 담당. state는 밖에서 주입.
@Composable
fun CartPage(
    state: CartUiState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onExpandMap: () -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    val colors = MaterialTheme.extendedColors
    val disconnected = state.geofenceStatus == StatusKind.Disconnect
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = PageBottomGap)
    ) {
        // breadcrumb — 좌우 여백 없이 가장자리까지.
        Breadcrumb(
            sectorName = state.sectorName,
            cartName = state.cartName,
            onBack = onBack
        )

        // 지도 배너 full-bleed. TODO: 라이브 지도(선택 카트 중심)로 교체.
        CartMapBanner(onExpandMap = onExpandMap)

        Column(modifier = Modifier.padding(horizontal = PageHorizontalMargin)) {
            Spacer(modifier = Modifier.height(MapToTitleGap))

            Text(
                text = state.cartName,
                style = Header20,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(TitleToIdGap))
            Text(
                text = "Registered Cart ID: ${state.registeredId}",
                style = Body14,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(SectionGap))

            // Operation Status: 연결끊김이면 Disconnect 배지, 아니면 텍스트(Driving 등).
            StatusInfoRow(label = "Operation Status") {
                if (disconnected) {
                    StatusBadge(kind = StatusKind.Disconnect)
                } else {
                    Text(text = state.operationStatus, style = Body14, color = colors.textPrimary)
                }
            }
            Spacer(modifier = Modifier.height(StatusRowGap))
            StatusInfoRow(label = "Geofence Status") {
                StatusBadge(kind = state.geofenceStatus)
            }

            Spacer(modifier = Modifier.height(StatusToRefreshGap))

            // 연결끊김이면 라이브 갱신이 없어 카운트다운/버튼 숨김(타임스탬프만).
            CartRefreshRow(
                timestamp = state.timestamp,
                onRefresh = onRefresh,
                showRefresh = !disconnected
            )

            Spacer(modifier = Modifier.height(RefreshToCardGap))

            CurrentLocationCard(address = state.address, addressColor = state.geofenceStatus.color)

            // Violation 전용 상세.
            state.violation?.let { v ->
                Spacer(modifier = Modifier.height(CardGap))
                ViolationMetricCard(label = "Violation Duration", value = v.duration, showClock = true)
                Spacer(modifier = Modifier.height(CardGap))
                ViolationMetricCard(label = "Max Speed Post-Violation", value = v.maxSpeed, showClock = false)

                SectionDivider(top = 50.dp, bottom = 46.dp)

                StatusInfoRow(label = "Violation at Time") {
                    Text(text = v.atTime, style = Body14, color = colors.textPrimary)
                }
                Spacer(modifier = Modifier.height(ViolationRowGap))
                StatusInfoRow(label = "Violation at Address") {
                    Text(text = v.atAddress, style = Body14, color = colors.textPrimary)
                }
            }
        }
    }
}

// < [back] Sector #1 > Cart #1
@Composable
private fun Breadcrumb(
    sectorName: String,
    cartName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.extendedColors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BackButton(onClick = onBack)
        Text(text = sectorName, style = Label14, color = colors.textDisabled)
        Image(
            painter = painterResource(R.drawable.ic_arrow_right_mini),
            contentDescription = null,
            modifier = Modifier
                .padding(horizontal = BreadcrumbArrowPadding)
                .size(16.dp)
        )
        Text(text = cartName, style = Label14, color = colors.textPrimary)
    }
}

// 지도 배너 + 확대 버튼. TODO: 라이브 지도(GeofenceMap, 선택 카트 중심)로 교체.
@Composable
private fun CartMapBanner(onExpandMap: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(CartMapHeight)
            .background(MaterialTheme.extendedColors.fillPrimary)
    ) {
        MapExpandButton(
            onClick = onExpandMap,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        )
    }
}

// 라벨 + 값(임의 컴포저블). Operation=텍스트/배지, Geofence=배지, Violation at=텍스트.
@Composable
private fun StatusInfoRow(
    label: String,
    modifier: Modifier = Modifier,
    value: @Composable () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier
                .width(StatusLabelWidth)
                .padding(InfoCellPadding),
            style = Body14,
            color = MaterialTheme.extendedColors.textSecondary
        )
        Box(modifier = Modifier.padding(InfoCellPadding)) {
            value()
        }
    }
}

// 타임스탬프 + (라이브면) 마지막 새로고침 이후 경과 시간(mm:ss) + 새로고침 버튼. Disconnect면 타임스탬프만.
@Composable
private fun CartRefreshRow(
    timestamp: String,
    onRefresh: () -> Unit,
    showRefresh: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = timestamp,
            style = Body13,
            color = MaterialTheme.extendedColors.textSecondary
        )
        if (showRefresh) {
            Spacer(modifier = Modifier.weight(1f))
            RefreshElapsed(onRefresh = onRefresh)
        }
    }
}

// 첫 로딩/마지막 새로고침 시각을 기록하고, (현재 - 기록) 경과 시간을 매초 mm:ss로 표시한다.
// 단순 카운터가 아니라 실제 시각 비교라 recompose/일시정지에도 어긋나지 않음.
// 아이콘 클릭 시 onRefresh 호출 + 기록 시각을 현재로 리셋(경과 0).
@Composable
private fun RefreshElapsed(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.extendedColors
    var lastRefreshMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var elapsedSec by remember { mutableIntStateOf(0) }
    LaunchedEffect(lastRefreshMillis) {
        while (true) {
            elapsedSec = ((System.currentTimeMillis() - lastRefreshMillis) / 1000L)
                .coerceAtLeast(0L)
                .toInt()
            delay(1000L)
        }
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = formatElapsed(elapsedSec), style = Body13, color = colors.textSecondary)
        Image(
            painter = painterResource(R.drawable.ic_refresh),
            contentDescription = "새로고침",
            colorFilter = ColorFilter.tint(colors.textSecondary),
            modifier = Modifier
                .size(16.dp)
                .clickable {
                    onRefresh()
                    lastRefreshMillis = System.currentTimeMillis()
                    elapsedSec = 0
                }
        )
    }
}

// 경과 초 → "1s" / "11s" / "1m 1s" / "11m 11s" (1분 미만이면 초만).
private fun formatElapsed(totalSec: Int): String {
    val m = totalSec / 60
    val s = totalSec % 60
    return if (m > 0) "${m}m ${s}s" else "${s}s"
}

// Current Location 카드. 주소 색은 지오펜스 상태 색.
@Composable
private fun CurrentLocationCard(
    address: String,
    addressColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .borderBox()
            .padding(horizontal = CardHorizontalPadding, vertical = CardVerticalPadding)
    ) {
        Text(
            text = "Current Location",
            style = Title16,
            color = MaterialTheme.extendedColors.textSecondary
        )
        // 주소: 블록은 중앙정렬, 텍스트 자체는 왼쪽정렬, 첫 "," 뒤 줄바꿈. 상하 패딩 42.5/39.5.
        Text(
            text = breakAtFirstComma(address),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 42.5.dp, bottom = 39.5.dp),
            style = Label18,
            color = addressColor,
            textAlign = TextAlign.Start
        )
    }
}

// 주소에서 첫 "," 뒤에 줄바꿈. "1776 Terminal Dr, Richland, WA 99354" → "1776 Terminal Dr,⏎Richland, WA 99354".
private fun breakAtFirstComma(address: String): String {
    val idx = address.indexOf(',')
    return if (idx >= 0) {
        address.substring(0, idx + 1) + "\n" + address.substring(idx + 1).trimStart()
    } else {
        address
    }
}

// 위반 지표 카드: [라벨] ...... [(시계) 값(빨강)].
@Composable
private fun ViolationMetricCard(
    label: String,
    value: String,
    showClock: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.extendedColors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .borderBox()
            .padding(horizontal = CardHorizontalPadding, vertical = CardVerticalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(vertical = ViolationDetailLabelPadding),
            style = Title16,
            color = colors.textPrimary
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showClock) {
                Image(
                    painter = painterResource(R.drawable.ic_clock),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colors.criticalPrimary),
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(text = value, style = Label18, color = colors.criticalPrimary)
        }
    }
}

@Preview(name = "Cart - Compliance", showBackground = true, backgroundColor = 0xFF0F0F0F, heightDp = 1000)
@Composable
private fun CartPageCompliancePreview() {
    GeofencingTheme { CartPage(state = sampleCartComplianceState()) }
}

@Preview(name = "Cart - Violation", showBackground = true, backgroundColor = 0xFF0F0F0F, heightDp = 1300)
@Composable
private fun CartPageViolationPreview() {
    GeofencingTheme { CartPage(state = sampleCartViolationState()) }
}

@Preview(name = "Cart - Disconnect", showBackground = true, backgroundColor = 0xFF0F0F0F, heightDp = 1000)
@Composable
private fun CartPageDisconnectPreview() {
    GeofencingTheme { CartPage(state = sampleCartDisconnectState()) }
}
