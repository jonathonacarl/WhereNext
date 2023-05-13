package hu.ait.wherenext.ui.screen.writepin

import android.Manifest
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import hu.ait.wherenext.data.LatLng
import hu.ait.wherenext.ui.screen.main.LocationViewModel
import java.io.File
import java.util.*

@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalPermissionsApi::class)
@Composable

fun WritePinScreen(
    latitude: Double = 0.0,
    longitude: Double = 0.0,
    onWritePinSuccess: () -> Unit = {},
    writePinViewModel: WritePinViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.factory),
) {
    var postTitle by remember { mutableStateOf("") }
    var postBody by remember { mutableStateOf("") }
    var cityLocation by remember { mutableStateOf("") }
    var location by remember { mutableStateOf(LatLng(latitude, longitude)) }

    val trackedLocations by locationViewModel.locationsFlow.collectAsState()
    var addressList = mutableListOf<Address>()
    var locationState = locationViewModel.getLocationLiveData().observeAsState()
    val context = LocalContext.current
    var geocoder = Geocoder(context, Locale.getDefault())
    var geocodeText by remember { mutableStateOf("") }

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

    Column(
        modifier = Modifier.padding(20.dp)
    ) {
        if (fineLocationPermissionState.status.isGranted) {
            Button(onClick = {
                locationViewModel.getLocationLiveData().startLocationUpdates()
            }) {
                Text(text = "Start Location Monitoring")
            }
        }

        OutlinedTextField(value = postTitle,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Post title") },
            onValueChange = {
                postTitle = it
            }
        )
        OutlinedTextField(value = postBody,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Post body") },
            onValueChange = {
                postBody = it
            }
        )
        Row {
            OutlinedTextField(value = cityLocation,
                modifier = Modifier.weight(0.3f),
                label = { Text(text = "Location") },
                onValueChange = {
                    cityLocation = it
                })

            // if user allows for location tracking, display this button
            if (fineLocationPermissionState.status.isGranted) {

                Button(onClick = {
                    locationViewModel.getLocationLiveData().startLocationUpdates()
                    Log.d("First", locationState.toString())
                    location = locationState.value?.let { LatLng(it.latitude, it.longitude) }!!
                }) {
                    Text(text = "Current Location")
                }
            }
        }


        // permission here...
        if (cameraPermissionState.status.isGranted) {
            Button(onClick = {
                val uri = ComposeFileProvider.getImageUri(context)
                imageUri = uri
                cameraLauncher.launch(uri) // opens the built in camera
            }) {
                Text(text = "Take photo")
            }
        } else {
            Column {
                val permissionText = if (cameraPermissionState.status.shouldShowRationale) {
                    "Please reconsider giving the camera permission. It is needed if you want to take a photo for the message."
                } else {
                    "Give permission for using photos with items."
                }
                Text(text = permissionText)
                Button(onClick = {
                    cameraPermissionState.launchPermissionRequest()
                }) {
                    Text(text = "Request permission")
                }
            }
        }

        Button(onClick = {

            var locationToEnter = LatLng(0.0, 0.0)

            if (cityLocation.isNotEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocationName(cityLocation,
                        3,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                val address: Address = addresses[0]
                                locationToEnter = LatLng(address.latitude, address.longitude)
                            }

                            override fun onError(errorMessage: String?) {
                                geocodeText = errorMessage!!
                                super.onError(errorMessage)
                            }
                        })
                }
            } else {
                locationToEnter = LatLng(location.latitude, location.longitude)
            }


            if (imageUri == null) {
                writePinViewModel.uploadPinPost(
                    title = postTitle,
                    postBody = postBody,
                    location = locationToEnter
                )
            } else {
                writePinViewModel.uploadPinPostImage(
                    contentResolver = context.contentResolver,
                    imageUri = imageUri!!,
                    title = postTitle,
                    postBody = postBody,
                    location = locationToEnter
                )
            }
        }) {
            Text(text = "Upload")
        }

        if (hasImage && imageUri != null) {
            AsyncImage(
                model = imageUri,
                modifier = Modifier.size(200.dp, 200.dp),
                contentDescription = "selected image"
            )
        }

        when (writePinViewModel.writePinUiState) {
            is WritePinUiState.LoadingPostUpload -> CircularProgressIndicator()
            is WritePinUiState.PostUploadSuccess -> {
                Text(text = "Post uploaded.")
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
                Text(text = "Image uploaded, starting post upload.")
            }
            is WritePinUiState.ErrorDuringImageUpload -> Text(text = "${(writePinViewModel.writePinUiState as WritePinUiState.ErrorDuringImageUpload).error}")

            else -> {}
        }
    }
}

class ComposeFileProvider : FileProvider(
    hu.ait.wherenext.R.xml.filepaths
) {
    companion object {
        fun getImageUri(context: Context): Uri {
            val directory = File(context.cacheDir, "images")
            directory.mkdirs()
            val file = File.createTempFile(
                "selected_image_",
                ".jpg",
                directory,
            )
            val authority = context.packageName + ".fileprovider"
            return getUriForFile(
                context,
                authority,
                file,
            )
        }
    }
}