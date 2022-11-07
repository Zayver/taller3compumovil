package com.ntn.taller3.composables.auth

import android.util.Log
import androidx.compose.material.SnackbarDuration
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parse.ParseException
import com.parse.ParseUser
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    fun setEmail(email: String) {
        _email.value = email
    }

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    fun setPassword(password: String) {
        _password.value = password
    }

    suspend fun login()  {
        val result = viewModelScope.async (Dispatchers.IO ) {
            _isLoading.value = true
            withTimeout(2000){
                try {
                    ParseUser.logIn(_email.value, _password.value)
                } catch (e: Exception) {
                    throw e
                } finally {
                    _isLoading.value = false
                }
            }
        }
        result.await()
    }






}