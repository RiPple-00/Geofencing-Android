package com.example.geofencing.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.geofencing.ui.theme.extendedColors

// StatusListRow(코어)의 의미 래퍼들. 컨텍스트별 폭/status는 여기서.
//
// Violation/Disconnect는 두 형태
//   - (sector, cart, ...) : Whole Sector 페이지처럼 섹터+카트를 함께 보여줄 때
//   - (cart, ...)         : 섹터가 이미 정해진 화면에서 카트만 보여줄 때(data2=null)

// 폭(360 기준 고정, 시간 칸은 weight로 stretch → 반응형).
// Violation = cart 78 / sector 100, Disconnect = cart 88 / sector 105, All Cart List = 85/90.
private val ViolationData1Width = 78.dp    // cart
private val ViolationData2Width = 100.dp   // sector
private val DisconnectData1Width = 88.dp   // cart
private val DisconnectData2Width = 105.dp  // sector
private val CartStateData1Width = 85.dp
private val CartStateData2Width = 90.dp

// Violation 섹션 행(카트+섹터): 우측에 시계 아이콘 + 빨강 경고 시간.
// 카트가 메인이므로 카트를 data1(왼쪽·강조), 섹터를 data2(보조)로 둔다.
@Composable
fun ViolationRow(
    sector: String,
    cart: String,
    remaining: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true
) = StatusListRow(
    data1 = cart,
    data2 = sector,
    status = RowStatus.AlertTime(remaining),
    onClick = onClick,
    modifier = modifier,
    style = StatusRowStyle(
        data1Width = ViolationData1Width,
        data2Width = ViolationData2Width,
        showDivider = showDivider
    )
)

// Violation 섹션 행(카트 단독): 섹터가 이미 정해진 화면용. data2 자리는 비워둔다.
@Composable
fun ViolationRow(
    cart: String,
    remaining: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true
) = StatusListRow(
    data1 = cart,
    data2 = null,
    status = RowStatus.AlertTime(remaining),
    onClick = onClick,
    modifier = modifier,
    style = StatusRowStyle(
        data1Width = ViolationData1Width,
        data2Width = ViolationData2Width,
        showDivider = showDivider
    )
)

// Disconnect 섹션 행(카트+섹터): 우측에 회색 경과 시간.
// 카트가 메인이므로 카트를 data1(왼쪽·강조), 섹터를 data2(보조)로 둔다.
@Composable
fun DisconnectRow(
    sector: String,
    cart: String,
    elapsed: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true
) = StatusListRow(
    data1 = cart,
    data2 = sector,
    status = RowStatus.Elapsed(elapsed),
    onClick = onClick,
    modifier = modifier,
    style = StatusRowStyle(
        data1Width = DisconnectData1Width,
        data2Width = DisconnectData2Width,
        showDivider = showDivider
    )
)

// Disconnect 섹션 행(카트 단독): 섹터가 이미 정해진 화면용. data2 자리는 비워둔다.
@Composable
fun DisconnectRow(
    cart: String,
    elapsed: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true
) = StatusListRow(
    data1 = cart,
    data2 = null,
    status = RowStatus.Elapsed(elapsed),
    onClick = onClick,
    modifier = modifier,
    style = StatusRowStyle(
        data1Width = DisconnectData1Width,
        data2Width = DisconnectData2Width,
        showDivider = showDivider
    )
)

// All Cart List 행: 카트 + 주행상태 + 상태 배지(Compliance / Violation / Disconnect).
// 주행상태는 의미 있는 값이라 data2를 text/secondary로 밝게 유지(기본 border/strong 대신).
@Composable
fun CartStateRow(
    cart: String,
    drivingState: String,
    kind: StatusKind,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true
) = StatusListRow(
    data1 = cart,
    data2 = drivingState,
    status = RowStatus.Badge(kind),
    onClick = onClick,
    modifier = modifier,
    style = StatusRowStyle(
        data1Width = CartStateData1Width,
        data2Width = CartStateData2Width,
        data2Color = MaterialTheme.extendedColors.textSecondary,
        showDivider = showDivider
    )
)
