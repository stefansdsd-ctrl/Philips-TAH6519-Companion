package com.philips.tah6519companion

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.philips.tah6519companion.ui.screens.DashboardScreen
import com.philips.tah6519companion.ui.screens.EqualizerScreen
import com.philips.tah6519companion.ui.screens.MediaScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") { DashboardScreen(navController) }
        composable("media") { MediaScreen() }
        composable("eq") { EqualizerScreen() }
    }
}
