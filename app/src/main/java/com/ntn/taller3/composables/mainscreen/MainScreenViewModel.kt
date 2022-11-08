package com.ntn.taller3.composables.mainscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.parse.ParseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class MainScreenViewModel: ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _userLocation = MutableStateFlow(LatLng(0.0,0.0))
    val userLocation = _userLocation.asStateFlow()


    private val _isOnline = MutableStateFlow(false) // si el usuario está disponible o no
    val isOnline = _isOnline.asStateFlow()



    suspend fun logOut() {
        withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
            _isLoading.value = true
            withTimeout(2000){
                try{
                    ParseUser.logOut()
                }catch (e: Exception){
                    throw e
                }finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun onLocationUpdate(location: LatLng) {
        _userLocation.value = location
    }

}