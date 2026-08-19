package com.example.geofencing.ui.analytics

// 수집할 사용자 행동을 타입으로 고정한다. 문자열("button_click")을 여기저기 흩뿌리면
// 오타·중복으로 데이터가 망가지므로, 이벤트는 반드시 이 sealed 계층으로만 표현한다.
// name/params는 전송 백엔드(Firebase/자체 API)가 그대로 쓰는 표준 형태.
sealed class AnalyticsEvent(
    val name: String,
    val params: Map<String, Any?> = emptyMap()
) {
    data object BellClicked : AnalyticsEvent("bell_clicked")

    data class TabSelected(val index: Int) :
        AnalyticsEvent("tab_selected", mapOf("index" to index))

    // 섹터 카드 → 해당 섹터 탭 진입
    data class SectorOpened(val sector: String) :
        AnalyticsEvent("sector_opened", mapOf("sector" to sector))

    // 카트 상세 드릴다운. from = 어느 지점에서 열었는지(가장 값진 데이터).
    data class CartOpened(val sector: String, val cart: String, val from: String) :
        AnalyticsEvent("cart_opened", mapOf("sector" to sector, "cart" to cart, "from" to from))

    // 로드 실패 후 재시도. screen = 어느 화면에서 눌렸는지.
    data class RetryClicked(val screen: String) :
        AnalyticsEvent("retry_clicked", mapOf("screen" to screen))
}
