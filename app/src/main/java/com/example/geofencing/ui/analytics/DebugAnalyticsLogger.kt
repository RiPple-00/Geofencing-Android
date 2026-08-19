package com.example.geofencing.ui.analytics

import android.util.Log
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

// 스캐폴딩용 임시 구현: 실제 전송 대신 Logcat("Analytics" 태그)에만 찍는다.
// 이걸로 "어떤 이벤트가 언제 찍히나"를 개발 중 바로 확인할 수 있고,
// 실제 수집이 필요해지면 FirebaseAnalyticsLogger 등으로 @Binds만 바꾸면 된다.
// TODO(전송): 실 구현체 추가 시 BuildConfig.DEBUG일 때만 Logcat, 릴리스는 실제 전송으로 분기.
@Singleton
class DebugAnalyticsLogger @Inject constructor() : AnalyticsLogger {
    override fun log(event: AnalyticsEvent) {
        val suffix = if (event.params.isEmpty()) "" else " ${event.params}"
        Log.d(TAG, "event: ${event.name}$suffix")
    }

    override fun screen(name: String) {
        Log.d(TAG, "screen: $name")
    }

    private companion object {
        const val TAG = "Analytics"
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {
    @Binds
    abstract fun bindAnalyticsLogger(impl: DebugAnalyticsLogger): AnalyticsLogger
}
