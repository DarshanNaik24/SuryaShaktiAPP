package com.suryashakti.solarmonitor.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.suryashakti.solarmonitor.ui.screens.*
import com.suryashakti.solarmonitor.ui.theme.SolarYellow
import com.suryashakti.solarmonitor.viewmodel.AuthViewModel
import com.suryashakti.solarmonitor.viewmodel.SolarViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Auth : Screen("auth", "Auth", Icons.Default.Lock)
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Log : Screen("log", "Log", Icons.Default.AddCircle)
    object Report : Screen("report", "Report", Icons.Default.BarChart)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

@Composable
fun NavGraph(authViewModel: AuthViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val solarViewModel: SolarViewModel = hiltViewModel()
    val items = listOf(Screen.Home, Screen.Log, Screen.Report, Screen.Settings)
    
    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                viewModel = authViewModel,
                onAuthenticated = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) { 
            MainScaffold(navController, items) { 
                HomeScreen(
                    viewModel = solarViewModel,
                    onProfileClick = { navController.navigate(Screen.Profile.route) }
                ) 
            }
        }
        composable(Screen.Log.route) { 
            MainScaffold(navController, items) { LogEnergyScreen(solarViewModel) }
        }
        composable(Screen.Report.route) { 
            MainScaffold(navController, items) { ReportScreen(solarViewModel) }
        }
        composable(Screen.Settings.route) { 
            MainScaffold(navController, items) { 
                SettingsScreen(
                    viewModel = solarViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(Screen.Profile.route) {
            MainScaffold(navController, items) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    solarViewModel = solarViewModel,
                    onLogout = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun MainScaffold(
    navController: NavHostController,
    items: List<Screen>,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { screen ->
                    val isSelected = currentRoute == screen.route || 
                        (screen == Screen.Home && currentRoute == Screen.Profile.route)
                        
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SolarYellow,
                            selectedTextColor = SolarYellow
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}
