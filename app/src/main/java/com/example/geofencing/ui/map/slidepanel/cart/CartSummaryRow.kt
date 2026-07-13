package com.example.geofencing.ui.map.slidepanel.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.geofencing.ui.theme.Body13
import com.example.geofencing.ui.theme.DarkBorderStrong
import com.example.geofencing.ui.theme.DarkCriticalPrimary
import com.example.geofencing.ui.theme.DarkTextPrimary
import com.example.geofencing.ui.theme.DarkTextSecondary
import com.example.geofencing.ui.theme.Label16
import com.example.geofencing.ui.theme.Label18
import com.example.geofencing.ui.theme.UnitSuffixStyle

// Summary 탭 AllCartSummaryRow와 같은 내용(All Cart/Violation/Compliance)을 재사용하되
// 간격은 실측값(All Cart-Violation 67dp, Violation-Compliance 29.5dp)으로 고정하고,
// chevron 아이콘은 쓰지 않는다.
@Composable
fun CartSummaryRow(
    modifier: Modifier = Modifier,
    allCartTotal: Int = 0,
    violationCount: Int = 0,
    complianceCount: Int = 0
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp)
            .height(54.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .width(78.dp)
                .height(16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "All Cart", style = Label16, color = DarkTextSecondary)
            Text(text = allCartTotal.toString(), style = Label16, color = DarkTextSecondary)
        }

        Spacer(modifier = Modifier.width(67.dp))

        Column(
            modifier = Modifier
                .width(74.dp)
                .height(54.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Violation",
                modifier = Modifier
                    .width(51.dp)
                    .height(18.dp),
                style = Body13,
                color = DarkBorderStrong
            )
            ValueWithUnit(
                number = violationCount.toString(),
                numberColor = DarkCriticalPrimary,
                unitColor = DarkCriticalPrimary
            )
        }

        Spacer(modifier = Modifier.width(29.5.dp))

        Column(
            modifier = Modifier
                .width(84.dp)
                .height(54.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Compliance",
                modifier = Modifier
                    .width(69.dp)
                    .height(18.dp),
                style = Body13,
                color = DarkBorderStrong
            )
            ValueWithUnit(
                number = complianceCount.toString(),
                numberColor = DarkTextPrimary,
                unitColor = DarkTextSecondary
            )
        }
    }
}

@Composable
private fun ValueWithUnit(
    number: String,
    numberColor: Color,
    unitColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = number,
            style = Label18,
            color = numberColor
        )
        Text(
            text = "Unit",
            style = UnitSuffixStyle,
            color = unitColor
        )
    }
}
