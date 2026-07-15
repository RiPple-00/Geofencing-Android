package com.example.geofencing.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.geofencing.ui.theme.Body13
import com.example.geofencing.ui.theme.GeofencingTheme
import com.example.geofencing.ui.theme.Label13
import com.example.geofencing.ui.theme.extendedColors

// 실패 상태를 알리는 최소 인라인 문구 + 재시도 액션. 디자인이 아직 확정되지 않은 오류 UI를
// 이 컴포넌트 하나로만 표현해서, 나중에 toast/별도 화면 등으로 바뀌어도 호출부(로직)는
// 그대로 두고 이 파일 내부만 교체하면 되게 한다.
@Composable
fun InlineRetryNotice(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryLabel: String = "다시 시도"
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            style = Body13,
            color = MaterialTheme.extendedColors.criticalPrimary
        )
        Text(
            text = retryLabel,
            style = Label13,
            color = MaterialTheme.extendedColors.brandPrimary,
            modifier = Modifier.clickable(onClick = onRetry)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InlineRetryNoticePreview() {
    GeofencingTheme {
        InlineRetryNotice(message = "데이터를 불러오지 못했습니다", onRetry = {})
    }
}
