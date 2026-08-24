package com.example.geofencing.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.geofencing.ui.theme.Body14
import com.example.geofencing.ui.theme.Header24
import com.example.geofencing.ui.theme.Label16
import com.example.geofencing.ui.theme.extendedColors

// 최소 로그인 화면: 아이디 + 비밀번호 + 로그인. 성공하면 onLoggedIn()으로 다음 화면 이동.
@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val colors = MaterialTheme.extendedColors
    val state by viewModel.state.collectAsState()
    var id by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val loading = state is LoginUiState.Loading

    // 성공 시 다음 화면으로.
    LaunchedEffect(state) { if (state is LoginUiState.Success) onLoggedIn() }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary,
        disabledTextColor = colors.textDisabled,
        focusedBorderColor = colors.brandPrimary,
        unfocusedBorderColor = colors.borderDefault,
        focusedLabelColor = colors.textSecondary,
        unfocusedLabelColor = colors.textSecondary,
        cursorColor = colors.brandPrimary
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .imePadding()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Login", style = Header24, color = colors.textPrimary)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = id,
            onValueChange = {
                id = it
                viewModel.clearError()
            },
            label = { Text("아이디") },
            singleLine = true,
            enabled = !loading,
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                viewModel.clearError()
            },
            label = { Text("비밀번호") },
            singleLine = true,
            enabled = !loading,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )

        // 에러 문구(자리는 항상 확보해 레이아웃이 튀지 않게).
        Spacer(Modifier.height(8.dp))
        Text(
            text = (state as? LoginUiState.Error)?.message ?: " ",
            style = Body14,
            color = colors.criticalPrimary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { viewModel.login(id, password) },
            enabled = !loading,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.brandPrimary,
                contentColor = colors.textPrimary,
                disabledContainerColor = colors.fillSecondary,
                disabledContentColor = colors.textDisabled
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (loading) {
                CircularProgressIndicator(
                    color = colors.textPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(text = "로그인", style = Label16)
            }
        }
    }
}
