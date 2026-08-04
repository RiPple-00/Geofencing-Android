package com.example.geofencing.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// StatusListRow(코어)의 의미 래퍼들. 컨텍스트별 폭/status는 여기서.
//
// Violation/Disconnect는 두 형태
//   - (sector, cart, ...) : Whole Sector 페이지처럼 섹터+카트를 함께 보여줄 때
//   - (cart, ...)         : 섹터가 이미 정해진 화면에서 카트만 보여줄 때(data2=null)

// 폭 확정값: Violation 100/78, Disconnect 100/88, All Cart List 85/90.
private val ViolationData1Width = 100.dp
private val ViolationData2Width = 78.dp
private val DisconnectData1Width = 100.dp
private val DisconnectData2Width = 88.dp
private val CartStateData1Width = 85.dp
private val CartStateData2Width = 90.dp

// Violation 섹션 행(섹터+카트): 우측에 시계 아이콘 + 빨강 경고 시간.
@Composable
fun ViolationRow(
    sector: String,
    cart: String,
    remaining: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true
) = StatusListRow(
    data1 = sector,
    data2 = cart,
    status = RowStatus.AlertTime(remaining),
    onClick = onClick,
    modifier = modifier,
    data1Width = ViolationData1Width,
    data2Width = ViolationData2Width,
    showDivider = showDivider
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
    data1Width = ViolationData1Width,
    data2Width = ViolationData2Width,
    showDivider = showDivider
)

// Disconnect 섹션 행(섹터+카트): 우측에 회색 경과 시간.
@Composable
fun DisconnectRow(
    sector: String,
    cart: String,
    elapsed: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true
) = StatusListRow(
    data1 = sector,
    data2 = cart,
    status = RowStatus.Elapsed(elapsed),
    onClick = onClick,
    modifier = modifier,
    data1Width = DisconnectData1Width,
    data2Width = DisconnectData2Width,
    showDivider = showDivider
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
    data1Width = DisconnectData1Width,
    data2Width = DisconnectData2Width,
    showDivider = showDivider
)

// All Cart List 행: 카트 + 주행상태 + 상태 배지(Compliance / Violation / Disconnect).
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
    data1Width = CartStateData1Width,
    data2Width = CartStateData2Width,
    showDivider = showDivider
)
