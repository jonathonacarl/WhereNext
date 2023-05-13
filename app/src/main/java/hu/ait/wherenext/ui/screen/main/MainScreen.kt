package hu.ait.wherenext.ui.screen.main

import android.Manifest
import android.location.Location
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import hu.ait.wherenext.R
import hu.ait.wherenext.navigation.Screen
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    onWriteNewPostClick: () -> Unit = {},
    locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.factory),
    navController: NavController,
) {
    Scaffold(
        topBar = {
            MainTopBar(
                title = stringResource(R.string.WhereNext),
                navController = navController
            )
        },
        floatingActionButton = {
            MainFloatingActionButton(
                onWriteNewPostClick = onWriteNewPostClick
            )
        }
    ) { contentPadding ->
        // Screen content
        Column(modifier = Modifier.padding(contentPadding)) {

            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()

            val fineLocationPermissionState = rememberPermissionState(
                Manifest.permission.ACCESS_FINE_LOCATION
            )

            var cameraState = rememberCameraPositionState {
                CameraPosition.fromLatLngZoom(
                    com.google.android.gms.maps.model.LatLng(
                        47.0,
                        19.0
                    ), 10f
                )
            }

            var markerPosition = remember {
                listOf(
                    com.google.android.gms.maps.model.LatLng(
                        1.35,
                        103.87
                    )
                ).toMutableStateList()
            }

            var uiSettings by remember {
                mutableStateOf(
                    MapUiSettings(
                        zoomControlsEnabled = true,
                        zoomGesturesEnabled = true
                    )
                )
            }

            var mapProperties by remember {
                mutableStateOf(
                    MapProperties(
                        mapType = MapType.SATELLITE,
                        isTrafficEnabled = true,
                        mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                            context,
                            R.raw.mapconfig
                        )
                    )
                )
            }

            if (fineLocationPermissionState.status.isGranted) {
                Button(onClick = {
                    locationViewModel.getLocationLiveData().startLocationUpdates()
                }) {
                    Text(text = "Start location monitoring")
                }
            } else {
                Column {
                    val permissionText =
                        if (fineLocationPermissionState.status.shouldShowRationale) {
                            "Reconsider giving permission"
                        } else {
                            "Give permission for location"
                        }
                    Text(text = permissionText)
                    Button(onClick = {
                        fineLocationPermissionState.launchPermissionRequest()
                    }) {
                        Text(text = "Request permission")
                    }
                }
            }

            var geocodeText by remember { mutableStateOf("") }

            var isSatellite by remember {
                mutableStateOf(false)
            }

            Switch(
                checked = isSatellite,
                onCheckedChange = {
                    isSatellite = it
                    mapProperties = mapProperties.copy(
                        mapType = if (isSatellite) MapType.SATELLITE else MapType.NORMAL
                    )
                }
            )

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraState,
                uiSettings = uiSettings,
                properties = mapProperties,
                onMapClick = {

                    Log.d("Latitude", it.latitude.toString())
                    Log.d("Longitude", it.longitude.toString())

                    navController.navigate(Screen.WritePin.route + "/${it.latitude.toFloat()}/${it.longitude.toFloat()}")

                    val random = Random(System.currentTimeMillis())
                    val cameraPosition = CameraPosition.Builder()
                        .target(it)
                        .zoom(1f + random.nextInt(5))
                        .tilt(30f + random.nextInt(15))
                        .bearing(-45f + random.nextInt(90))
                        .build()
                    //cameraState.position = cameraPostion
                    coroutineScope.launch {
                        cameraState.animate(
                            CameraUpdateFactory.newCameraPosition(cameraPosition), 3000
                        )
                    }
                }
            ) {

            }
        }
    }
}

@Composable
fun MainFloatingActionButton(
    onWriteNewPostClick: () -> Unit = {},
) {

    FloatingActionButton(
        onClick = {
            onWriteNewPostClick()
        },
        containerColor = MaterialTheme.colorScheme.secondary,
        shape = RoundedCornerShape(16.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Add",
            tint = Color.White,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    title: String,
    navController: NavController,
) {
    TopAppBar(
        title = { Text(title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor =
            MaterialTheme.colorScheme.secondaryContainer
        ),
        actions = {
            IconButton(
                onClick = { navController.navigate(Screen.Messages.route) }
            ) {
                Icon(Icons.Filled.Info, contentDescription = "Info")
            }
        }
    )
}

fun getLocationText(location: Location?): String {
    return """
       Lat: ${location?.latitude}
       Lng: ${location?.longitude}
       Alt: ${location?.altitude}
       Speed: ${location?.speed}
       Accuracy: ${location?.accuracy}
    """.trimIndent()
}
