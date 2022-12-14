package com.ntn.taller3.composables.mainscreen

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.ntn.taller3.composables.common.DialogBoxLoading
import com.ntn.taller3.composables.navigation.Screens
import com.ntn.taller3.data.UserNotification
import com.ntn.taller3.services.Locator
import com.ntn.taller3.services.LocatorViewModel
import com.ntn.taller3.services.Reader
import kotlinx.coroutines.launch
import org.json.JSONObject


@OptIn(ExperimentalAnimationApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainScreen(navController: NavController, userNotification: UserNotification?,_viewModel: MainScreenViewModel = viewModel()) {
    val scaffoldState = rememberScaffoldState()
    val isLoading by _viewModel.isLoading.collectAsState(false)
    if (isLoading) {
        DialogBoxLoading()
    }
    LaunchedEffect(key1 = Unit){
        if(userNotification != null){
            _viewModel.viewUserNotification(userNotification)
        }
    }
    RequestLocation(scaffoldState)
    val state by _viewModel.uiState
    AnimatedContent(targetState = state) {
        when (it) {
            is UIState.Map -> {
                Scaffold(
                    topBar = { TopBar(scaffoldState, navController = navController) },
                    scaffoldState = scaffoldState
                ) {
                    Map(scaffoldState)
                }
            }
            is UIState.ListOnlineUsers -> UsersScreen()
        }
    }


}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun getDatafromNotification(extras: Bundle?) {
    if (extras != null) {
        // extras.keySet().forEach{Log.i("key",it)}
        val data = extras.getString("com.parse.Data")
        if (data != null) {
            Log.i("data", data)
            val resp = JSONObject(data)
            val alert = resp.getString("alert")
            val user = resp.getString("title")
            val title = resp.getString("user")
            Log.i("user", user)
            Log.i("title", alert)
            Log.i("alert", title)
        }
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
                        _viewModel.setOnline()
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
    scaffoldState: ScaffoldState,
    _viewModel: MainScreenViewModel = viewModel(),
    _locationsViewModel: Reader = viewModel()
) {
    val internalLocations by remember {
        mutableStateOf(_locationsViewModel.internalLocations)
    }


    val context = LocalContext.current
    val activity = context.findActivity()
    val intent = activity?.intent

    //Get data from notification
    val extras: Bundle? = intent?.extras

    getDatafromNotification(extras)


    val userLocation by _viewModel.userLocation.collectAsState()
    val cameraPosition = rememberCameraPositionState() {
        position = CameraPosition.fromLatLngZoom(userLocation, 10f)
    }

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
            title = "Posici??n del usuario"
        )

        if (isWatching) {
            val otherUser by _viewModel.otherUser.collectAsState()
            val uri by _viewModel.otherUserUri.collectAsState()
            LaunchedEffect(key1 = Unit) {
                scaffoldState.snackbarHostState.showSnackbar(
                    message = "La distancia es ${
                        _viewModel.calculateDistance(
                            userLocation.latitude,
                            userLocation.longitude,
                            otherUser.latitude,
                            otherUser.longitude
                        )
                    } kms",
                    actionLabel = "Dismiss",
                    duration = SnackbarDuration.Short
                )
            }
            MarkerInfoWindowContent(
                state = MarkerState(
                    LatLng(otherUser.latitude, otherUser.longitude)
                ),
                title = otherUser.username,
                snippet = "Usuario observado",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
            ) {
                uri?.let { it1 ->
                    Image(
                        bitmap = it1.asImageBitmap(),
                        contentDescription = "",
                        contentScale = ContentScale.Inside,
                        modifier = Modifier.size(44.dp)
                    )
                }


            }
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
    var checkPermissionStatus by remember {
        mutableStateOf(false)
    }
    val coroutineScope = rememberCoroutineScope()
    val settingResultRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode == RESULT_OK) {
            checkPermissionStatus = true
            Log.d("appDebug", "Accepted")
        } else {
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
                    checkPermissionStatus = true
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
                checkPermissionStatus = true
            }
        }
    )
    if (checkPermissionStatus) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                val location by _locator.requestLocationUpdates().observeAsState()
                location?.let {
                    //Log.d("Mio", "GPS UPDATE")
                    _viewModel.onLocationUpdate(it)
                }
            }
            else -> {
                Log.d("Mio", "Permiso Denegado nada que hacer")
            }
        }
    }
}

private fun checkLocationSettings(
    context: Context,
    onDisabled: (IntentSenderRequest) -> Unit,
    onEnabled: () -> Unit
) {
    /*
    val mLocationRequest = LocationRequest.Builder(100L).setMinUpdateIntervalMillis(50L)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)*/
    val mLocationRequest = Locator.locationRequest

    val builder = LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest)
    val client = LocationServices.getSettingsClient(context)
    val task = client.checkLocationSettings(builder.build())
    task.addOnSuccessListener {
        Log.d("Mio", "GPS ON")
        onEnabled()
    }
    task.addOnFailureListener {
        val resolvable = ResolvableApiException((it as ApiException).status)
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
    MainScreen(rememberNavController(), null)
}