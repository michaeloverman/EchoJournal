package com.plcoding.echojournal.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.plcoding.echojournal.echos.presentation.create_echo.CreateEchoRoot
import com.plcoding.echojournal.echos.presentation.echos.EchosRoot
import com.plcoding.echojournal.echos.presentation.util.toCreateEchoRoute

@Composable
fun NavigationRoot(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoute.Echos
    ) {
        composable<NavigationRoute.Echos> {
            EchosRoot(
                onNavigateToCreateEcho = { details ->
                    navController.navigate(details.toCreateEchoRoute())
                }
            )
        }

        composable<NavigationRoute.CreateEcho> {
            CreateEchoRoot()
        }
    }
}