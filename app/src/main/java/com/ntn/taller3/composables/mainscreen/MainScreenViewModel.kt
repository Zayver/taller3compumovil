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
import com.ntn.taller3.data.UserNotification
import com.parse.*
import com.parse.livequery.ParseLiveQueryClient
import com.parse.livequery.SubscriptionHandling
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import kotlin.math.*


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

    private val _isOnline = MutableStateFlow(false) // si el usuario est?? disponible o no
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
                    val image = username?.let { it1 -> retrieveUserImage(it1) }
                    if (username != null)
                        _users.value.add(
                            UserDetails(
                                username,
                                image,
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
            if (obj.getString("username") == ParseUser.getCurrentUser().username) {
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
                        if (obj.objectId == _otherUser.value.objectID) {
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
            pushNotification()
            viewModelScope.launch {
                _isOnline.value = true
                val obj = ParseObject("Users")
                obj.put("username", ParseUser.getCurrentUser().username)
                obj.put("latitude", _userLocation.value.latitude)
                obj.put("longitude", _userLocation.value.longitude)
                obj.save()
                userObjId = obj.objectId

            }
            pushAvailableNotification()
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
        val uploadTask = storageRef.child("images/" + _otherUser.value.username + ".jpg").getBytes(
            1024.0.pow(50.0).toLong()
        )
        uploadTask.addOnSuccessListener {
            _otherUserUri.value = BitmapFactory.decodeByteArray(it, 0, it.size)

            Log.e("Frebase", "Image Retrieve success: ${localFile.toUri()}")
        }.addOnFailureListener {
            Log.e("Frebase", "Image Retrive fail")
            //       mProgressDialog.dismiss()
        }
    }

    private fun retrieveUserImage(user: String): Bitmap? {
        val localFile = File.createTempFile("images", "jpg")
        val storageRef = FirebaseStorage.getInstance().reference
        val uploadTask = storageRef.child("images/" + user + ".jpg").getBytes(
            1024.0.pow(50.0).toLong()
        )
        var bitmap: Bitmap? = null
        uploadTask.addOnSuccessListener {
            bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)

            Log.e("Frebase", "Image Retrieve success: ${localFile.toUri()}")
        }.addOnFailureListener {
            Log.e("Frebase", "Image Retrive fail")
            //       mProgressDialog.dismiss()
        }
        return bitmap
    }

    private fun pushAvailableNotification() {

        val client = OkHttpClient()
        val type = "application/json; charset=utf-8".toMediaType()


        val json: String = mapToJson(
            "Nuevo usuario disponible  \uD83D\uDDFA???\uD83C\uDDE8\uD83C\uDDF4",
            ParseUser.getCurrentUser().username + " ahora esta disponible para seguirlo \uD83E\uDDED \uD83D\uDCCD \uD83D\uDCCD",
            ParseUser.getCurrentUser().username
        )


        val body = RequestBody.create(type, json)
        val request = Request.Builder()
            .url("http://3.80.151.200:1337/parse/push")
            .addHeader("X-Parse-Application-Id", "findit")
            .addHeader("X-Parse-Master-Key", "finditkey")
            .post(body)
            .build()
        try {
            val response = client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Handle this
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.i("response", response.toString())
                }
            })
        } catch (e: IOException) {

        }
    }

    private fun mapToJson(title_message: String, alert_message: String, username: String): String {
        val json = JSONObject()

        val list_channels = JSONArray()
        list_channels.put("AvailableUser")

        json.put("channels", list_channels)

        val data = JSONObject()

        data.put("alert", alert_message)
        data.put("title", title_message)
        data.put("user", username)

        val obj = JSONObject()
        obj.put("username", username)
        obj.put("latitude", _userLocation.value.latitude)
        obj.put("longitude", _userLocation.value.longitude)
        //UserNotification(username, _userLocation.value.latitude, _userLocation.value.longitude)
        data.put("userNotification", obj)
        json.put("data", data)

        Log.i("json", json.toString())
        """
        '{
        "channels": [
        "AvailableUser"
        ],
        "data": {
            "alert": "The Giants won against the Mets 2-3.",
            "tittle": "The Giants won against the Mets 2-3."
        }
    }'
    """
        return json.toString()
    }


    fun calculateDistance(
        initialLat: Double, initialLong: Double,
        finalLat: Double, finalLong: Double
    ): Double {
        var initialLat = initialLat
        var finalLat = finalLat
        val R = 6371 // km (Earth radius)
        val dLat = toRadians(finalLat - initialLat)
        val dLon = toRadians(finalLong - initialLong)
        initialLat = toRadians(initialLat)
        finalLat = toRadians(finalLat)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                sin(dLon / 2) * sin(dLon / 2) * cos(initialLat) * cos(finalLat)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    private fun toRadians(deg: Double): Double {
        return deg * (Math.PI / 180)
    }

    private fun pushNotification() {
        val params: HashMap<String, String?> = HashMap()
        params["deviceId"] = "1234567890"
        params["message"] = "Hola esta es una notificaci??n de pruebaaaaaaa"
        ParseCloud.callFunctionInBackground("sendPush", params,
            FunctionCallback<Any?> { result, e -> Log.d("MIO", "done: $e") })
    }

    fun viewUserNotification(userNotification: UserNotification) {
        onWatchingOtherUser(UserDetails(
            userNotification.username,
            null,
            userNotification.latitude,
            userNotification.longitude,
            ""
        ))
    }
}
