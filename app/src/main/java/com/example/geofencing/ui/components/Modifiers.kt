package com.example.geofencing.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.geofencing.ui.theme.CardShape
import com.example.geofencing.ui.theme.extendedColors

// borderBox의 테두리 색 선택지. 디자인의 border 토큰으로만 제한한다.
enum class BorderTone { Focus, Default }

// 라디우스(8dp) + 1dp 테두리 고정 보더 박스. 색만 tone으로 선택하고, 내부 패딩은 호출부가 담당.
fun Modifier.borderBox(tone: BorderTone = BorderTone.Focus): Modifier = composed {
    val color = when (tone) {
        BorderTone.Focus -> MaterialTheme.extendedColors.borderFocus
        BorderTone.Default -> MaterialTheme.extendedColors.borderDefault
    }
    clip(CardShape)
        .border(1.dp, color, CardShape)
}

// 리플(물결) 없는 클릭. 아이콘 버튼 등에서 반복되던 보일러플레이트를 한 곳으로.
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}
