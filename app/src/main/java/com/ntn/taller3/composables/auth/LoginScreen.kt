package com.ntn.taller3.composables.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
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
fun LoginScreen() {
    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Title()
        Spacer(modifier = Modifier.padding(40.dp))
        Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
            TitledTextField(title = "Email", hint = "Email", value = "", onTextChange = {})
            TitledTextField(title = "Password", hint = "Email", value = "", onTextChange = {})
        }
        Spacer(modifier = Modifier.weight(1f))
        Foot()
    }
}


@Composable
private fun Title() {
    Text(text = "Taller #3", fontSize = 45.sp, fontWeight = FontWeight.ExtraBold)
}
@Composable
private fun Foot(){
    OutlinedButton(onClick = { /*TODO*/ }) {
        Text("Iniciar Sesión")
    }
}


@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    LoginScreen()
}