package com.example.geofencing.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.geofencing.ui.common.LoadState
import com.example.geofencing.ui.theme.extendedColors

// LoadState 하나를 받아 로딩/오류/정상을 분기하는 공용 슬롯.
// 오류 UI는 InlineRetryNotice(재시도 포함) 하나로만 표현 → 디자인 확정 시 그 파일만 교체하면 된다.
@Composable
fun <T> LoadStateContent(
    state: LoadState<T>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    when (state) {
        LoadState.Loading -> LoadingIndicator(modifier)
        is LoadState.Error -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            InlineRetryNotice(
                message = state.message,
                onRetry = onRetry,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
        is LoadState.Success -> content(state.data)
    }
}

// 중앙 정렬 로딩 스피너. 별도 로딩 화면이 없어 최소 형태로만 둔다.
@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.extendedColors.brandPrimary,
            strokeWidth = 3.dp
        )
    }
}
