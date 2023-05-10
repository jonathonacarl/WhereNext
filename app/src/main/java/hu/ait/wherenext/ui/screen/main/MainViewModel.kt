package hu.ait.wherenext.ui.screen.main

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import hu.ait.wherenext.data.PinPost
import hu.ait.wherenext.data.PinPostWithID
import hu.ait.wherenext.ui.screen.writepin.WritePinViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow


sealed interface MainScreenUIState {
    object Init : MainScreenUIState

    data class Success(val postList: List<PinPostWithID>) : MainScreenUIState
    data class Error(val error: String?) : MainScreenUIState
}

class MainViewModel : ViewModel() {

    var currentUserId: String = Firebase.auth.currentUser!!.uid


    fun postsList() = callbackFlow {
        val snapshotListener =
            FirebaseFirestore.getInstance().collection(WritePinViewModel.COLLECTION_POSTS)
                .addSnapshotListener { snapshot, e ->
                    val response = if (snapshot != null) {
                        val postList = snapshot.toObjects(PinPost::class.java)
                        val postWithIdList = mutableListOf<PinPostWithID>()

                        postList.forEachIndexed { index, post ->
                            postWithIdList.add(PinPostWithID(snapshot.documents[index].id, post))
                        }

                        MainScreenUIState.Success(
                            postWithIdList
                        )
                    } else {
                        MainScreenUIState.Error(e?.message.toString())
                    }

                    trySend(response) // emit this value through the flow
                }
        awaitClose {
            snapshotListener.remove()
        }
    }

    fun deletePin(postKey: String) {
        FirebaseFirestore.getInstance().collection(
            WritePinViewModel.COLLECTION_POSTS
        ).document(postKey).delete()
    }
}
