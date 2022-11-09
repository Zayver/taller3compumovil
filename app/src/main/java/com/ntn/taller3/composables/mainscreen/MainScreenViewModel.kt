package com.ntn.taller3.composables.mainscreen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.ntn.taller3.data.UserDetails
import com.parse.*
import com.parse.livequery.ParseLiveQueryClient
import com.parse.livequery.SubscriptionHandling
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout


sealed class UIState {
    object Map : UIState()
    object ListOnlineUsers : UIState()
}

class MainScreenViewModel : ViewModel() {
    private val _uiState = mutableStateOf<UIState>(UIState.Map)
    val uiState: State<UIState>
        get() = _uiState

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _userLocation = MutableStateFlow(LatLng(0.0, 0.0))
    val userLocation = _userLocation.asStateFlow()

    private val _otherUser = MutableStateFlow(UserDetails("", null, 0.0, 0.0, ""))
    val otherUser = _otherUser.asStateFlow()

    private val _users = MutableStateFlow<MutableList<UserDetails>>(mutableListOf())
    val users = _users.asStateFlow()

    private val _isWatching = MutableStateFlow(false)
    val isWatching = _isWatching.asStateFlow()

    private val _isOnline = MutableStateFlow(false) // si el usuario está disponible o no
    val isOnline = _isOnline.asStateFlow()

    var userObjId = ""

    init {
        getOnlineUsers()
    }

    private fun pushNofification(user:String){
        // When users indicate they are Giants fans, we subscribe them to that channel.
        val push = ParsePush()
        push.setChannel("Notifications")
        push.setMessage(user+" Ahora esta disponible.")
        push.sendInBackground()
    }

    private fun getOnlineUsers() {
        val parseLiveQueryClient: ParseLiveQueryClient = ParseLiveQueryClient.Factory.getClient()
        val parseQuery = ParseQuery<ParseObject>("Users")
        val subscriptionHandling: SubscriptionHandling<ParseObject> =
            parseLiveQueryClient.subscribe(parseQuery)

        subscriptionHandling.handleSubscribe {
            Log.i("live", "suscribed")
        }
        subscriptionHandling.handleEvent(SubscriptionHandling.Event.CREATE) { query, obj ->
            run {
                val user = obj.getString("username")
                val latitude = obj.getDouble("latitude")
                val longitude = obj.getDouble("longitude")

                if(user != null)
                    _users.value.add(UserDetails(user,null, latitude, longitude, ""))
            }
        }
        subscriptionHandling.handleError { query, exception ->
            Log.i("live", "no cambia $exception")
        }
    }


    suspend fun logOut() {
        withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
            _isLoading.value = true
            withTimeout(2000) {
                try {
                    ParseUser.logOut()
                } catch (e: Exception) {
                    throw e
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun onUIStateChange(state: UIState) {
        _uiState.value = state
    }

    fun onLocationUpdate(location: LatLng) {
        _userLocation.value = location
        if (_isOnline.value)
            updateBackendLocation()
    }

    private fun updateBackendLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            val obj = ParseObject("Users")
            obj.objectId = userObjId
            obj.put("latitude", _userLocation.value.latitude)
            obj.put("longitude", _userLocation.value.longitude)
            obj.saveInBackground()
        }
    }

    fun setOnline() {
        if (!_isOnline.value) {
            viewModelScope.launch {
                _isOnline.value = true
                val obj = ParseObject("Users")
                val username:String = ParseUser.getCurrentUser().username
                obj.put("username", username)
                obj.put("latitude", _userLocation.value.latitude)
                obj.put("longitude", _userLocation.value.longitude)
                obj.save()
                userObjId = obj.objectId
                pushNofification(username)
            }
        } else {
            viewModelScope.launch {
                val query = ParseQuery.getQuery<ParseObject>("Users")
                query.getInBackground(userObjId) { obj, e ->
                    run {
                        if (e == null) {
                            obj.delete()
                        }
                    }
                }
                _isOnline.value = false
                userObjId = ""
            }
        }
    }

    fun activeCreate() {
        if (_isOnline.value) {

        }
        viewModelScope.launch {
            _isOnline.value = true
            val test = ParseQuery<ParseObject>("Users")

            val obj = ParseObject("Users")
            obj.put("username", ParseUser.getCurrentUser().username)
            obj.put("latitude", _userLocation.value.latitude)
            obj.put("longitude", _userLocation.value.longitude)
            obj.saveInBackground()
            _otherUser.value.objectID = obj.objectId
        }
    }

    private fun decodeImage(imageString: String): Bitmap? {
        //decode base64 string to image
        val imageBytes = Base64.decode(imageString, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun addUser(
        username: String,
        latitude: Double,
        longitude: Double,
        image: Bitmap,
        objectId: String
    ): UserDetails {
        return UserDetails(
            image = image,
            username = username,
            latitude = latitude,
            longitude = longitude,
            objectID = objectId
        )
    }

}