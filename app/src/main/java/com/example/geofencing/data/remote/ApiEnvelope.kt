package com.example.geofencing.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ApiHeader(
    val success: Boolean,
    val resultCode: String,
    val message: String
)

@Serializable
data class ApiResponse<T>(
    val header: ApiHeader,
    val data: T? = null
)

// BE 응답의 header.success == false, 혹은 성공인데 data가 null인 경우 사용.
class ApiException(val resultCode: String, message: String) : Exception(message)

// 모든 API 응답이 공통으로 쓰는 { header, data } 래퍼를 풀어서 data만 꺼낸다.
// 실패거나 data가 없으면 ApiException을 던진다.
fun <T> ApiResponse<T>.unwrap(): T {
    if (!header.success) throw ApiException(header.resultCode, header.message)
    return data ?: throw ApiException(header.resultCode, "data가 없습니다: ${header.message}")
}
