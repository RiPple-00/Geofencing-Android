package com.example.geofencing.ui.mock

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

// parseInstant: 오프셋 포함 → OffsetDateTime, 그 외 → UTC Instant, 실패 → null.
class ParseInstantTest {

    @Test
    fun `오프셋 포함 타임스탬프를 파싱한다`() {
        // 2026-08-25T09:00:00+09:00 == 2026-08-25T00:00:00Z
        assertEquals(
            Instant.parse("2026-08-25T00:00:00Z"),
            parseInstant("2026-08-25T09:00:00+09:00")
        )
    }

    @Test
    fun `Z(UTC) 타임스탬프를 파싱한다`() {
        assertEquals(
            Instant.parse("2026-08-25T00:00:00Z"),
            parseInstant("2026-08-25T00:00:00Z")
        )
    }

    @Test
    fun `파싱 불가한 값은 null을 반환한다`() {
        assertNull(parseInstant("not-a-timestamp"))
        assertNull(parseInstant(""))
    }
}
