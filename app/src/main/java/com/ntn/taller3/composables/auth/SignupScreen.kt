package com.ntn.taller3.composables.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ntn.taller3.composables.common.TitledTextField

@Composable
fun SignUpScreen() {
    Column(modifier = Modifier.padding(25.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Title()
        Spacer(modifier = Modifier.padding(20.dp))
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier.verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            TitledTextField(title = "Nombre", hint = "Nombre", value = "", onTextChange = {})
            TitledTextField(title = "Apellido", hint = "Apellido", value = "", onTextChange = {})
            TitledTextField(title = "Email", hint = "Email", value = "", onTextChange = {})
            TitledTextField(
                title = "Contraseña",
                hint = "Contraseña",
                value = "",
                onTextChange = {}) //TODO CAMBIAR A CONTRASEÑA
            // TODO IMAGEN RESTO CONTACTOS
        }
        Spacer(modifier = Modifier.weight(1f))
        Foot()
    }
}

@Composable
private fun Title() {
    Text(text = "Registro", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
}

@Composable
private fun Foot() {
    OutlinedButton(onClick = { /*TODO*/ }) {
        Text(text = "Registrarse")
    }
}

@Composable
@Preview(showSystemUi = true)
private fun Preview() {
    SignUpScreen()
}