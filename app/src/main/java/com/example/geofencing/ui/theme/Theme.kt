package com.example.geofencing.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// M3 표준 컴포넌트(Button, TextField, Scaffold 등)가 참조하는 역할에만
// 디자인 토큰을 매핑한다. primaryContainer/tertiary 등 대응하는 디자인 값이
// 없는 역할은 M3 기본값을 그대로 둔다 (해당 역할 쓰는 컴포넌트 추가 시 다시 확인 필요).
private val DarkColorScheme = darkColorScheme(
    primary = DarkBrandPrimary,
    onPrimary = DarkFillHighest,
    secondary = DarkFillSecondary,
    onSecondary = DarkTextPrimary,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkFillPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorderStrong,
    outlineVariant = DarkBorderDefault,
    error = DarkCriticalPrimary,
    onError = DarkFillLowest
)

private val LightColorScheme = lightColorScheme(
    primary = LightBrandPrimary,
    onPrimary = LightFillHighest,
    secondary = LightFillSecondary,
    onSecondary = LightTextPrimary,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightFillPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorderStrong,
    outlineVariant = LightBorderDefault,
    error = LightCriticalPrimary,
    onError = LightFillLowest
)

@Composable
fun GeofencingTheme(
    // 현재는 다크 전용 디자인이라 기본값 true로 고정. 추후 라이트 모드 지원 시
    // isSystemInDarkTheme() 또는 사용자 설정값으로 교체
    darkTheme: Boolean = true,
    // 디자인 파일의 고정 팔레트를 그대로 쓰기 위해 기본값은 false (Android 12+ 다이나믹 컬러 미사용)
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}