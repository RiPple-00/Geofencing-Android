package com.example.geofencing.ui.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// 폴링 루프/딜레이와 분리된 순수 상태 전이 함수만 검증한다. 루프의 실제 타이밍 검증은
// 이번 범위에서 제외(후속 작업)하기로 했다.
class NextConsecutivePollFailuresTest {

    @Test
    fun `성공하면 0으로 리셋된다`() {
        assertEquals(0, nextConsecutivePollFailures(previous = 3, succeeded = true))
    }

    @Test
    fun `실패하면 이전 값에서 1씩 증가한다`() {
        assertEquals(1, nextConsecutivePollFailures(previous = 0, succeeded = false))
        assertEquals(2, nextConsecutivePollFailures(previous = 1, succeeded = false))
        assertEquals(3, nextConsecutivePollFailures(previous = 2, succeeded = false))
    }

    @Test
    fun `연속 실패 시작 시점에만 실패 상태로 전이하고 이후 연속 실패에서는 유지되며 성공하면 리셋된다`() {
        var count = 0

        count = nextConsecutivePollFailures(count, succeeded = false)
        assertTrue("최초 실패에서 실패 상태로 전이해야 한다", count > 0)

        val countAfterFirstFailure = count
        count = nextConsecutivePollFailures(count, succeeded = false)
        assertTrue("연속 실패 중에는 계속 실패 상태를 유지해야 한다", count > 0)
        assertTrue("연속 실패 횟수는 계속 누적되어야 한다", count > countAfterFirstFailure)

        count = nextConsecutivePollFailures(count, succeeded = true)
        assertFalse("성공하면 실패 상태가 리셋되어야 한다", count > 0)

        count = nextConsecutivePollFailures(count, succeeded = false)
        assertTrue("리셋 후 다시 실패하면 다시 실패 상태로 전이해야 한다", count > 0)
    }
}
