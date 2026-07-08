package com.example.geofencing.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.geofencing.R

val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold)
)

// 디자인 파일의 텍스트 스타일 토큰 (실측값)
// TODO: 나머지 스타일(header/24, caption/* 등)이 확정되면 계속 추가하세요.
val Header20 = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 24.sp
)
val Body13 = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 18.2.sp
)
val Body14 = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 19.6.sp
)
val Label13 = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.Medium,
    fontSize = 13.sp,
    lineHeight = 14.3.sp
)
val Label14 = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 16.1.sp
)
val Label16 = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 16.sp
)
val Label18 = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.Medium,
    fontSize = 18.sp,
    lineHeight = 19.8.sp
)
val Label10 = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.Medium,
    fontSize = 10.sp,
    lineHeight = 11.5.sp
)
val Body10 = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.Normal,
    fontSize = 10.sp,
    lineHeight = 14.sp
)

// M3 표준 컴포넌트(TopAppBar, Button, TextField 등)가 참조하는 Typography.
// 실측값이 있는 역할은 위 raw 토큰을 그대로 재사용하고, 대응 값이 없는 역할은
// Pretendard 폰트만 맞춘 임시값을 쓴다(TODO 표시, 실측값 확정 시 교체).
val Typography = Typography(
    // 제목 - header/20 하나만 확정. Medium/Small은 아직 디자인 값 없음(TODO)
    titleLarge = Header20,
    titleMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),
    // 본문 - bodyLarge는 아직 디자인 값 없음(TODO)
    bodyLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = Body14,
    bodySmall = Body13,
    // 라벨 - label/13,14,16,18 중 3개만 M3 슬롯에 매핑. label/18은 raw 상수로만 사용.
    labelLarge = Label16,
    labelMedium = Label14,
    labelSmall = Label13
)
