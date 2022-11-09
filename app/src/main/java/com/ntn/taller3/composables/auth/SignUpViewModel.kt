package com.ntn.taller3.composables.auth

import android.R.attr.data
import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parse.ParseFile
import com.parse.ParseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeout


class SignUpViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _expanded = MutableStateFlow(false)
    val expanded = _expanded.asStateFlow()

    fun setExpanded(expanded: Boolean) {
        _expanded.value = expanded
    }

    private val _selectedTypeText = MutableStateFlow("Cédula de ciudadania")
    val selectedTypeText = _selectedTypeText.asStateFlow()

    fun setSelectedTypeText(selectedtype: String) {
        _selectedTypeText.value = selectedtype
    }

    private val _id = MutableStateFlow("")
    val id = _id.asStateFlow()

    fun setId(id: String) {
        _id.value = id
    }

    private val _firstname = MutableStateFlow("")
    val firstname = _firstname.asStateFlow()

    fun setFirstName(firstname: String) {
        _firstname.value = firstname
    }

    private val _lastname = MutableStateFlow("")
    val lastname = _lastname.asStateFlow()

    fun setLastName(lastname: String) {
        _lastname.value = lastname
    }

    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    fun setUserName(username: String) {
        _username.value = username
    }

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

    private val _image = MutableStateFlow<Uri?>(null)
    val image = _image.asStateFlow()

    fun setImage(image: Uri) {
        _image.value = image
    }


    suspend fun signup(contentResolver: ContentResolver) {
        _isLoading.value = true
        val result = viewModelScope.async(Dispatchers.IO) {
            val user = ParseUser()
            user.username = _username.value
            user.setPassword(_password.value)
            user.email = _email.value

            // other fields can be set just like with ParseObject
            user.put("first_name", _firstname.value)
            user.put("last_name", _lastname.value)
            user.put("type_id", _selectedTypeText.value)
            user.put("id_number", _id.value)

            /* TODO DESCOMENTAR SI SE ACTIVA LA SUBIDA PUBLICA DE ARCHIVOS EN EL SERVIDOR
            val bytes = _image.value?.let { uri ->
                contentResolver.openInputStream(uri)?.use { it.buffered().readBytes() }
            }
            val file = ParseFile(bytes, "image/'*") TODO QUITAR COMILLAAAAA
            file.save()
            user.put("image", file)
            */
            withTimeout(3000) {
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


}

