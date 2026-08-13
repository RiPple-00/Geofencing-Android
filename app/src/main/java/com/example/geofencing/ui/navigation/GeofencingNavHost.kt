package com.example.geofencing.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.geofencing.ui.home.HomeScreen
import com.example.geofencing.ui.wholesector.SampleSectorNames
import com.example.geofencing.ui.wholesector.WholeSectorViewModel

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
            // WholeSector 데이터는 ViewModel(→ DashboardRepository, 현재 mock)에서 주입.
            // TODO: Sector/Cart도 ViewModel로 연결(지금은 HomeScreen 내부 샘플 함수 사용).
            val viewModel: WholeSectorViewModel = hiltViewModel()
            val wholeSectorState by viewModel.state.collectAsState()
            HomeScreen(
                sectorNames = SampleSectorNames,
                wholeSectorState = wholeSectorState
            )
        }
    }
}
