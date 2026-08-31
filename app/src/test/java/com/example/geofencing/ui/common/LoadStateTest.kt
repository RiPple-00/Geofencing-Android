package com.example.geofencing.ui.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

// LoadState.map: Success의 데이터만 변환하고 Loading/Error는 그대로 통과하는지 검증.
class LoadStateTest {

    @Test
    fun `Success는 데이터가 변환된다`() {
        val result = LoadState.Success(2).map { it * 10 }
        assertEquals(LoadState.Success(20), result)
    }

    @Test
    fun `Error는 메시지를 유지한 채 통과한다`() {
        val error: LoadState<Int> = LoadState.Error("boom")
        val result = error.map { it * 10 }
        assertEquals(LoadState.Error("boom"), result)
    }

    @Test
    fun `Loading은 그대로 통과한다`() {
        val loading: LoadState<Int> = LoadState.Loading
        val result = loading.map { it * 10 }
        assertSame(LoadState.Loading, result)
    }

    @Test
    fun `Loading과 Error에서는 transform이 호출되지 않는다`() {
        var called = false
        LoadState.Loading.map { called = true }
        (LoadState.Error("e") as LoadState<Int>).map { called = true }
        assertEquals(false, called)
    }
}
