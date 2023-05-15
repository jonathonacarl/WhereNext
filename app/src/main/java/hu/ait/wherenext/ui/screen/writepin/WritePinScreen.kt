package hu.ait.wherenext.ui.screen.writepin

import android.Manifest
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import hu.ait.wherenext.R
import hu.ait.wherenext.data.LatLng
import hu.ait.wherenext.navigation.Screen
import hu.ait.wherenext.ui.screen.main.LocationViewModel
import hu.ait.wherenext.ui.screen.main.MainTopBar
import java.io.File
import java.util.*

@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalPermissionsApi::class)
@Composable

fun WritePinScreen(
    latitude: Double = 0.0,
    longitude: Double = 0.0,
    currLocationPressed : Boolean,
    onWritePinSuccess: () -> Unit = {},
    writePinViewModel: WritePinViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.factory),
    navController: NavController,
) {
    var postTitle by rememberSaveable { mutableStateOf("") }
    var postBody by rememberSaveable { mutableStateOf("") }
    var cityLocation by rememberSaveable { mutableStateOf("") }
    var location by remember { mutableStateOf(LatLng(latitude, longitude)) }
    var currentLocationPressed by rememberSaveable { mutableStateOf(currLocationPressed) }

    var postTitleStateError by rememberSaveable { mutableStateOf(true) }
    var postBodyStateError by rememberSaveable { mutableStateOf(true) }
    var wasMapPressed by rememberSaveable { mutableStateOf(latitude != 0.0 && longitude != 0.0) }

    val locationState = locationViewModel.getLocationLiveData().observeAsState()
    val context = LocalContext.current

    val fineLocationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )

    var hasImage by remember {
        mutableStateOf(false)
    }
    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            hasImage = success
        }
    )

    Scaffold(
        topBar = {
            MainTopBar(
                title = stringResource(R.string.WhereNext),
                navController = navController,
                currentLocationPressed = currentLocationPressed
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(contentPadding)
        ) {
            if (fineLocationPermissionState.status.isGranted) {
                Button(onClick = {
                    locationViewModel.getLocationLiveData().startLocationUpdates()
                }) {
                    Text(text = stringResource(R.string.start_location))
                }
            }

            OutlinedTextField(value = postTitle,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.post_title)) },
                onValueChange = {
                    postTitle = it
                    postTitleStateError = postTitle.isEmpty()
                },
                isError = postTitleStateError,
                trailingIcon = {
                    if (postTitleStateError) {
                        Icon(
                            Icons.Filled.Warning,
                            stringResource(R.string.error),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

            )
            OutlinedTextField(value = postBody,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(R.string.postBody)) },
                onValueChange = {
                    postBody = it
                    postBodyStateError = postBody.isEmpty()
                },
                isError = postBodyStateError,
                trailingIcon = {
                    if (postBodyStateError) {
                        Icon(
                            Icons.Filled.Warning,
                            stringResource(R.string.error),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
            Row {
                OutlinedTextField(value =
                if (currentLocationPressed) {
                    stringResource(R.string.currentLocation)
                } else if (wasMapPressed) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(latitude, longitude, 3)
                        { addresses ->
                            val address: Address = addresses[0]
                            cityLocation = address.getAddressLine(0)
                        }
                    }
                    cityLocation

                } else {
                       cityLocation
                       },
                    enabled = !currentLocationPressed,
                    modifier = Modifier.weight(0.3f),
                    label = { Text(text = stringResource(R.string.location)) },
                    onValueChange = {
                        cityLocation = it
                    },
                    isError = cityLocation.isEmpty() && !currentLocationPressed,
                    trailingIcon = {
                        if (cityLocation.isEmpty() && !currentLocationPressed) {
                            Icon(
                                Icons.Filled.Warning,
                                stringResource(R.string.error),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                Button(onClick = {
                    location = locationState.value?.let { LatLng(it.latitude, it.longitude) }!!
                    currentLocationPressed = !currentLocationPressed
                }, enabled = locationViewModel.firstPositionArrived) {
                    Text(text = stringResource(R.string.currentLocation))
                }
            }


            // permission here...
            if (cameraPermissionState.status.isGranted) {
                Button(onClick = {
                    val uri = ComposeFileProvider.getImageUri(context)
                    imageUri = uri
                    cameraLauncher.launch(uri) // opens the built in camera
                }) {
                    Text(text = stringResource(R.string.photo))
                }
            } else {
                Column {
                    val permissionText = if (cameraPermissionState.status.shouldShowRationale) {
                        stringResource(R.string.reconsider)
                    } else {
                        stringResource(R.string.givePermission)
                    }
                    Text(text = permissionText)
                    Button(onClick = {
                        cameraPermissionState.launchPermissionRequest()
                    }) {
                        Text(text = stringResource(R.string.request))
                    }
                }
            }

            var latLng by remember { mutableStateOf(LatLng()) }
            var addressToAdd by remember { mutableStateOf("") }

            Button(
                onClick = {

                    val geocoder = Geocoder(context, Locale.getDefault())

                    if (cityLocation.isNotEmpty()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            geocoder.getFromLocationName(
                                cityLocation,
                                3
                            ) { addresses ->
                                val address: Address = addresses[0]
                                latLng = LatLng(address.latitude, address.longitude)
                                addressToAdd = address.getAddressLine(0)
                            }
                        }
                    } else {
                        latLng = LatLng(location.latitude, location.longitude)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            geocoder.getFromLocation(latLng.latitude, latLng.longitude, 3)
                            { addresses ->
                                val address: Address = addresses[0]
                                addressToAdd = address.getAddressLine(0)
                            }
                        }
                    }

                    if (imageUri == null && latLng != LatLng(
                            0.0,
                            0.0
                        ) && addressToAdd.isNotEmpty()
                    ) {

                        writePinViewModel.uploadPinPost(
                            title = postTitle,
                            postBody = postBody,
                            location = latLng,
                            address = addressToAdd
                        )
                    } else if (latLng != LatLng(0.0, 0.0) && addressToAdd.isNotEmpty()) {
                        writePinViewModel.uploadPinPostImage(
                            contentResolver = context.contentResolver,
                            imageUri = imageUri!!,
                            title = postTitle,
                            postBody = postBody,
                            location = latLng,
                            address = addressToAdd
                        )
                    }
                },
                enabled = !postTitleStateError && !postBodyStateError && (cityLocation.isNotEmpty() || currentLocationPressed)
            ) {
                Text(text = stringResource(R.string.upload))
            }

            if (hasImage && imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    modifier = Modifier.size(200.dp, 200.dp),
                    contentDescription = stringResource(R.string.selectedImage)
                )
            }

            when (writePinViewModel.writePinUiState) {
                is WritePinUiState.LoadingPostUpload -> CircularProgressIndicator()
                is WritePinUiState.PostUploadSuccess -> {
                    Text(text = stringResource(R.string.uploaded))
                    onWritePinSuccess()
                }
                is WritePinUiState.ErrorDuringPostUpload ->
                    Text(
                        text = "${
                            (writePinViewModel.writePinUiState as WritePinUiState.ErrorDuringPostUpload).error
                        }"
                    )

                is WritePinUiState.LoadingImageUpload -> CircularProgressIndicator()
                is WritePinUiState.ImageUploadSuccess -> {
                    Text(text = stringResource(R.string.imageUploaded))
                }
                is WritePinUiState.ErrorDuringImageUpload -> Text(text = "${(writePinViewModel.writePinUiState as WritePinUiState.ErrorDuringImageUpload).error}")

                else -> {}
            }
        }
    }
}

class ComposeFileProvider : FileProvider(
    R.xml.filepaths
) {
    companion object {
        fun getImageUri(context: Context): Uri {
            val directory = File(context.cacheDir, context.getString(R.string.images))
            directory.mkdirs()
            val file = File.createTempFile(
                context.getString(R.string.selected_image),
                context.getString(R.string.jpg),
                directory,
            )
            val authority = context.packageName + context.getString(R.string.fileprovider)
            return getUriForFile(
                context,
                authority,
                file,
            )
        }
    }
}