package com.training.blueconnect.navigation

sealed class Screen(val route: String) {

    data object Home : Screen("home")

    data object Client : Screen("client")

    data object Server : Screen("server")
}