package com.example.geofencing.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.geofencing.ui.theme.GeofencingTheme

// CartStatRow(코어)의 의미 래퍼. 섹터 카드 하단 통계: Whole Cart / Violation / Disconnect
@Composable
fun SectorCardStatRow(
    wholeCart: Int,
    violation: Int,
    disconnect: Int,
    modifier: Modifier = Modifier
) = CartStatRow(
    stats = listOf(
        CartStat(CartStatRole.WholeCart, wholeCart),
        CartStat(CartStatRole.Violation, violation),
        CartStat(CartStatRole.Disconnect, disconnect)
    ),
    style = CartStatStyle.Card,
    modifier = modifier
)

@Preview(name = "SectorCardStatRow", widthDp = 328, showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
private fun SectorCardStatRowPreview() {
    GeofencingTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // 전부 비-0
            SectorCardStatRow(wholeCart = 47, violation = 1, disconnect = 2)
            // violation / disconnect = 0 (독립적으로 disabled 되는지)
            SectorCardStatRow(wholeCart = 47, violation = 0, disconnect = 0)
            // 전부 0 → Whole Cart 라벨/값까지 disabled
            SectorCardStatRow(wholeCart = 0, violation = 0, disconnect = 0)
        }
    }
}
