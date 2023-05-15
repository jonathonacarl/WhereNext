package hu.ait.wherenext.ui.screen.main

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import hu.ait.wherenext.R
import hu.ait.wherenext.navigation.Screen
import hu.ait.wherenext.ui.screen.messages.MessageScreenUIState
import hu.ait.wherenext.ui.screen.messages.MessagesViewModel
import hu.ait.wherenext.ui.screen.messages.PinCard
import java.util.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.factory),
    messagesViewModel: MessagesViewModel = viewModel(),
    navController: NavController,
    latitude: Double,
    longitude: Double,
    zoomPost: Boolean = false
) {

    val postListState = messagesViewModel.postsList().collectAsState(
        initial = MessageScreenUIState.Init
    )

    var displayPost by remember { mutableStateOf("") }

    var showPost by remember { mutableStateOf(false) }

    var currentLocationPressed by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MainTopBar(
                title = stringResource(R.string.WhereNext),
                navController = navController,
                currentLocationPressed = currentLocationPressed
            )
        },
        floatingActionButton = {
            MainFloatingActionButton(
                onWriteNewPostClick = {
                    navController.navigate(Screen.WritePin.route + "/${0.0}/${0.0}/${currentLocationPressed}")
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { contentPadding ->

        // Screen content
        Column(modifier = Modifier.padding(contentPadding)) {

            if (postListState.value == MessageScreenUIState.Init) {
                Text(text = stringResource(R.string.initializing))
            }

            val context = LocalContext.current

            val fineLocationPermissionState = rememberPermissionState(
                Manifest.permission.ACCESS_FINE_LOCATION
            )

            val uiSettings by remember {
                mutableStateOf(
                    MapUiSettings(
                        zoomControlsEnabled = true,
                        zoomGesturesEnabled = true
                    )
                )
            }

//            val cameraPositionState = if (zoomPost) {
//                rememberCameraPositionState {
//                    position = CameraPosition.fromLatLngZoom(LatLng(latitude, longitude), 0f)
//                }
//            } else {
//                rememberCameraPositionState {
//                    position = CameraPosition.fromLatLngZoom(LatLng(latitude, longitude), 2f)
//                }
//            }

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
                    currentLocationPressed = !currentLocationPressed
                }) {
                    Text(text = stringResource(R.string.start_location_monitoring))
                }
            } else {
                Column {
                    val permissionText =
                        if (fineLocationPermissionState.status.shouldShowRationale) {
                            stringResource(R.string.reconsider_giving_permission)
                        } else {
                            stringResource(R.string.give_permission)
                        }
                    Text(text = permissionText)
                    Button(onClick = {
                        fineLocationPermissionState.launchPermissionRequest()
                        currentLocationPressed = !currentLocationPressed
                    }) {
                        Text(text = stringResource(R.string.request_permission))
                    }
                }
            }

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
                uiSettings = uiSettings,
                properties = mapProperties,
                //cameraPositionState = cameraPositionState,
                onMapClick = {
                    navController.navigate(Screen.WritePin.route +
                            "/${it.latitude.toFloat()}/${it.longitude.toFloat()}/${currentLocationPressed}"
                    )
                }
            ) {

                if (postListState.value is MessageScreenUIState.Success) {

                    for (pinPost in (postListState.value as MessageScreenUIState.Success).postList) {

                        Marker(
                            state = rememberMarkerState(
                                key = pinPost.pinPostID,
                                position = LatLng(
                                    pinPost.pinPost.location.latitude,
                                    pinPost.pinPost.location.longitude
                                )

                            ),
                            title = "${pinPost.pinPost.title} from ${pinPost.pinPost.author}",
                            snippet = "Marker Location: ${pinPost.pinPost.address}",
                            draggable = messagesViewModel.currentUserId == pinPost.pinPost.uid,
                            onClick = {
                                displayPost = pinPost.pinPostID
                                showPost = true
                                true
                            },
                            tag = pinPost.pinPostID
                        )

                        if (displayPost == pinPost.pinPostID && showPost) {
                            Dialog(onDismissRequest = { showPost = false }) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight(),
                                    shape = RoundedCornerShape(size = 6.dp)
                                ) {

                                    Column {
                                        PinCard(
                                            pinPost = pinPost.pinPost,
                                            currentUserId = messagesViewModel.currentUserId,
                                            navController = navController
                                        )
                                    }
                                }
                            }

                        }

                    }
                }
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
            contentDescription = stringResource(R.string.add),
            tint = Color.White,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    title: String,
    navController: NavController,
    currentLocationPressed: Boolean
) {
    TopAppBar(
        title = { Text(title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor =
            MaterialTheme.colorScheme.secondaryContainer
        ),
        actions = {

            Row {
                IconButton(
                    onClick = { navController.navigate(Screen.Messages.route + "/${currentLocationPressed}") }
                ) {
                    Icon(Icons.Filled.Info, contentDescription = stringResource(R.string.info))
                }
                IconButton(
                    onClick = { navController.navigate(Screen.Main.route + "/${0.0}/${0.0}") }
                ) {
                    Icon(Icons.Filled.Home, contentDescription = stringResource(R.string.home))
                }
            }
        }
    )
}
