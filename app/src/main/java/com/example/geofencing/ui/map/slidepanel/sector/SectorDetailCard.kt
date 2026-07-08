package com.example.geofencing.ui.map.slidepanel.sector

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.geofencing.R
import com.example.geofencing.ui.map.slidepanel.ChevronButton
import com.example.geofencing.ui.theme.Body13
import com.example.geofencing.ui.theme.CardShape
import com.example.geofencing.ui.theme.DarkBorderFocus
import com.example.geofencing.ui.theme.DarkBorderStrong
import com.example.geofencing.ui.theme.DarkBrandPrimary
import com.example.geofencing.ui.theme.DarkCriticalPrimary
import com.example.geofencing.ui.theme.DarkFillSecondary
import com.example.geofencing.ui.theme.DarkTextPrimary
import com.example.geofencing.ui.theme.DarkTextSecondary
import com.example.geofencing.ui.theme.Label18
import com.example.geofencing.ui.theme.Pretendard
import com.example.geofencing.ui.theme.SlidePanelHorizontalMargin
import com.example.geofencing.ui.theme.UnitSuffixStyle

// Figma에 이름 없이 리터럴로 온 스타일(label/12: fontSize 12/lineHeight 13.2/weight 500)
// - 재사용되는 곳이 없어 이 컴포넌트 전용 상수로 둠.
private val Label12 = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 13.2.sp,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both
    )
)

// 좌우로 스와이프되는 Sector 상세 정보 카드. header/section/status_area 3부분 중
// section은 콘텐츠가 없어 생략 - header와 status_area만 구현.
// TODO: Sector 이름/주소/수치는 domain/repository 확정 전까지 상수로 채움.
@Composable
fun SectorDetailCard(
    modifier: Modifier = Modifier,
    name: String = "Sector #1",
    address: String = "1776 Terminal Dr, Richland, WA 99354",
    allCartCount: Int = 16,
    violationCount: Int = 4,
    complianceCount: Int = 47
) {
    // violation이 하나라도 있으면 카드 border가 criticalPrimary(빨강)로 바뀐다 - 상세
    // 카드/리스트 카드 공통 규칙.
    val borderColor = if (violationCount > 0) DarkCriticalPrimary else DarkBorderFocus
    Column(
        modifier = modifier
            .fillMaxWidth()
            // StateTabRow 등 SlidePanel 전체가 공유하는 좌우 공통 여백.
            .padding(horizontal = SlidePanelHorizontalMargin)
            .height(154.dp)
            .border(width = 1.dp, color = borderColor, shape = CardShape)
            .background(color = DarkFillSecondary, shape = CardShape)
    ) {
        SectorDetailHeader(
            name = name,
            address = address,
            hasViolation = violationCount > 0,
            // 카드 상단과 19dp, status_area와 24dp 간격 실측값.
            modifier = Modifier.padding(top = 19.dp, bottom = 24.dp)
        )
        SectorStatusArea(
            allCartCount = allCartCount,
            violationCount = violationCount,
            complianceCount = complianceCount
        )
    }
}

@Composable
private fun SectorDetailHeader(
    name: String,
    address: String,
    hasViolation: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(start = 12.dp, end = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .width(260.dp)
                .height(41.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // violation이 하나라도 있으면 ic_warning(자체 빨간색 내장), 없으면
                // ic_ok(brandPri/primary로 tint).
                if (hasViolation) {
                    Image(
                        painter = painterResource(R.drawable.ic_warning),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(1.16667.dp)
                            .size(14.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.ic_ok),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(DarkBrandPrimary),
                        modifier = Modifier
                            .padding(1.16667.dp)
                            .size(14.dp)
                    )
                }
                Text(
                    text = name,
                    style = Label18,
                    color = DarkTextPrimary
                )
            }
            Text(
                text = address,
                modifier = Modifier.width(260.dp),
                style = Label12,
                color = DarkTextSecondary
            )
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = null,
                modifier = Modifier
                    .padding(1.dp)
                    .size(20.dp)
            )
        }
    }
}

// 세 항목은 SpaceBetween, 좌우 여백 20dp (실측 확인됨).
@Composable
private fun SectorStatusArea(
    allCartCount: Int,
    violationCount: Int,
    complianceCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        SectorStatColumn(
            label = "All Cart",
            value = allCartCount.toString(),
            valueColor = DarkTextPrimary,
            modifier = Modifier.width(46.dp)
        )
        SectorStatColumn(
            label = "Violation",
            value = violationCount.toString(),
            // violation이 0이면 다른 수치와 같은 기본 색상, 1개 이상이면 빨강으로 강조.
            valueColor = if (violationCount > 0) DarkCriticalPrimary else DarkTextPrimary
        )
        SectorStatColumn(
            label = "Compliance",
            value = complianceCount.toString(),
            valueColor = DarkTextPrimary,
            showArrow = true
        )
    }
}

@Composable
private fun SectorStatColumn(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    showArrow: Boolean = false
) {
    Column(
        modifier = modifier.height(44.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = label, style = Body13, color = DarkBorderStrong)
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = value, style = Label18, color = valueColor)
            Text(text = "Unit", style = UnitSuffixStyle, color = DarkTextSecondary)
            if (showArrow) {
                ChevronButton()
            }
        }
    }
}
