package com.example.geofencing.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.geofencing.ui.theme.GeofencingTheme
import com.example.geofencing.ui.theme.Label14
import com.example.geofencing.ui.theme.Space05
import com.example.geofencing.ui.theme.Title16
import com.example.geofencing.ui.theme.extendedColors

// title과 item list 사이 기본 간격. StatusListRow(48dp 중앙정렬) 섹션 기준:
// 실제 "제목 → 첫 cart" = 이 값 + Space05(2) + 중앙정렬(≈16). 목표 34.5 = 16.5 + 2 + ≈16.
// 아이템이 플러시 카드인 섹션(Sector List)은 중앙정렬이 없어 호출부에서 titleToItemsGap을 따로 넘긴다.
private val SectionTitleToItemsGap = 16.5.dp

// 섹션 = [타이틀 줄: 제목(좌) + 수량(우)] + [아이템 슬롯]. Violation / Disconnect / Sector List 공용.
// 제목/수량 양식만 이 컴포넌트가 고정하고, 아이템은 호출부가 슬롯으로 넘김
// count/unit은 "3 Carts", "3 Sectors"처럼 숫자+단위로 표기(디자인상 복수형 처리는 없음).
@Composable
fun ListSection(
    title: String,
    count: Int,
    unit: String,
    modifier: Modifier = Modifier,
    titleToItemsGap: Dp = SectionTitleToItemsGap,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = Title16,
                color = MaterialTheme.extendedColors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$count $unit",
                style = Label14,
                color = MaterialTheme.extendedColors.borderStrong
            )
        }

        Spacer(modifier = Modifier.height(titleToItemsGap))

        // 아이템 리스트 전체에 상하 2dp(Figma --05) 패딩. 좌우는 0.
        Column(modifier = Modifier.padding(vertical = Space05)) {
            content()
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
private fun ListSectionPreview() {
    GeofencingTheme {
        ListSection(title = "Violation", count = 3, unit = "Carts") {
            ViolationRow("Sector #3", "Cart #5", "1h 19m 23s", {})
            ViolationRow("Sector #1", "Cart #2", "8m 45s", {})
            ViolationRow("Sector #3", "Cart #7", "2m 6s", {}, showDivider = false)
        }
    }
}
