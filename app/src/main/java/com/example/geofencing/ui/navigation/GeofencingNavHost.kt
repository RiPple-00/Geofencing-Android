package com.example.geofencing.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.geofencing.ui.home.HomeScreen
import com.example.geofencing.ui.wholesector.SampleSectorNames
import com.example.geofencing.ui.wholesector.sampleWholeSectorState

// TODO: Login, Loading 화면 구현되면 시작 지점을 Loading -> (로그인 여부에 따라) Login/Main 으로 변경
sealed class Screen(val route: String) {
    data object Main : Screen("main")
}

@Composable
fun GeofencingNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route,
        modifier = modifier
    ) {
        composable(Screen.Main.route) {
            // TODO: ViewModel에서 실제 데이터 주입. 지금은 임시 샘플로 새 UI(HomeScreen) 확인.
            HomeScreen(
                sectorNames = SampleSectorNames,
                wholeSectorState = sampleWholeSectorState()
            )
        }
    }
}
