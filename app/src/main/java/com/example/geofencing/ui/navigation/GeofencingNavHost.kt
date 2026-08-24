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
import com.example.geofencing.ui.login.LoginScreen
import com.example.geofencing.ui.wholesector.WholeSectorViewModel

// TODO: Loading 화면 생기면 시작 지점을 Loading -> (로그인 여부에 따라) Login/Main 으로 변경.
sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Main : Screen("main")
}

@Composable
fun GeofencingNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoggedIn = {
                    // 로그인 성공 → Main. 뒤로가기로 로그인 화면에 돌아오지 않도록 스택에서 제거.
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Main.route) {
            // WholeSector 데이터는 ViewModel(→ DashboardRepository, 현재 mock)에서 주입.
            // Sector/Cart ViewModel은 HomeScreen 내부에서 hiltViewModel()로 연결한다.
            val viewModel: WholeSectorViewModel = hiltViewModel()
            val wholeSectorState by viewModel.state.collectAsState()
            HomeScreen(
                wholeSectorState = wholeSectorState,
                onWholeSectorRetry = viewModel::retry
            )
        }
    }
}
