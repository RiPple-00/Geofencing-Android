package com.example.geofencing.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.geofencing.ui.theme.Body12
import com.example.geofencing.ui.theme.Body13
import com.example.geofencing.ui.theme.GeofencingTheme
import com.example.geofencing.ui.theme.Header24
import com.example.geofencing.ui.theme.Label18
import com.example.geofencing.ui.theme.UnitSuffixDisabledStyle
import com.example.geofencing.ui.theme.UnitSuffixStyle
import com.example.geofencing.ui.theme.extendedColors

// 3-스탯 각 컬럼의 역할. 라벨 텍스트 + 단위 표기 + 색 규칙을 캡슐화
// col1만 컨텍스트별로 달라짐(Compliance / Whole Carts / Whole Cart), Violation/Disconnect는 공통.
enum class CartStatRole(val label: String) {
    Compliance("Compliance"),
    WholeCarts("Whole Carts"),
    WholeCart("Whole Cart"),
    Violation("Violation"),
    Disconnect("Disconnect")
}

data class CartStat(val role: CartStatRole, val value: Int)

// 사이즈 변형. Header=페이지 상단(header/24 숫자), Card=섹터 카드(label/18 숫자 + cart 단위).
sealed interface CartStatStyle {
    data object Header : CartStatStyle
    data object Card : CartStatStyle
}

// 고정 컬럼 폭. 나머지 컬럼은 글자 크기만큼(wrap), 컬럼 사이는 SpaceBetween(auto).
private val HeaderColumnWidth = 94.dp
private val CardViolationWidth = 61.dp

// 스타일별 행 좌우 여백: Header(Whole Carts/Compliance)=8, Card(Whole Cart)=0.
private val HeaderRowHorizontalPadding = 8.dp
private val CardRowHorizontalPadding = 0.dp

// 라벨+수치 3열 통계. MapScreen의 SlidePanel Sector item(SectorDetailCard의 SectorStatColumn)을 응용.
// 색은 값/역할/스타일에 따라 컴포넌트가 결정
@Composable
fun CartStatRow(
    stats: List<CartStat>,
    style: CartStatStyle,
    modifier: Modifier = Modifier
) {
    val horizontalPadding = when (style) {
        CartStatStyle.Header -> HeaderRowHorizontalPadding
        CartStatStyle.Card -> CardRowHorizontalPadding
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        stats.forEach { stat ->
            CartStatColumn(stat = stat, style = style)
        }
    }
}

@Composable
private fun CartStatColumn(
    stat: CartStat,
    style: CartStatStyle,
    modifier: Modifier = Modifier
) {
    val labelStyle: TextStyle
    val valueStyle: TextStyle
    when (style) {
        CartStatStyle.Header -> {
            labelStyle = Body13
            valueStyle = Header24
        }
        CartStatStyle.Card -> {
            labelStyle = Body12
            valueStyle = Label18
        }
    }
    val width = columnWidth(stat.role, style)

    Column(
        modifier = if (width != null) modifier.width(width) else modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stat.role.label,
            style = labelStyle,
            color = labelColor(style, stat.value)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stat.value.toString(),
                style = valueStyle,
                color = valueColor(stat.role, stat.value)
            )
            // Card 스타일에선 값 뒤에 "cart" unit이 붙는다. 숫자 색과 무관하게:
            // 값≠0 → text-secondary(+180% line-height), 값 0 → text-disabled(+140% line-height).
            if (style == CartStatStyle.Card) {
                val enabled = stat.value != 0
                Text(
                    text = "cart",
                    style = if (enabled) UnitSuffixStyle else UnitSuffixDisabledStyle,
                    color = if (enabled) {
                        MaterialTheme.extendedColors.textSecondary
                    } else {
                        MaterialTheme.extendedColors.textDisabled
                    }
                )
            }
        }
    }
}

// 고정 폭: Header는 col1/Violation=94dp, Card는 Violation=61dp. 나머지는 wrap(null).
private fun columnWidth(role: CartStatRole, style: CartStatStyle): Dp? = when (style) {
    CartStatStyle.Header -> when (role) {
        CartStatRole.Compliance, CartStatRole.WholeCarts, CartStatRole.Violation -> HeaderColumnWidth
        else -> null
    }
    CartStatStyle.Card -> when (role) {
        CartStatRole.Violation -> CardViolationWidth
        else -> null
    }
}

@Composable
private fun valueColor(role: CartStatRole, value: Int): Color {
    val colors = MaterialTheme.extendedColors
    return when {
        value == 0 -> colors.textDisabled
        role == CartStatRole.Violation -> colors.criticalPrimary
        role == CartStatRole.Disconnect -> colors.borderStrong
        else -> colors.textPrimary
    }
}

@Composable
private fun labelColor(style: CartStatStyle, value: Int): Color {
    val colors = MaterialTheme.extendedColors
    return when {
        value != 0 -> colors.textSecondary
        style == CartStatStyle.Card -> colors.textDisabled
        else -> colors.borderStrong
    }
}

// col1 라벨별(Compliance / Whole Carts / Whole Cart) × 값 0/비-0 색 확인용. Violation/Disconnect 컬럼 포함.
// Compliance·Whole Carts는 Header 스타일, Whole Cart는 Card 스타일.
@Preview(name = "Compliance 62", widthDp = 328, showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
private fun ComplianceNonZeroPreview() {
    GeofencingTheme {
        CartStatRow(
            listOf(
                CartStat(CartStatRole.Compliance, 62),
                CartStat(CartStatRole.Violation, 3),
                CartStat(CartStatRole.Disconnect, 2)
            ),
            CartStatStyle.Header
        )
    }
}

@Preview(name = "Compliance 0", widthDp = 328, showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
private fun ComplianceZeroPreview() {
    GeofencingTheme {
        CartStatRow(
            listOf(
                CartStat(CartStatRole.Compliance, 0),
                CartStat(CartStatRole.Violation, 0),
                CartStat(CartStatRole.Disconnect, 0)
            ),
            CartStatStyle.Header
        )
    }
}

@Preview(name = "Whole Carts 47", widthDp = 328, showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
private fun WholeCartsNonZeroPreview() {
    GeofencingTheme {
        CartStatRow(
            listOf(
                CartStat(CartStatRole.WholeCarts, 47),
                CartStat(CartStatRole.Violation, 3),
                CartStat(CartStatRole.Disconnect, 2)
            ),
            CartStatStyle.Header
        )
    }
}

@Preview(name = "Whole Carts 0", widthDp = 328, showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
private fun WholeCartsZeroPreview() {
    GeofencingTheme {
        CartStatRow(
            listOf(
                CartStat(CartStatRole.WholeCarts, 0),
                CartStat(CartStatRole.Violation, 0),
                CartStat(CartStatRole.Disconnect, 0)
            ),
            CartStatStyle.Header
        )
    }
}

@Preview(name = "Whole Cart 47", widthDp = 328, showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
private fun WholeCartNonZeroPreview() {
    GeofencingTheme {
        CartStatRow(
            listOf(
                CartStat(CartStatRole.WholeCart, 47),
                CartStat(CartStatRole.Violation, 1),
                CartStat(CartStatRole.Disconnect, 2)
            ),
            CartStatStyle.Card
        )
    }
}

@Preview(name = "Whole Cart 0", widthDp = 328, showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
private fun WholeCartZeroPreview() {
    GeofencingTheme {
        CartStatRow(
            listOf(
                CartStat(CartStatRole.WholeCart, 0),
                CartStat(CartStatRole.Violation, 0),
                CartStat(CartStatRole.Disconnect, 0)
            ),
            CartStatStyle.Card
        )
    }
}
