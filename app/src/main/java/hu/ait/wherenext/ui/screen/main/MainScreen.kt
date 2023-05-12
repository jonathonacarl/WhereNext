package hu.ait.wherenext.ui.screen.main

import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import android.Manifest
import android.location.Location
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import hu.ait.wherenext.R
import hu.ait.wherenext.data.PinPost
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random

@Composable
fun MainScreen(
    onWriteNewPostClick: () -> Unit = {},
    mainScreenViewModel: MainViewModel = viewModel(),
) {
    val postListState = mainScreenViewModel.postsList().collectAsState(
        initial = MainScreenUIState.Init
    )

    Scaffold(
        topBar = { MainTopBar(title = stringResource(R.string.WhereNext)) },
        floatingActionButton = {
            MainFloatingActionButton(
                onWriteNewPostClick = onWriteNewPostClick
            )
        }
    ) { contentPadding ->
        // Screen content
        Column(modifier = Modifier.padding(contentPadding)) {

            if (postListState.value == MainScreenUIState.Init) {
                Text(text = "Initializing..")
            } else if (postListState.value is MainScreenUIState.Success) {
                Text(
                    text = "Messages number: " +
                            "${(postListState.value as MainScreenUIState.Success).postList.size}"
                )

                LazyColumn {
                    items((postListState.value as MainScreenUIState.Success).postList) {
                        PinCard(
                            pinPost = it.pinPost,
                            onRemoveItem = {
                                mainScreenViewModel.deletePin(it.pinPostID)
                            },
                            currentUserId = mainScreenViewModel.currentUserId
                        )
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
            contentDescription = "Add",
            tint = Color.White,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(title: String) {
    TopAppBar(
        title = { Text(title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor =
            MaterialTheme.colorScheme.secondaryContainer
        ),
        actions = {
            IconButton(
                onClick = { }
            ) {
                Icon(Icons.Filled.Info, contentDescription = "Info")
            }
        }
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MyMap(
    locationViewModel: LocationViewModel =
        viewModel(factory = LocationViewModel.factory)
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val trackedLocations by locationViewModel.locationsFlow.collectAsState()
    var locationState = locationViewModel.getLocationLiveData().observeAsState()

    val fineLocationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    var cameraState = rememberCameraPositionState {
        CameraPosition.fromLatLngZoom(LatLng(47.0, 19.0), 10f)
    }

    var markerPosition = remember {
        listOf(LatLng(1.35, 103.87)).toMutableStateList()
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

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        if (fineLocationPermissionState.status.isGranted) {
            Button(onClick = {
                locationViewModel.getLocationLiveData().startLocationUpdates()
            }) {
                Text(text = "Start location monitoring")
            }
        } else {
            Column() {
                val permissionText = if (fineLocationPermissionState.status.shouldShowRationale) {
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

        var geocodeText by remember{ mutableStateOf("") }

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
                markerPosition.add(it)

                val random = Random(System.currentTimeMillis())
                val cameraPostion = CameraPosition.Builder()
                    .target(it)
                    .zoom(1f + random.nextInt(5))
                    .tilt(30f + random.nextInt(15))
                    .bearing(-45f + random.nextInt(90))
                    .build()
                //cameraState.position = cameraPostion
                coroutineScope.launch {
                    cameraState.animate(
                        CameraUpdateFactory.newCameraPosition(cameraPostion), 3000
                    )
                }
            }
        ) {
            for (position in markerPosition) {
                Marker(
                    state = MarkerState(position = position),
                    title = "My Marker",
                    snippet = "Marker description loc: ${position.latitude}, ${position.longitude}",
                    draggable = true,
                    onClick = {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            geocoder.getFromLocation(
                                it.position.latitude,
                                it.position.longitude,
                                3,
                                object : Geocoder.GeocodeListener {
                                    override fun onGeocode(addrs: MutableList<Address>) {
                                        val addr =
                                            "${addrs[0].getAddressLine(0)}, ${
                                                addrs[0].getAddressLine(
                                                    1
                                                )
                                            }, ${addrs[0].getAddressLine(2)}"

                                        geocodeText = addr
                                    }

                                    override fun onError(errorMessage: String?) {
                                        geocodeText = errorMessage!!
                                        super.onError(errorMessage)
                                    }
                                })
                        }
                        true
                    }
                )
            }

        }
    }
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


@Composable
fun PinCard(
    pinPost: PinPost,
    onRemoveItem: () -> Unit = {},
    currentUserId: String = "",
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        ),
        modifier = Modifier.padding(5.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = pinPost.title,
                    )
                    Text(
                        text = pinPost.location.toString()
                    )
                    Text(
                        text = pinPost.body,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentUserId == pinPost.uid) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.clickable {
                                onRemoveItem()
                            },
                            tint = Color.Red
                        )
                    }
                }
            }

            if (pinPost.imgUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(pinPost.imgUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(80.dp)
                )
            }
        }
    }
}