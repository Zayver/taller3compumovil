package com.ntn.taller3.composables.mainscreen

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.ntn.taller3.composables.common.DialogBoxLoading
import com.ntn.taller3.services.LocatorViewModel
import com.ntn.taller3.services.Reader
import kotlinx.coroutines.launch
import com.ntn.taller3.composables.navigation.Screens

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainScreen(navController: NavController, _viewModel: MainScreenViewModel = viewModel()) {
    val scaffoldState = rememberScaffoldState()
    val isLoading by _viewModel.isLoading.collectAsState(false)
    if(isLoading){
        DialogBoxLoading()
    }
    RequestLocation(scaffoldState)
    when(_viewModel.uiState.value){
        is UIState.Map -> {
            Scaffold(
                topBar = { TopBar(scaffoldState, navController = navController) },
                scaffoldState = scaffoldState
            ) {
                Map()
            }
        }
        is UIState.ListOnlineUsers -> UsersScreen()
    }

}

@Composable
private fun TopBar(
    scaffoldState: ScaffoldState,
    navController: NavController,
    _viewModel: MainScreenViewModel = viewModel()
) {
    val isOnline by _viewModel.isOnline.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    TopAppBar(title = { Text("Taller #3") },
        actions = {
            IconButton(onClick = { _viewModel.setOnline() }) {
                Icon(Icons.Default.Power, "", tint = if (isOnline) Color.Green else Color.Red)
            }
            IconButton(onClick = {
                _viewModel.onUIStateChange(UIState.ListOnlineUsers)
            }) {
                Icon(Icons.Default.SupervisorAccount, "")
            }
            IconButton(onClick = {
                coroutineScope.launch {
                    try {
                        _viewModel.logOut()
                        navController.popBackStack()
                        navController.navigate(Screens.Login.route)
                    } catch (e: Exception) {
                        launch {
                            scaffoldState.snackbarHostState.showSnackbar(
                                message = "Error on logout: ${e.message}",
                                actionLabel = "Dismiss",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
            }) {
                Icon(Icons.Default.Logout, "")
            }
        }
    )
}

@Composable
private fun Map(
    _viewModel: MainScreenViewModel = viewModel(),
    _locationsViewModel: Reader = viewModel()
) {
    val internalLocations by remember {
        mutableStateOf(_locationsViewModel.internalLocations)
    }
    val cameraPosition = rememberCameraPositionState()
    val userLocation by _viewModel.userLocation.collectAsState()
    val isWatching by _viewModel.isWatching.collectAsState()
    GoogleMap(cameraPositionState = cameraPosition) {
        internalLocations.forEach {
            Marker(
                state = MarkerState(
                    position = LatLng(
                        it.lat,
                        it.lon
                    ),
                ),
                title = it.name
            )
        }

        Marker(
            state = MarkerState(
                position = LatLng(userLocation.latitude, userLocation.longitude)
            ),
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
            title = "Posición del usuario"
        )

        if(isWatching){
            val otherUser by _viewModel.otherUser.collectAsState()
            Marker(
                state = MarkerState(
                    position = LatLng(otherUser.latitude, otherUser.longitude)
                ),
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                title = otherUser.username
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestLocation(
    scaffoldState: ScaffoldState,
    _locator: LocatorViewModel = viewModel(),
    _viewModel: MainScreenViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val settingResultRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode == RESULT_OK)
            Log.d("appDebug", "Accepted")
        else {
            Log.d("appDebug", "Denied")
        }
    }

    val ctx = LocalContext.current
    val accessLocation = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    ) {
        if (it) {
            Log.d("Mio", "Permisos otorgados")
            checkLocationSettings(
                context = ctx,
                onDisabled = { intentSenderRequest ->
                    settingResultRequest.launch(intentSenderRequest)
                },
                onEnabled = {
                }
            )

        } else {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(
                    message = "Permisos no otorgados"
                )
            }
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(
        key1 = lifecycleOwner,
        effect = {
            val observer = LifecycleEventObserver { _, event ->

                if (event == Lifecycle.Event.ON_START) {
                    accessLocation.launchPermissionRequest()
                }

            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    )
    when (PackageManager.PERMISSION_GRANTED) {
        ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) -> {
            val location by _locator.requestLocationUpdates().observeAsState()
            location?.let {
                _viewModel.onLocationUpdate(it)
            }
        }
    }
}

private fun checkLocationSettings(
    context: Context,
    onDisabled: (IntentSenderRequest) -> Unit,
    onEnabled: () -> Unit
) {
    val mLocationRequest = LocationRequest.create()
        .setInterval(1000L)
        .setFastestInterval(500L)
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    val builder = LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest)
    val client = LocationServices.getSettingsClient(context)
    val task = client.checkLocationSettings(builder.build())
    task.addOnSuccessListener {
        Log.d("Mio", "GPS ON")
        onEnabled()
    }
    task.addOnFailureListener {
        val resolvable = ResolvableApiException((it as ApiException).status);
        if (resolvable.statusCode == CommonStatusCodes.RESOLUTION_REQUIRED) {
            val isr = IntentSenderRequest.Builder(resolvable.resolution).build()
            Log.d("Mio", "Prender el GPS")
            onDisabled(isr)
        } else {
            //displayErrorText("Se necesita activar el GPS")
            Log.d("Mio", "No se ha activado el GPS")
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    MainScreen(rememberNavController())
}
