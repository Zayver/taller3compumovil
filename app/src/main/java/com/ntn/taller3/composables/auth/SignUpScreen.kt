package com.ntn.taller3.composables.auth

import android.graphics.ImageDecoder
import android.media.Image
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.ButtonDefaults.elevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.ntn.taller3.R
import com.ntn.taller3.composables.common.TitledTextField
import com.ntn.taller3.fileprovider.ComposeFileProvider


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SignUpScreen(_viewModel: SignUpViewModel = viewModel()) {
    val firstname by _viewModel.firstname.collectAsState()
    val lastname by _viewModel.lastname.collectAsState()
    val email by _viewModel.email.collectAsState()
    val username by _viewModel.username.collectAsState()
    val password by _viewModel.password.collectAsState()
    val id by _viewModel.id.collectAsState()
    val image by _viewModel.image.collectAsState()
    val expanded by _viewModel.expanded.collectAsState()
    val selectedOptionText by _viewModel.selectedTypeText.collectAsState()
    val options = listOf("Cédula de ciudadania", "Registro civil de nacimiento", "Tarjeta de identidad", "Tarjeta de extranjería")
    val bitmap = _viewModel.bitmap.collectAsState()

    val galleryLauncher = rememberLauncherForActivityResult(contract =
    ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            _viewModel.setImage(uri)
        }
    }

    val context = LocalContext.current

    Column(modifier = Modifier.padding(25.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Title()
        Spacer(modifier = Modifier.padding(20.dp))
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier.verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            TitledTextField(title = "Nombre de usuario", hint = "Nombre de usuario", value = username) {
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

            Column() {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = {galleryLauncher.launch("image/*")})
                    { Text("Buscar en la galería") }
                }

                Spacer(modifier = Modifier.height(12.dp))

                image?.let {
                    if (Build.VERSION.SDK_INT < 28) {
                        _viewModel.setBitmap(MediaStore.Images
                            .Media.getBitmap(context.contentResolver,it))

                    } else {
                        val source = ImageDecoder
                            .createSource(context.contentResolver, it)
                        _viewModel.setBitmap(ImageDecoder.decodeBitmap(source))
                    }

                    bitmap.value?.let {  btm ->
                        Image(bitmap = btm.asImageBitmap(),
                            contentDescription =null,
                            modifier = Modifier.size(400.dp))
                    }
                }
            }

            TitledTextField(
                title = "Contraseña",
                hint = "Contraseña",
                value = password
            ) { _viewModel.setPassword(it) } //T
            Spacer(modifier = Modifier.weight(1f))
            //
            // Foot()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = {_viewModel.signup()})
                { Text("Registrarse") }
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
    SignUpScreen()
}

