package hu.ait.wherenext.ui.screen.writepin

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import java.io.File

@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WritePinScreen(
    onWritePinSuccess: () -> Unit = {},
    writePinViewModel: WritePinViewModel = viewModel(),
) {
    var postTitle by remember { mutableStateOf("") }
    var postBody by remember { mutableStateOf("") }
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    val context = LocalContext.current
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
            if (imageUri == null) {
                writePinViewModel.uploadPinPost(postTitle, postBody)
            } else {
                writePinViewModel.uploadPinPostImage(
                    context.contentResolver,
                    imageUri!!,
                    postTitle,
                    postBody
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
                        (
                                writePinViewModel.writePinUiState as WritePinUiState.ErrorDuringPostUpload).error
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