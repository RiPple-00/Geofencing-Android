package com.example.geofencing.ui.map.slidepanel.summary

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.geofencing.R
import com.example.geofencing.ui.theme.Body13
import com.example.geofencing.ui.theme.DarkBorderStrong
import com.example.geofencing.ui.theme.DarkCriticalPrimary
import com.example.geofencing.ui.theme.DarkTextPrimary
import com.example.geofencing.ui.theme.DarkTextSecondary
import com.example.geofencing.ui.theme.Label16
import com.example.geofencing.ui.theme.Label18
import com.example.geofencing.ui.theme.Pretendard

// Figma에 이름 없이 리터럴로 온 스타일(fontSize 12/lineHeight 21.6/weight 400)
// - 재사용되는 곳이 없어 이 컴포넌트 전용 상수로 둠.
// trim=Both + includeFontPadding=false로 lineHeight/레거시 여백을 다 걷어내고 폰트 실제
// 크기에 타이트하게 맞춰서, 크기가 다른 Label18("3"/"47")과 세로 중심이 맞도록 한다.
private val UnitTextStyle = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 21.6.sp,
    letterSpacing = (-0.024).sp,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both
    )
)

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
            style = UnitTextStyle,
            color = unitColor
        )
    }
}

// Violation/Compliance 둘 다 동일한 chevron 버튼(padding/아이콘 동일) 공유.
// CSS 기준: width는 32px 고정이지만 height는 지정값 없이 min-height:24px만 있어서
// 세로는 콘텐츠(아이콘+padding)에 맞춰 늘어나는 구조 - height를 고정하지 않는다.
@Composable
private fun ChevronButton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .width(32.dp)
            .heightIn(min = 24.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_arrow_right),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
    }
}
