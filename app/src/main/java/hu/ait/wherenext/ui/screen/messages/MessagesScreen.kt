package hu.ait.wherenext.ui.screen.messages

import android.Manifest
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.R
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import hu.ait.wherenext.data.PinPost
import hu.ait.wherenext.navigation.Screen
import hu.ait.wherenext.ui.screen.main.MainFloatingActionButton
import hu.ait.wherenext.ui.screen.main.MainTopBar

@Composable
fun MessagesScreen(
    messagesViewModel: MessagesViewModel = viewModel(),
    navController: NavController, 
    onWriteNewPostClick: () -> Unit = {}
) {
    val postListState = messagesViewModel.postsList().collectAsState(
        initial = MessageScreenUIState.Init
    )

    Scaffold(
        topBar = { MainTopBar(title = stringResource(hu.ait.wherenext.R.string.WhereNext), navController = navController) },
        floatingActionButton = {
            MainFloatingActionButton(
                onWriteNewPostClick = onWriteNewPostClick
            )
        }
    ) { contentPadding ->
        // Screen content
        Column(modifier = Modifier.padding(contentPadding)) {

            if (postListState.value == MessageScreenUIState.Init) {
                Text(text = "Initializing..")
            } else if (postListState.value is MessageScreenUIState.Success) {
                Text(
                    text = "Messages number: " +
                            "${(postListState.value as MessageScreenUIState.Success).postList.size}"
                )
                
                LazyColumn() {
                    items((postListState.value as MessageScreenUIState.Success).postList) {
                        PinCard(pinPost = it.pinPost, 
                            onRemoveItem = {
                                messagesViewModel.deletePin(it.pinPostID)
                            }, 
                            currentUserId = messagesViewModel.currentUserId)
                        
                    }
                }
                
            }
        }
    }
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