package com.example.geofencing.ui.mock

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration

// formatDuration: 위반 지속시간(now - occurred_at) 포맷. 1시간 미만은 "Xm YYs", 이상은 "Xh YYm".
class FormatDurationTest {

    @Test
    fun `1시간 미만은 분과 초로 표시된다`() {
        assertEquals("8m 45s", formatDuration(Duration.ofSeconds(8 * 60 + 45)))
        assertEquals("0m 05s", formatDuration(Duration.ofSeconds(5)))
        assertEquals("0m 00s", formatDuration(Duration.ZERO))
    }

    @Test
    fun `초는 2자리로 0 패딩된다`() {
        assertEquals("1m 09s", formatDuration(Duration.ofSeconds(69)))
    }

    @Test
    fun `1시간 이상은 시와 분으로 표시되고 분은 2자리 패딩된다`() {
        assertEquals("2h 05m", formatDuration(Duration.ofMinutes(2 * 60 + 5)))
        assertEquals("1h 00m", formatDuration(Duration.ofHours(1)))
    }

    @Test
    fun `음수(시계 오차 등)는 0으로 보정된다`() {
        assertEquals("0m 00s", formatDuration(Duration.ofSeconds(-30)))
    }
}
