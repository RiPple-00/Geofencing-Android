package com.example.geofencing.ui.analytics

// 사용자 행동 로그의 진입점. UI/ViewModel은 "어디로 보내는지"(Firebase/자체 API/no-op)를
// 몰라야 하며, 이 인터페이스에만 의존한다. 구현체 교체만으로 전송 대상을 바꾼다.
// (DashboardRepository의 mock↔API 교체 패턴과 동일한 철학.)
interface AnalyticsLogger {
    fun log(event: AnalyticsEvent)
    fun screen(name: String)
}
