package com.ntn.taller3.composables.mainscreen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.storage.FirebaseStorage
import com.ntn.taller3.data.UserDetails
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser
import com.parse.livequery.ParseLiveQueryClient
import com.parse.livequery.SubscriptionHandling
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import kotlin.math.pow

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

    private val _otherUserUri = MutableStateFlow<Bitmap?>(null)
    val otherUserUri = _otherUserUri.asStateFlow()

    private val _users = MutableStateFlow<MutableList<UserDetails>>(mutableStateListOf())
    val users = _users.asStateFlow()

    private val _isWatching = MutableStateFlow(false)
    val isWatching = _isWatching.asStateFlow()

    private val _isOnline = MutableStateFlow(false) // si el usuario está disponible o no
    val isOnline = _isOnline.asStateFlow()

    private var userObjId = ""

    init {
        getOnlineUsers()
    }

    private fun getOnlineUsers() {
        val parseLiveQueryClient: ParseLiveQueryClient = ParseLiveQueryClient.Factory.getClient()
        val parseQuery = ParseQuery<ParseObject>("Users")
        val subscriptionHandling: SubscriptionHandling<ParseObject> =
            parseLiveQueryClient.subscribe(parseQuery)

        subscriptionHandling.handleSubscribe { query ->
            query.whereNotEqualTo("username", ParseUser.getCurrentUser().username)
            query.findInBackground().onSuccess { listResult ->
                listResult.result.forEach {
                    val username = it.getString("username")
                    val latitude = it.getDouble("latitude")
                    val longitude = it.getDouble("longitude")
                    if (username != null)
                        _users.value.add(
                            UserDetails(
                                username,
                                null,
                                latitude,
                                longitude,
                                it.objectId
                            )
                        )
                }
            }
            Log.d("Mio", "Finished initial query")
        }

        subscriptionHandling.handleEvents { _, event, obj ->
            if(obj.getString("username") == ParseUser.getCurrentUser().username){
                return@handleEvents
            }
            run {
                when (event) {
                    SubscriptionHandling.Event.CREATE -> {
                        obj.getString("username")?.let {
                            UserDetails(
                                it,
                                null,
                                obj.getDouble("latitude"),
                                obj.getDouble("longitude"),
                                obj.objectId
                            )
                        }?.let {
                            _users.value.add(
                                it
                            )
                        }
                        Log.d("Mio", "Recibido objeto creadp: $event")
                    }
                    SubscriptionHandling.Event.UPDATE -> {
                        val res = _users.value.indexOfFirst {
                            it.username == obj.getString("username")
                        }
                        _users.value[res] = _users.value[res].copy(
                            latitude = obj.getDouble("latitude"),
                            longitude = obj.getDouble("longitude")
                        )
                        if (_isWatching.value) {
                            _otherUser.value = _otherUser.value.copy(
                                latitude = obj.getDouble("latitude"),
                                longitude = obj.getDouble("longitude")
                            )
                        }

                        Log.d("Mio", "Actualizado objeto: $event")
                    }
                    SubscriptionHandling.Event.DELETE -> {
                        val i = _users.value.indexOfFirst {
                            it.username == obj.getString("username")
                        }
                        _users.value.removeAt(i)
                        Log.d("Mio", "Para borrar")
                        if(obj.objectId == _otherUser.value.objectID){
                            _isWatching.value = false
                            Log.d("Mio", "Borrado el marcador")
                        }
                        Log.d("Mio", "Eliminado objeto: $event")
                    }
                    else -> {}
                }
            }
        }

        subscriptionHandling.handleError { _, exception ->
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
                obj.put("username", ParseUser.getCurrentUser().username)
                obj.put("latitude", _userLocation.value.latitude)
                obj.put("longitude", _userLocation.value.longitude)
                obj.save()
                userObjId = obj.objectId
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

    fun onWatchingOtherUser(user: UserDetails) {
        _isWatching.value = true
        _otherUser.value = user
        retrieveUserImage()

    }


    private fun retrieveUserImage() {
        val localFile = File.createTempFile("images", "jpg")
        val storageRef = FirebaseStorage.getInstance().reference
        val uploadTask = storageRef.child("images/"+_otherUser.value.username+".jpg").getBytes(
            1024.0.pow(50.0).toLong())
        uploadTask.addOnSuccessListener {
            _otherUserUri.value = BitmapFactory.decodeByteArray(it, 0, it.size)

            Log.e("Frebase", "Image Retrieve success: ${localFile.toUri()}")
        }.addOnFailureListener {
            Log.e("Frebase", "Image Retrive fail")
            //       mProgressDialog.dismiss()
        }
    }

}