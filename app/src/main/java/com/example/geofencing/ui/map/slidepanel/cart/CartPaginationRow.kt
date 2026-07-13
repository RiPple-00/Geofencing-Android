package com.example.geofencing.ui.map.slidepanel.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.geofencing.ui.theme.Label14
import com.example.geofencing.ui.theme.RoundedLg
import com.example.geofencing.ui.theme.RoundedMd
import com.example.geofencing.ui.theme.extendedColors

// 한 페이지에 최대 이만큼만 아이템을 보여주고, 넘치면 번호 페이지네이션으로 나눈다.
const val CartListPageSize = 7

// 페이지 번호 버튼: 현재 페이지는 fill+1.5dp border(6dp 라운드) 박스, 나머지는 배경 없는
// 텍스트만(8dp 라운드는 시각적으로 드러나지 않지만 리플 모양 통일을 위해 clip에 사용).
@Composable
fun CartPaginationRow(
    pageCount: Int,
    currentPage: Int,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            CartPageNumberButton(
                number = index + 1,
                selected = index == currentPage,
                onClick = { onPageSelected(index) }
            )
        }
    }
}

@Composable
private fun CartPageNumberButton(
    number: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(if (selected) RoundedMd else RoundedLg)
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(shape)
            .then(
                if (selected) {
                    Modifier
                        .background(color = MaterialTheme.extendedColors.fillPrimary, shape = shape)
                        .border(width = 1.5.dp, color = MaterialTheme.extendedColors.borderDefault, shape = shape)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            style = Label14,
            color = MaterialTheme.extendedColors.textSecondary
        )
    }
}
