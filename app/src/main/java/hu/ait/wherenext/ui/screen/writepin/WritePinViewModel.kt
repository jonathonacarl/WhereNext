package hu.ait.wherenext.ui.screen.writepin

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import hu.ait.wherenext.data.PinPost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.*

sealed interface WritePinUiState {
    object Init : WritePinUiState
    object LoadingPostUpload : WritePinUiState
    object PostUploadSuccess : WritePinUiState
    data class ErrorDuringPostUpload(val error: String?) : WritePinUiState

    object LoadingImageUpload : WritePinUiState
    data class ErrorDuringImageUpload(val error: String?) : WritePinUiState
    object ImageUploadSuccess : WritePinUiState
}

class WritePinViewModel: ViewModel() {
    companion object {
        const val COLLECTION_POSTS = "posts"
    }

    var writePinUiState: WritePinUiState by mutableStateOf(WritePinUiState.Init)
    private var auth: FirebaseAuth = Firebase.auth

    fun uploadPinPost(title: String, postBody: String, imgUrl: String = "") {
        writePinUiState = WritePinUiState.LoadingPostUpload

        val myPost = PinPost(
            uid = auth.currentUser!!.uid,
            author = auth.currentUser!!.email!!,
            title = title,
            body = postBody,
            imgUrl = imgUrl
        )

        val postsCollection = FirebaseFirestore.getInstance().collection(COLLECTION_POSTS)

        postsCollection.add(myPost).addOnSuccessListener {
            writePinUiState = WritePinUiState.PostUploadSuccess
        }.addOnFailureListener{
            writePinUiState = WritePinUiState.ErrorDuringPostUpload(it.message)
        }
    }


    @RequiresApi(Build.VERSION_CODES.P)
    fun uploadPinPostImage(
        contentResolver: ContentResolver, imageUri: Uri,
        title: String, postBody: String
    ) {
        viewModelScope.launch {
            writePinUiState = WritePinUiState.LoadingImageUpload

            val source = ImageDecoder.createSource(contentResolver, imageUri)
            val bitmap = ImageDecoder.decodeBitmap(source)

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageInBytes = baos.toByteArray()

            // prepare the empty file in the cloud
            val storageRef = FirebaseStorage.getInstance().reference
            val newImage = withContext(Dispatchers.IO) {
                URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8")
            } + ".jpg"
            val newImagesRef = storageRef.child("images/$newImage")

            // upload the jpeg byte array to the created empty file
            newImagesRef.putBytes(imageInBytes)
                .addOnFailureListener { e ->
                    writePinUiState = WritePinUiState.ErrorDuringImageUpload(e.message)
                }.addOnSuccessListener {
                    writePinUiState = WritePinUiState.ImageUploadSuccess

                    newImagesRef.downloadUrl.addOnCompleteListener { task ->
                        // the public URL of the image is: task.result.toString()
                        uploadPinPost(title, postBody, task.result.toString())
                    }
                }
        }
    }

}