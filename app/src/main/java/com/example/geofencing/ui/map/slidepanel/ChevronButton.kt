package com.example.geofencing.ui.map.slidepanel

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.geofencing.R

// Summary/Sector 등 여러 카드가 공유하는 chevron(>) 버튼.
// CSS 기준: width는 32px 고정이지만 height는 지정값 없이 min-height:24px만 있어서
// 세로는 콘텐츠(아이콘+padding)에 맞춰 늘어나는 구조 - height를 고정하지 않는다.
@Composable
fun ChevronButton(modifier: Modifier = Modifier) {
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
            modifier = Modifier
                .padding(1.dp)
                .size(16.dp)
        )
    }
}
