package com.example.geofencing.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// 디자인 파일의 시맨틱 토큰(fill/text/border/critical/warning/brand)을
// M3 ColorScheme 역할명과 분리해서 그대로 노출하는 레이어.
// 화면/컴포넌트에서는 MaterialTheme.extendedColors.xxx 로 접근한다.
data class ExtendedColors(
    val fillPrimary: Color,
    val fillSecondary: Color,
    val fillLowest: Color,
    val fillHighest: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val criticalPrimary: Color,
    val warningPrimary: Color,
    val brandPrimary: Color,
    val borderDefault: Color,
    val borderStrong: Color,
    val borderFocus: Color,
    val background: Color
)

val DarkExtendedColors = ExtendedColors(
    fillPrimary = DarkFillPrimary,
    fillSecondary = DarkFillSecondary,
    fillLowest = DarkFillLowest,
    fillHighest = DarkFillHighest,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textDisabled = DarkTextDisabled,
    criticalPrimary = DarkCriticalPrimary,
    warningPrimary = DarkWarningPrimary,
    brandPrimary = DarkBrandPrimary,
    borderDefault = DarkBorderDefault,
    borderStrong = DarkBorderStrong,
    borderFocus = DarkBorderFocus,
    background = DarkBackground
)

// TODO: 라이트 모드 디자인 값이 확정되면 실제 값으로 교체. 지금은 다크 값을 임시로 재사용.
val LightExtendedColors = DarkExtendedColors

val LocalExtendedColors = staticCompositionLocalOf { DarkExtendedColors }

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    get() = LocalExtendedColors.current
