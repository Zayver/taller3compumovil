package com.ntn.taller3.composables.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ntn.taller3.composables.auth.LoginScreen
import com.ntn.taller3.composables.auth.SignUpScreen
import com.ntn.taller3.composables.mainscreen.MainScreen

@Composable
fun RootNavGraph(navController: NavHostController, start: String){
    NavHost(navController = navController, startDestination = start){
        composable(Screens.Login.route){
            LoginScreen(navController)
        }
        composable(Screens.SignUp.route){
            SignUpScreen(navController)
        }
        composable(Screens.MainScreen.route){
            MainScreen(navController)
        }
    }
}

sealed class Screens(val route: String){
    object Login: Screens("login")
    object SignUp: Screens("signup")
    object MainScreen: Screens("main_screen")
}
