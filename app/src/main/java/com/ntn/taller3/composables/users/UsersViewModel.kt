package com.ntn.taller3.composables.users

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.livequery.ParseLiveQueryClient
import com.parse.livequery.SubscriptionHandling
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class UsersViewModel: ViewModel() {

    private val _users = MutableStateFlow<List<UserDetails>>(emptyList())
    val users = _users.asStateFlow()



    fun getData() {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                onChangeList()
                _users.emit(_users.value)
            }
            liveUsers()
        }
    }

    fun addUser(username:String,latitude:Double,longitude:Double,image:Bitmap): UserDetails {
        return UserDetails(
            image = image,
            username = username,
            latitude = latitude,
            longitude = longitude
        )
    }

    fun onChangeList() {

        val query = ParseQuery<ParseObject>("Users")

        query.findInBackground { objects: List<ParseObject>, e: ParseException? ->
            if (e == null) {

                //Log.d("multiple", "Objects: $objects")
                var users_list:List<UserDetails> = emptyList<UserDetails>()

                for(i in objects!!){

                    val username: String? = i.getString("username")
                    val image: Bitmap? = decodeImage(i.getString("image")!!)
                    val latitude: Double = i.getDouble("latidude")
                    val longitude: Double = i.getDouble("longitude")

                    if (username != null && image != null) {
                            users_list += addUser(username,latitude,longitude,image)
                    }
                }

                _users.value=users_list

            } else {
                Log.e("multiple", "ParseError: ", e)
            }
        }
    }

    fun pushData(){
        //TODO get data of current user
        /*
        val query = ParseQuery.getQuery<ParseObject>("Users")
        var image: String? = ""
        query.getInBackground("cm0jiPc4FQ") { obj, e ->
            if (e == null) {
                image = obj.getString("image")
                Log.i("si","consulto")
            } else {
                // something went wrong
            }
        }
        val userDetails = ParseObject("Users")
        userDetails.put("image", image!!)
        userDetails.put("username", "Sean Plott")
        userDetails.put("latitude", 1.0)
        userDetails.put("longitude",2.0)
        userDetails.saveInBackground()
         */
    }


    fun liveUsers() {

        // Parse.initialize should be called first
        val parseLiveQueryClient: ParseLiveQueryClient = ParseLiveQueryClient.Factory.getClient()

        val  parseQuery = ParseQuery<ParseObject>("Users")

        val subscriptionHandling: SubscriptionHandling<ParseObject> = parseLiveQueryClient.subscribe(parseQuery)

        subscriptionHandling.handleSubscribe {
            //Log.i("live","suscribed")
        }
        subscriptionHandling.handleEvents { query, event, `object` ->
            //Log.i("live","si cambia"+query.toString())
            onChangeList()
        }
        subscriptionHandling.handleError { query, exception ->
            //Log.i("live","no cambia"+exception.toString())
        }

        //parseLiveQueryClient.unsubscribe(parseQuery)
    }

    fun decodeImage(imageString:String): Bitmap? {
        //decode base64 string to image
        val imageBytes = Base64.decode(imageString, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

}