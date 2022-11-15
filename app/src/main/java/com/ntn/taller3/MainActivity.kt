package com.ntn.taller3

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Surface
import androidx.navigation.compose.rememberNavController
import com.ntn.taller3.composables.navigation.RootNavGraph
import com.ntn.taller3.composables.navigation.Screens
import com.ntn.taller3.data.UserNotification
import com.ntn.taller3.ui.theme.Taller3Theme
import com.parse.ParseObject
import com.parse.ParseQuery
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



        val argument = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra("userNotification", UserNotification::class.java)
        } else {
            intent.getParcelableExtra<UserNotification>("userNotification")
        }

        Log.d("Mio", "Argument init activity: $argument")

        setContent {
            Taller3Theme {
                val navController = rememberNavController()
                Surface() {
                    RootNavGraph(navController = navController, start = destination, argument)
                }
            }
        }
    }
    override fun onStop() {
        super.onStop()
        val currentUser = ParseUser.getCurrentUser()
        if(currentUser != null){
            val query = ParseQuery.getQuery<ParseObject>("Users")
            query.whereMatches("username", currentUser.username)
            val task = query.findInBackground()
            task.onSuccess {
                println("Hola")
                it.result.forEach { obj ->
                    obj.deleteInBackground()
                }
            }

        }
    }

}