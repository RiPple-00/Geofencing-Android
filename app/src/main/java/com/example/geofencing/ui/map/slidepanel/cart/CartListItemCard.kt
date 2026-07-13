package com.example.geofencing.ui.map.slidepanel.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.geofencing.R
import com.example.geofencing.ui.theme.DarkBorderFocus
import com.example.geofencing.ui.theme.DarkBrandPrimary
import com.example.geofencing.ui.theme.DarkCriticalPrimary
import com.example.geofencing.ui.theme.DarkFillSecondary
import com.example.geofencing.ui.theme.DarkTextPrimary
import com.example.geofencing.ui.theme.Label16
import com.example.geofencing.ui.theme.RoundedMd
import com.example.geofencing.ui.theme.SlidePanelHorizontalMargin

// Sector 탭의 SectorListItemCard와 동일한 디자인(이름 + violation일 때 ic_warning/ic_ok,
// 우측 화살표). Cart 이름만 다르게 표시한다.
@Composable
fun CartListItemCard(
    name: String,
    hasViolation: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (hasViolation) DarkCriticalPrimary else DarkBorderFocus
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SlidePanelHorizontalMargin)
            .height(60.dp)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(RoundedMd))
            .background(color = DarkFillSecondary, shape = RoundedCornerShape(RoundedMd))
            .padding(start = 12.dp, top = 14.dp, end = 12.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                Text(text = name, style = Label16, color = DarkTextPrimary)
            }

            Image(
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
