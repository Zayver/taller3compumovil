package com.ntn.taller3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Surface
import androidx.navigation.compose.rememberNavController
import com.ntn.taller3.composables.navigation.RootNavGraph
import com.ntn.taller3.composables.navigation.Screens
import com.ntn.taller3.ui.theme.Taller3Theme
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser
import org.json.JSONObject

class MainActivity : ComponentActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentUser = ParseUser.getCurrentUser()
        val destination = if(currentUser == null){
            Screens.Login.route
        }else{
            Screens.MainScreen.route
        }

       // getPayload()

        setContent {
            Taller3Theme {
                val navController = rememberNavController()
                Surface() {
                    RootNavGraph(navController = navController, start = destination)
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
        //TODO DELETE PARSE OBJECTS IN SERVER
    }

    private fun getPayload(){
        //Get data from notification
        val extras = intent?.extras

        if(extras != null) {
            // extras.keySet().forEach{Log.i("keyyyyyyyyyyyyyyyyyyy",it)}
            val data =extras.getString("com.parse.Data")
            if (data != null) {
                Log.i("data",data)
                val resp:JSONObject = JSONObject(data)
                val alert =resp.getString("alert")
                val user =resp.getString("title")
                val title = resp.getString("user")
                Log.i("user",user)
                Log.i("title",alert)
                Log.i("alert",title)
            }

        }
    }

}