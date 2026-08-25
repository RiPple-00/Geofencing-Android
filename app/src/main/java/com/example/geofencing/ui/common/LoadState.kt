package com.example.geofencing.ui.common

// 화면 데이터의 로딩/성공/실패를 명시적으로 표현.
// 이전엔 UiState? 의 null 하나로 "로딩중"과 "없음/오류"를 뭉뚱그려, 스피너와 오류 문구를 구분할 수 없었다.
// Repository가 API로 바뀌면 Error 가 실제 예외 메시지를 담는다.
sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Success<out T>(val data: T) : LoadState<T>
    data class Error(val message: String) : LoadState<Nothing>
}

// Success의 데이터만 변환한다(Loading/Error는 그대로 통과). Repository의 LoadState<도메인>을
// UI state로 옮길 때 쓴다.
inline fun <T, R> LoadState<T>.map(transform: (T) -> R): LoadState<R> = when (this) {
    is LoadState.Success -> LoadState.Success(transform(data))
    is LoadState.Error -> LoadState.Error(message)
    LoadState.Loading -> LoadState.Loading
}
