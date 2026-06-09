package com.training.blueconnect.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.training.blueconnect.uiscreen.client.ClientScreen
import com.training.blueconnect.uiscreen.home.HomeScreen
import com.training.blueconnect.uiscreen.server.ServerScreen

@Composable
fun AppNavGraph() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {

        composable(Screen.Home.route) {

            HomeScreen(
                onClientClick = {
                    navController.navigate(Screen.Client.route)
                },
                onServerClick = {
                    navController.navigate(Screen.Server.route)
                }
            )
        }

        composable(Screen.Client.route) {
            ClientScreen()
        }

        composable(Screen.Server.route) {
            ServerScreen()
        }
    }
}