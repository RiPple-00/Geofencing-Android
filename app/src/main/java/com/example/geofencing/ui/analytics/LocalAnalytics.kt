package com.example.geofencing.ui.analytics

import androidx.compose.runtime.staticCompositionLocalOf

// Composable 트리에서 AnalyticsLogger에 접근하기 위한 통로.
// Composable은 Hilt로 직접 주입받을 수 없어, MainActivity(@AndroidEntryPoint)가 주입받은
// 구현체를 CompositionLocalProvider로 내려준다. 계측은 이 값을 읽어 log/screen을 호출한다.
// 기본값은 no-op: Provider가 없는 @Preview 등에서도 안전하게 동작한다.
val LocalAnalytics = staticCompositionLocalOf<AnalyticsLogger> {
    object : AnalyticsLogger {
        override fun log(event: AnalyticsEvent) {}
        override fun screen(name: String) {}
    }
}
