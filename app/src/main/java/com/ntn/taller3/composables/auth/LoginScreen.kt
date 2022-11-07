package com.ntn.taller3.composables.auth

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ntn.taller3.composables.common.DialogBoxLoading
import com.ntn.taller3.composables.common.TitledPasswordTextField
import com.ntn.taller3.composables.common.TitledTextField
import kotlinx.coroutines.*

@Composable
fun LoginScreen(_viewmodel: LoginViewModel = viewModel()) {
    val email by _viewmodel.email.collectAsState()
    val password by _viewmodel.password.collectAsState()
    val isLoading by _viewmodel.isLoading.collectAsState()

    val scaffoldState = rememberScaffoldState()
    if (isLoading) {
        DialogBoxLoading()
    }
    Scaffold(modifier = Modifier.padding(20.dp), scaffoldState = scaffoldState) {
        Column(
            modifier = Modifier.padding(it),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Title()
            Spacer(modifier = Modifier.padding(40.dp))
            Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                TitledTextField(
                    title = "Email",
                    hint = "Email",
                    value = email,
                    onTextChange = { _viewmodel.setEmail(it) })
                TitledPasswordTextField(
                    title = "Password",
                    hint = "Email",
                    value = password,
                    onTextChange = { _viewmodel.setPassword(it) })
            }
            Spacer(modifier = Modifier.weight(1f))
            Foot(scaffoldState)
        }
    }
}


@Composable
private fun Title() {
    Text(text = "Taller #3", fontSize = 45.sp, fontWeight = FontWeight.ExtraBold)
}

@Composable
private fun Foot(scaffoldState: ScaffoldState, _viewModel: LoginViewModel = viewModel()) {
    val coroutineScope = rememberCoroutineScope()


    OutlinedButton(
        onClick = {
            coroutineScope.launch{
                try{
                    _viewModel.login()
                }catch (e: Exception){
                    coroutineScope.launch {
                        scaffoldState.snackbarHostState.showSnackbar(
                            message = "Error on login : ${e.message}",
                            actionLabel = "Dismiss",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }

        }

    ) {
        Text("Iniciar Sesión")
    }

}


@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    LoginScreen()
}