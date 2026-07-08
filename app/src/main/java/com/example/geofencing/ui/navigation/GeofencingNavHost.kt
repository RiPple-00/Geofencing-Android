package com.example.geofencing.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.geofencing.ui.detail.DetailScreen
import com.example.geofencing.ui.home.HomeScreen
import com.example.geofencing.ui.map.MapScreen

// TODO: Login, Loading 화면 구현되면 시작 지점을 Loading -> (로그인 여부에 따라) Login/Main 으로 변경
sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object Home : Screen("home")
    data object Detail : Screen("detail/{geofenceId}") {
        fun createRoute(geofenceId: String) = "detail/$geofenceId"
    }
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
            MapScreen()
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onGeofenceClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) }
            )
        }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("geofenceId") { type = NavType.StringType })
        ) {
            DetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
