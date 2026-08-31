package com.example.geofencing.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// 컴포넌트별로 공통으로 참조하는 코너 반경
val ButtonShape = Shapes.small
val CardShape = Shapes.small // 모니터링 위젯 카드 실측값 8dp
val BottomSheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)

// 보더 두께 (실측값)
val BorderWidth4 = 4.dp
val BorderWidth6 = 6.dp
val BorderWidth8 = 8.dp

// 코너 반경 (실측값, 피그마 변수명 그대로)
val RoundedMd = 6.dp
val RoundedLg = 8.dp
