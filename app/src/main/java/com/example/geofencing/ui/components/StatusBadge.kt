package com.example.geofencing.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.geofencing.R
import com.example.geofencing.ui.theme.Body14
import com.example.geofencing.ui.theme.extendedColors

// 지오펜스 상태 종류. 배지 / 리스트 행(RowStatus.Badge) / Cart 상세가 공유.
// 라벨을 보유하고, 색·아이콘은 아래 확장으로 매핑
enum class StatusKind(val label: String) {
    Compliance("Compliance"),
    Violation("Violation"),
    Disconnect("Disconnect")
}

// kind → 색: 단일 진실 소스. 배지 아이콘뿐 아니라 Cart 상세의 주소 텍스트 등도 이 색을 공유
// 테마 대응 색이라 @Composable 컨텍스트에서만 접근 가능
val StatusKind.color: Color
    @Composable get() = when (this) {
        StatusKind.Compliance -> MaterialTheme.extendedColors.brandPrimary
        StatusKind.Violation -> MaterialTheme.extendedColors.criticalPrimary
        StatusKind.Disconnect -> MaterialTheme.extendedColors.borderStrong
    }

// kind → 아이콘: Violation/Disconnect는 ic_warning 공유(색만 다름), Compliance는 ic_ok.
val StatusKind.iconRes: Int
    get() = when (this) {
        StatusKind.Compliance -> R.drawable.ic_ok
        StatusKind.Violation, StatusKind.Disconnect -> R.drawable.ic_warning
    }

// 아이콘과 라벨 사이 gap
private val BadgeIconTextGap = 6.dp

// 상태 배지: [kind 색 아이콘] [라벨]. 리스트 행의 Badge와 Cart 상세의 Operation/Geofence Status가 공유.
// 아이콘만 kind 색이고 라벨은 항상 text/secondary
@Composable
fun StatusBadge(kind: StatusKind, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(BadgeIconTextGap),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(kind.iconRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(kind.color),
            modifier = Modifier.size(16.dp)
        )
        Text(text = kind.label, style = Body14, color = MaterialTheme.extendedColors.textSecondary)
    }
}
