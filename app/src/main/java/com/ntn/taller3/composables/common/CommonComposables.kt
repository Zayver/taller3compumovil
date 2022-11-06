package com.ntn.taller3.composables.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

@Composable
fun TitledTextField(title: String, hint: String, value: String, onTextChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        OutlinedTextField(
            value = value,
            onValueChange = onTextChange,
            label = { Text(hint) },
            shape = RoundedCornerShape(20),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun TitledPasswordTextField(title: String, value: String, onTextChange: (String) -> Unit, hint: String) {
    var passwordVisible by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        OutlinedTextField(
            value = value,
            onValueChange = onTextChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(hint) },
            shape = RoundedCornerShape(20),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                // Please provide localized description for accessibility services
                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, description)
                }
            }
        )
    }
}

@Preview
@Composable
private fun Preview() {
    TitledTextField("TitledTextField", "Hint", "", {})
}