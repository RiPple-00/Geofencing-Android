package com.example.geofencing.ui.map.slidepanel

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
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
// padding으로 안쪽 공간을 깎으면(32dp - 좌우 12dp*2 = 8dp) 16dp 아이콘이 그 안에 눌려서
// 더 작게 보인다 - Box + contentAlignment로 중앙 정렬만 해서 아이콘이 눌리지 않고
// 원래 크기(필요하면 32dp 박스 밖으로 살짝 넘치더라도) 그대로 보이게 한다.
@Composable
fun ChevronButton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(32.dp)
            .heightIn(min = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_arrow_right),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
    }
}
