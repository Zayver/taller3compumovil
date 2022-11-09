package com.ntn.taller3.composables.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.ntn.taller3.composables.common.DialogBoxLoading
import com.ntn.taller3.composables.common.TitledPasswordTextField
import com.ntn.taller3.composables.common.TitledTextField
import com.ntn.taller3.composables.navigation.Screens
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SignUpScreen(navController: NavController, _viewModel: SignUpViewModel = viewModel()) {
    val firstname by _viewModel.firstname.collectAsState()
    val lastname by _viewModel.lastname.collectAsState()
    val email by _viewModel.email.collectAsState()
    val username by _viewModel.username.collectAsState()
    val password by _viewModel.password.collectAsState()
    val id by _viewModel.id.collectAsState()
    val image by _viewModel.image.collectAsState()
    val expanded by _viewModel.expanded.collectAsState()
    val selectedOptionText by _viewModel.selectedTypeText.collectAsState()
    val options = listOf(
        "Cédula de ciudadania",
        "Registro civil de nacimiento",
        "Tarjeta de identidad",
        "Tarjeta de extranjería"
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract =
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            _viewModel.setImage(uri)
        }
    }

    val context = LocalContext.current

    val scaffoldState = rememberScaffoldState()
    val isLoading by _viewModel.isLoading.collectAsState()
    if (isLoading) {
        DialogBoxLoading()
    }
    Scaffold(modifier = Modifier.padding(20.dp), scaffoldState = scaffoldState) { padding_values ->
        Column(
            modifier = Modifier.padding(padding_values),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Title()
            Spacer(modifier = Modifier.padding(20.dp))
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                TitledTextField(
                    title = "Nombre de usuario",
                    hint = "Nombre de usuario",
                    value = username
                ) {
                    _viewModel.setUserName(
                        it
                    )
                }
                TitledTextField(title = "Nombre", hint = "Nombre", value = firstname) {
                    _viewModel.setFirstName(
                        it
                    )
                }
                TitledTextField(title = "Apellido", hint = "Apellido", value = lastname) {
                    _viewModel.setLastName(
                        it
                    )
                }
                TitledTextField(title = "Email", hint = "Email", value = email) {
                    _viewModel.setEmail(
                        it
                    )
                }

                ExposedDropdownMenuBox(
                    modifier = Modifier.fillMaxWidth(),
                    expanded = expanded,
                    onExpandedChange = {
                        _viewModel.setExpanded(!expanded)
                    }
                ) {
                    TextField(
                        readOnly = true,
                        value = selectedOptionText,
                        onValueChange = { _viewModel.setSelectedTypeText(it) },
                        label = { Text("Tipo de Documento") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expanded
                            )
                        },
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )
                    ExposedDropdownMenu(
                        modifier = Modifier.fillMaxWidth(),
                        expanded = expanded,
                        onDismissRequest = {
                            _viewModel.setExpanded(false)
                        }
                    ) {
                        options.forEach { selectionOption ->
                            DropdownMenuItem(
                                onClick = {
                                    _viewModel.setSelectedTypeText(selectionOption)
                                    _viewModel.setExpanded(false)
                                }
                            ) {
                                Text(text = selectionOption)
                            }
                        }
                    }
                }

                TitledTextField(title = "Número de documento", hint = "Número", value = id) {
                    _viewModel.setId(
                        it
                    )
                }

                Column {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(onClick = { galleryLauncher.launch("image/*") })
                        { Text("Buscar en la galería") }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AsyncImage(model = image, contentDescription = "")
                }

                TitledPasswordTextField(
                    title = "Contraseña",
                    hint = "Contraseña",
                    onTextChange = { _viewModel.setPassword(it) },
                    value = password
                )
                Spacer(modifier = Modifier.weight(1f))


                val coroutineScope = rememberCoroutineScope()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = {
                        coroutineScope.launch {
                            try {
                                _viewModel.signup(context.contentResolver)
                                navController.popBackStack()
                                navController.navigate(Screens.MainScreen.route)
                            } catch (e: Exception) {
                                coroutineScope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar(
                                        message = "Error on login : ${e.message}",
                                        actionLabel = "Dismiss",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    })
                    { Text("Registrarse") }
                }
            }

        }
    }
}

@Composable
private fun Title() {
    Text(text = "Registro", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
}


@Composable
@Preview(showSystemUi = true)
private fun Preview() {
    SignUpScreen(rememberNavController())
}

