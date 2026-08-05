package com.example.geofencing.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.geofencing.R
import com.example.geofencing.ui.theme.Body14
import com.example.geofencing.ui.theme.ButtonSpacing
import com.example.geofencing.ui.theme.GeofencingTheme
import com.example.geofencing.ui.theme.Label14
import com.example.geofencing.ui.theme.Space2
import com.example.geofencing.ui.theme.extendedColors

// data1/data2/RowStatus 공통 내부 좌우 패딩. gap 없이 이 패딩만으로 간격을 만든다.
private val ColumnHorizontalPadding = 8.dp

// arrow는 16x16 아이콘 + 상하 Space2(8) / 좌우 ButtonSpacing(12) 패딩.
private val ArrowIconSize = 16.dp

// 아이콘과 글씨 사이 gap (실측값).
private val StatusIconTextGap = 6.dp

// 구분선 상/하 패딩. 아이템과 구분선 사이 별도 gap은 없고 이 패딩이 곧 간격
private val DividerPaddingTop = 8.dp
private val DividerPaddingBottom = 12.dp

sealed interface RowStatus {
    // 시계 아이콘 + 빨강 시간 (예: "1h 19m 23s", "8m 45s")
    data class AlertTime(val text: String) : RowStatus

    // 아이콘 없음 + 회색 경과시간 (예: "1 Days ago", "5 hours ago")
    data class Elapsed(val text: String) : RowStatus

    // 아이콘 + 라벨 배지 (Compliance / Violation / Disconnect)
    data class Badge(val kind: StatusKind) : RowStatus
}

// Whole Sector의 Violation/Disconnect, All Cart List, Sector/Cart 단일 행이 공유하는 리스트 행.
// 골격(gap 없음): [data1(고정폭)] [data2(고정폭)] [RowStatus(남은 공간, 좌측정렬)] [arrow]. 각 컬럼은 내부 좌우 8dp.
// data1/data2 폭은 사용처마다 다르므로 파라미터. 확정값: Violation=100/78, Disconnect=100/88, All Cart List=85/90.
// data2가 null이어도 data2Width는 유지(자리 예약). 표시 조건은 호출부(페이지)가 데이터로 판단.
@Composable
fun StatusListRow(
    data1: String,
    data2: String?,
    status: RowStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    data1Width: Dp = 100.dp,
    data2Width: Dp = 78.dp,
    showDivider: Boolean = true
) {
    val colors = MaterialTheme.extendedColors
    Column(modifier = modifier.fillMaxWidth()) {
        // 행 높이는 고정하지 않고 가장 큰 자식(arrow=아이콘 16 + 상하 8·2 = 32dp)에 가변
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = data1,
                modifier = Modifier
                    .width(data1Width)
                    .padding(horizontal = ColumnHorizontalPadding),
                style = Label14,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // null이어도 폭은 유지해서 여러 행의 status가 세로로 정렬되게 한다.
            Text(
                text = data2.orEmpty(),
                modifier = Modifier
                    .width(data2Width)
                    .padding(horizontal = ColumnHorizontalPadding),
                style = Body14,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // RowStatus는 남은 공간을 채우고 내용은 좌측 정렬(시계 아이콘 등이 왼쪽으로 나란히 정렬됨).
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = ColumnHorizontalPadding),
                contentAlignment = Alignment.CenterStart
            ) {
                StatusContent(status = status)
            }

            Image(
                painter = painterResource(R.drawable.ic_arrow_right_16),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colors.borderStrong),
                modifier = Modifier
                    .padding(horizontal = ButtonSpacing, vertical = Space2)
                    .size(ArrowIconSize)
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(top = DividerPaddingTop, bottom = DividerPaddingBottom),
                thickness = 1.dp,
                color = dividerColorFor(status)
            )
        }
    }
}

// Badge 리스트=border/default, 시간(AlertTime/Elapsed)=border/focus.
@Composable
private fun dividerColorFor(status: RowStatus): Color = when (status) {
    is RowStatus.Badge -> MaterialTheme.extendedColors.borderDefault
    is RowStatus.AlertTime, is RowStatus.Elapsed -> MaterialTheme.extendedColors.borderFocus
}

@Composable
private fun StatusContent(status: RowStatus, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.extendedColors
    when (status) {
        is RowStatus.AlertTime -> Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(StatusIconTextGap),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_clock),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colors.criticalPrimary),
                modifier = Modifier.size(16.dp)
            )
            Text(text = status.text, style = Body14, color = colors.criticalPrimary)
        }

        is RowStatus.Elapsed -> Text(
            modifier = modifier,
            text = status.text,
            style = Body14,
            color = colors.borderStrong
        )

        // 배지 렌더링은 공용 StatusBadge에 위임(Cart 상세와 규칙 공유).
        is RowStatus.Badge -> StatusBadge(kind = status.kind, modifier = modifier)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
private fun StatusListRowPreview() {
    GeofencingTheme {
        Column {
            // Violation (100/78)
            StatusListRow("Sector #3", "Cart #5", RowStatus.AlertTime("1h 19m 23s"), {})
            StatusListRow("Sector #1", "Cart #2", RowStatus.AlertTime("8m 45s"), {})
            // All Cart List (85/90)
            StatusListRow(
                "Cart #1", "Driving", RowStatus.Badge(StatusKind.Compliance), {},
                data1Width = 85.dp, data2Width = 90.dp
            )
            StatusListRow(
                "Cart #4", "Driving", RowStatus.Badge(StatusKind.Disconnect), {},
                data1Width = 85.dp, data2Width = 90.dp
            )
            // Disconnect (100/88), data2 없는 단일 행
            StatusListRow(
                "Cart #2", null, RowStatus.Elapsed("1 Days ago"), {},
                data2Width = 88.dp, showDivider = false
            )
        }
    }
}
