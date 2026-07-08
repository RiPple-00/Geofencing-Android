package com.example.geofencing

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.geofencing.ui.navigation.GeofencingNavHost
import com.example.geofencing.ui.theme.GeofencingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 앱이 항상 다크 테마라 상태바/내비게이션바 아이콘도 항상 밝은 색으로 고정
        // (시스템이 라이트 모드면 enableEdgeToEdge() 기본값이 어두운 아이콘을 골라 안 보이는 문제 방지)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            GeofencingTheme {
                // 시스템 인셋을 여기서 소비하지 않고 각 화면(MapScreen 등)이 직접 처리하도록 비움.
                // 그래야 지도/블러 배경이 상태바 뒤까지 실제로 그려짐(edge-to-edge).
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    GeofencingNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
