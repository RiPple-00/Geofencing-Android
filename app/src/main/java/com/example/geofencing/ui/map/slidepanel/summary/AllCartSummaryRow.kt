package com.example.geofencing.ui.map.slidepanel.summary

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
import com.example.geofencing.ui.map.slidepanel.ChevronButton
import com.example.geofencing.ui.theme.Body13
import com.example.geofencing.ui.theme.DarkBorderStrong
import com.example.geofencing.ui.theme.DarkCriticalPrimary
import com.example.geofencing.ui.theme.DarkTextPrimary
import com.example.geofencing.ui.theme.DarkTextSecondary
import com.example.geofencing.ui.theme.Label16
import com.example.geofencing.ui.theme.Label18
import com.example.geofencing.ui.theme.UnitSuffixStyle

// 좌우 30dp 여백 + All Cart-Violation : Violation-Compliance 간격 = 30:35 비율로 화면
// 폭에 맞춰 유연하게 분배(고정 301dp 블록 대신). 각 그룹 자체 폭(78/74/84dp)은 고정값 유지.
// TODO: All Cart/Violation/Compliance 수치는 domain/repository 확정 전까지 상수로 채움.
@Composable
fun AllCartSummaryRow(modifier: Modifier = Modifier) {
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
            Text(text = "50", style = Label16, color = DarkTextSecondary)
        }

        Spacer(modifier = Modifier.weight(30f))

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
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ValueWithUnit(number = "3", numberColor = DarkCriticalPrimary, unitColor = DarkCriticalPrimary)
                ChevronButton()
            }
        }

        Spacer(modifier = Modifier.weight(35f))

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
            Row(
                modifier = Modifier
                    .width(84.dp)
                    .height(32.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ValueWithUnit(number = "47", numberColor = DarkTextPrimary, unitColor = DarkTextSecondary)
                ChevronButton()
            }
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
