package com.ntn.taller3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Surface
import androidx.navigation.compose.rememberNavController
import com.ntn.taller3.composables.auth.LoginScreen
import com.ntn.taller3.composables.navigation.RootNavGraph
import com.ntn.taller3.composables.navigation.Screens
import com.ntn.taller3.ui.theme.Taller3Theme
import com.parse.ParseUser

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentUser = ParseUser.getCurrentUser()
        val destination = if(currentUser == null){
            Screens.Login.route
        }else{
            Screens.MainScreen.route
        }
        setContent {
            Taller3Theme {
                val navController = rememberNavController()
                Surface() {
                    RootNavGraph(navController = navController, start = destination)
                }
            }
        }
    }
}