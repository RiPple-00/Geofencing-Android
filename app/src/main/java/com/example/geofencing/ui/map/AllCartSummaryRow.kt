package com.example.geofencing.ui.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.geofencing.R
import com.example.geofencing.ui.theme.Body13
import com.example.geofencing.ui.theme.DarkBorderStrong
import com.example.geofencing.ui.theme.DarkCriticalPrimary
import com.example.geofencing.ui.theme.DarkTextSecondary
import com.example.geofencing.ui.theme.Label16
import com.example.geofencing.ui.theme.Label18
import com.example.geofencing.ui.theme.Pretendard

// Figma에 이름 없이 리터럴로 온 스타일(fontSize 12/lineHeight 21.6/weight 400) - 재사용되는
// 곳이 없어 Type.kt 토큰으로 승격하지 않고 이 컴포넌트 전용 상수로 둠.
private val UnitTextStyle = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 21.6.sp
)

// TODO: totalCartCount/violationCount는 domain/repository 확정 전까지 상수로 채움.
@Composable
fun AllCartSummaryRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .width(301.dp)
            .height(54.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally),
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
                Text(
                    text = "3",
                    modifier = Modifier
                        .width(12.dp)
                        .height(20.dp),
                    style = Label18,
                    color = DarkCriticalPrimary
                )
                Text(
                    text = "Unit",
                    modifier = Modifier
                        .width(22.dp)
                        .height(22.dp),
                    style = UnitTextStyle,
                    color = DarkCriticalPrimary
                )
                Row(
                    modifier = Modifier
                        .size(32.dp)
                        .padding(PaddingValues(horizontal = 12.dp, vertical = 8.dp)),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_arrow_right),
                        contentDescription = null
                    )
                }
            }
        }
    }
}
