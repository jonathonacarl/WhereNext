package hu.ait.wherenext.ui.screen.messages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import hu.ait.wherenext.data.PinPost
import hu.ait.wherenext.navigation.Screen
import hu.ait.wherenext.ui.screen.main.MainFloatingActionButton
import hu.ait.wherenext.ui.screen.main.MainTopBar

@Composable
fun MessagesScreen(
    messagesViewModel: MessagesViewModel = viewModel(),
    navController: NavController,
    currentLocationPressed: Boolean = false
) {
    val postListState = messagesViewModel.postsList().collectAsState(
        initial = MessageScreenUIState.Init
    )

    Scaffold(
        topBar = {
            MainTopBar(
                title = stringResource(hu.ait.wherenext.R.string.WhereNext),
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

                LazyColumn {
                    items((postListState.value as MessageScreenUIState.Success).postList) {

                        PinCard(
                            pinPost = it.pinPost,
                            onRemoveItem = {
                                messagesViewModel.deletePin(it.pinPostID)
                            },
                            currentUserId = messagesViewModel.currentUserId,
                            navController = navController
                        )

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
    navController: NavController,
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
        Column {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = pinPost.title,
                    )

                    Text(
                        text = pinPost.body,
                    )

                    Row {

                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "My Location",
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.Main.route + "/${pinPost.location.latitude}/${pinPost.location.longitude}")
                            }
                        )

                        Text(
                            text = pinPost.address

                        )
                    }

                    Text(
                        text = pinPost.author
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