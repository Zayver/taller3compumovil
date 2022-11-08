package com.ntn.taller3.composables.auth

import android.R
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.core.net.toFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parse.ParseUser
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeout
import java.io.ByteArrayOutputStream
import java.io.File


class SignUpViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

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

    private val _image = MutableStateFlow<Uri?>(null)
    val image = _image.asStateFlow()

    fun setImage(image: Uri){
        _image.value = image
    }

    private val _bitmap = MutableStateFlow<Bitmap?>(null)
    val bitmap = _bitmap.asStateFlow()

    fun setBitmap(bitmap: Bitmap){
        _bitmap.value = bitmap
    }

    suspend fun signup() {
        _isLoading.value = true
        val result = viewModelScope.async {
            val user = ParseUser()
            user.username = _username.value
            user.setPassword(_password.value)
            user.email = _email.value

        // other fields can be set just like with ParseObject
        user.put("first_name",_firstname.value)
        user.put("last_name",_lastname.value)
        user.put("type_id", _selectedTypeText.value)
        user.put("id_number", _id.value)

        var img:String? = encodeImage(_bitmap.value)

            if (!img.isNullOrEmpty())
                user.put("image", img)
            withTimeout(2000) {
                try {
                    user.signUp()
                } catch (e: Exception) {
                    throw e
                } finally {
                    _isLoading.value = false
                }
            }
        }
        result.await()
    }

    private fun encodeImage(bm: Bitmap?): String? {
        val baos = ByteArrayOutputStream()
        if (bm != null) {
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        }
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }







}

