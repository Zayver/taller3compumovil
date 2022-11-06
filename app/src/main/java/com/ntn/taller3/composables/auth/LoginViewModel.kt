package com.ntn.taller3.composables.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parse.ParseException
import com.parse.ParseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel: ViewModel() {
    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    fun setEmail(email: String) {
        _email.value = email
    }

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    fun setPassword(password: String){
        _password.value = password
    }

    fun login() =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                ParseUser.logIn(_email.value, _password.value)
                println("success")
            }catch (e: ParseException){
                println("Errorrrrrr +")
            }
        }


}