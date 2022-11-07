package com.ntn.taller3.composables.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parse.ParseException
import com.parse.ParseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class SignUpViewModel : ViewModel() {


    private val _expanded = MutableStateFlow(false)
    val expanded = _expanded.asStateFlow()

    fun setExpanded(expanded: Boolean){
        _expanded.value = expanded
    }

    private val _selectedTypeText = MutableStateFlow("Cédula de ciudadania")
    val selectedTypeText = _selectedTypeText.asStateFlow()

    fun setSelectedTypeText(selectedtype: String){
        _selectedTypeText.value = selectedtype
    }

    private val _id = MutableStateFlow("")
    val id = _id.asStateFlow()

    fun setId(id: String){
        _id.value = id
    }

    private val _firstname = MutableStateFlow("")
    val firstname = _firstname.asStateFlow()

    fun setFirstName(firstname: String){
        _firstname.value = firstname
    }

    private val _lastname = MutableStateFlow("")
    val lastname = _lastname.asStateFlow()

    fun setLastName(lastname: String){
        _lastname.value = lastname
    }

    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    fun setUserName(username: String){
        _username.value = username
    }

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

    private val _image = MutableStateFlow("")
    val image = _image.asStateFlow()

    fun setImage(image: String){
        _image.value = image
    }

    fun signup(){
        val user = ParseUser()
        user.username = _username.value
        user.setPassword(_password.value)
        user.email = _email.value

        // other fields can be set just like with ParseObject
        user.put("first_name",_firstname.value)
        user.put("last_name",_lastname.value)
        user.put("type_id", _selectedTypeText.value)
        user.put("id_number", _id.value)


        user.signUpInBackground { e ->
            if (e == null) {
                // Hooray! Let them use the app now.
                Log.i("hola","ok")
            } else {
                // Sign up didn't succeed. Look at the ParseException
                // to figure out what went wrong
                Log.i("hola","no ok"+e.toString())
            }
        }


    }




}

