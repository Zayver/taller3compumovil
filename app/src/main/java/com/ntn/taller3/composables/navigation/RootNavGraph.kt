package com.ntn.taller3.composables.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.ntn.taller3.composables.auth.LoginScreen
import com.ntn.taller3.composables.auth.SignUpScreen
import com.ntn.taller3.composables.mainscreen.MainScreen
import com.ntn.taller3.composables.mainscreen.UsersScreen
import com.ntn.taller3.data.UserNotification

@Composable
fun RootNavGraph(navController: NavHostController, start: String, userNotification: UserNotification?){
    NavHost(navController = navController, startDestination = start){
        composable(Screens.Login.route){
            LoginScreen(navController)
        }
        composable(Screens.SignUp.route){
            SignUpScreen(navController)
        }
        composable(Screens.Users.route){
            UsersScreen()
        }
        composable(Screens.MainScreen.route, deepLinks = listOf(navDeepLink { uriPattern = "main_screen_nofication" })){
            MainScreen(navController, userNotification = userNotification)
        }
    }
}

sealed class Screens(val route: String){
    object Login: Screens("login")
    object SignUp: Screens("signup")
    object Users: Screens("users")
    object MainScreen: Screens("main_screen")
}