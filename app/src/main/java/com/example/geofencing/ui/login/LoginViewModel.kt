package com.example.geofencing.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// 로그인 폼 상태. Idle=초기, Loading=요청중, Success=성공(→ 화면 이동), Error=실패(메시지 표시).
sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun login(id: String, password: String) {
        if (_state.value == LoginUiState.Loading) return
        viewModelScope.launch {
            _state.value = LoginUiState.Loading
            _state.value = auth.login(id, password).fold(
                onSuccess = { LoginUiState.Success },
                onFailure = { LoginUiState.Error(it.message ?: "로그인에 실패했습니다.") }
            )
        }
    }

    // 입력이 바뀌면 에러 문구를 지운다.
    fun clearError() {
        if (_state.value is LoginUiState.Error) _state.value = LoginUiState.Idle
    }
}
