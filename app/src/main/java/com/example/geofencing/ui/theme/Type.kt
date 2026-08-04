package com.example.geofencing.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.example.geofencing.R

val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold)
)

// 기본 설정이면 lineHeight가 fontSize보다 클 때 여백이 글자 아래로만 쏠려서, 같은 Row 안에
// 크기가 다른 텍스트끼리 세로로 정렬해도 기준선이 어긋나 보인다(예: "3"/"Unit"). 모든 텍스트
// 토큰에 동일하게 적용해서 글자가 항상 자기 박스 안에서 수직 중앙에 오도록 강제한다.
// trim=Both로 lineHeight가 만든 여분 여백을 위아래 다 제거해서 박스를 폰트 실제 크기에
// 타이트하게 맞추고, includeFontPadding=false로 레거시 안드로이드 여백까지 제거한다.
private val CenteredLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.Both
)
private val NoFontPadding = PlatformTextStyle(includeFontPadding = false)

// 디자인 파일의 텍스트 스타일 토큰
val Header20 = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 24.sp,
    platformStyle = NoFontPadding,
    lineHeightStyle = CenteredLineHeight
)
val Header24 = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.SemiBold,
    fontSize = 24.sp,
    lineHeight = 31.2.sp,
    platformStyle = NoFontPadding,
    lineHeightStyle = CenteredLineHeight
)
val Body12 = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.8.sp,
    platformStyle = NoFontPadding,
    lineHeightStyle = CenteredLineHeight
)
val Body13 = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 18.2.sp,
    platformStyle = NoFontPadding,
    lineHeightStyle = CenteredLineHeight
)
val Body14 = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 19.6.sp,
    platformStyle = NoFontPadding,
    lineHeightStyle = CenteredLineHeight
)
val Label13 = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.Medium,
    fontSize = 13.sp,
    lineHeight = 14.3.sp,
    platformStyle = NoFontPadding,
    lineHeightStyle = CenteredLineHeight
)
val Label14 = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 16.1.sp,
    platformStyle = NoFontPadding,
    lineHeightStyle = CenteredLineHeight
)
val Label16 = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 16.sp,
    platformStyle = NoFontPadding,
    lineHeightStyle = CenteredLineHeight
)
// 카드/섹션 타이틀용
// Label16(16/16)과 lineHeight 비율이 달라 별도 토큰.
// StatusCardContainer의 사설 StatusCardTitleStyle과 동일 값
val Title16 = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 19.2.sp,
    platformStyle = NoFontPadding,
    lineHeightStyle = CenteredLineHeight
)
val Label18 = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.Medium,
    fontSize = 18.sp,
    lineHeight = 19.8.sp,
    platformStyle = NoFontPadding,
    lineHeightStyle = CenteredLineHeight
)
val Label10 = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.Medium,
    fontSize = 10.sp,
    lineHeight = 11.5.sp,
    platformStyle = NoFontPadding,
    lineHeightStyle = CenteredLineHeight
)
val Body10 = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.Normal,
    fontSize = 10.sp,
    lineHeight = 14.sp,
    platformStyle = NoFontPadding,
    lineHeightStyle = CenteredLineHeight
)

// 이름 없이 리터럴로 온 스타일(fontSize 12/lineHeight 21.6/weight 400) - Summary/Sector 양쪽에서 재사용되어 공용 토큰으로 승격.
val UnitSuffixStyle = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 21.6.sp,
    letterSpacing = (-0.024).sp,
    platformStyle = NoFontPadding,
    lineHeightStyle = CenteredLineHeight
)

// M3 표준 컴포넌트(TopAppBar, Button, TextField 등)가 참조하는 Typography.
// 실측값이 있는 역할은 위 raw 토큰을 그대로 재사용하고, 대응 값이 없는 역할은
// Pretendard 폰트만 맞춘 임시값을 쓴다
val Typography = Typography(
    // 제목 - header/20 하나만 확정. Medium/Small은 아직 디자인 값 없음(TODO)
    titleLarge = Header20,
    titleMedium = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        platformStyle = NoFontPadding,
        lineHeightStyle = CenteredLineHeight
    ),
    titleSmall = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        platformStyle = NoFontPadding,
        lineHeightStyle = CenteredLineHeight
    ),
    // 본문 - bodyLarge는 아직 디자인 값 없음(TODO)
    bodyLarge = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        platformStyle = NoFontPadding,
        lineHeightStyle = CenteredLineHeight
    ),
    bodyMedium = Body14,
    bodySmall = Body13,
    // 라벨 - label/13,14,16,18 중 3개만 M3 슬롯에 매핑. label/18은 raw 상수로만 사용.
    labelLarge = Label16,
    labelMedium = Label14,
    labelSmall = Label13
)
