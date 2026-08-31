package com.example.geofencing.ui.sector

import org.junit.Assert.assertEquals
import org.junit.Test

// paginationItems: total<=5면 전부 숫자, 초과면 [1 … (현재±1) … 마지막]로 축약.
class PaginationItemsTest {

    // 결과를 비교하기 쉬운 문자열로 (숫자는 "n", 생략은 "...").
    private fun tokens(current: Int, total: Int): List<String> =
        paginationItems(current, total).map {
            when (it) {
                is PageItem.Number -> it.page.toString()
                PageItem.Ellipsis -> "..."
            }
        }

    @Test
    fun `5페이지 이하는 생략 없이 전부 나온다`() {
        assertEquals(listOf("1"), tokens(current = 1, total = 1))
        assertEquals(listOf("1", "2", "3", "4", "5"), tokens(current = 3, total = 5))
    }

    @Test
    fun `초과 시 현재가 앞쪽이면 뒤에만 생략이 붙는다`() {
        // [1 2 … 10]  (현재 1: start=2, end=2)
        assertEquals(listOf("1", "2", "...", "10"), tokens(current = 1, total = 10))
    }

    @Test
    fun `초과 시 현재가 가운데면 양쪽에 생략이 붙는다`() {
        // [1 … 4 5 6 … 10]
        assertEquals(listOf("1", "...", "4", "5", "6", "...", "10"), tokens(current = 5, total = 10))
    }

    @Test
    fun `초과 시 현재가 끝쪽이면 앞에만 생략이 붙는다`() {
        // [1 … 9 10]  (현재 10: start=9, end=9)
        assertEquals(listOf("1", "...", "9", "10"), tokens(current = 10, total = 10))
    }

    @Test
    fun `현재가 2면 앞쪽 생략은 없다`() {
        // start=2라 start>2가 아니므로 앞 생략 없음: [1 2 3 … 10]
        assertEquals(listOf("1", "2", "3", "...", "10"), tokens(current = 2, total = 10))
    }
}
