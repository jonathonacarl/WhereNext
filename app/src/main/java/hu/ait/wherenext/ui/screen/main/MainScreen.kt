package hu.ait.wherenext.ui.screen.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.res.stringResource
import hu.ait.wherenext.R
import hu.ait.wherenext.data.PinPost

@Composable
fun MainScreen(
    onWriteNewPostClick: () -> Unit = {},
    mainScreenViewModel: MainViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val postListState = mainScreenViewModel.postsList().collectAsState(
        initial = MainScreenUIState.Init)

    Scaffold(
        topBar = { MainTopBar(title = stringResource(R.string.WhereNext)) },
        floatingActionButton = {
            MainFloatingActionButton(
                onWriteNewPostClick = onWriteNewPostClick,
                snackbarHostState = snackbarHostState
            )
        }
    ) { contentPadding ->
        // Screen content
        Column(modifier = Modifier.padding(contentPadding)) {

            if (postListState.value == MainScreenUIState.Init) {
                Text(text = "Initializing..")
            } else if (postListState.value is MainScreenUIState.Success) {
                //Text(text = "Messages number: " +
                //        "${(postListState.value as MainScreenUIState.Success).postList.size}")

                LazyColumn() {
                    items((postListState.value as MainScreenUIState.Success).postList){
                        PinCard(pinPost = it.pinPost,
                            onRemoveItem = {
                                mainScreenViewModel.deletePin(it.pinPostID)
                            },
                            currentUserId = mainScreenViewModel.currentUserId)
                    }
                }
            }

        }
    }
}

@Composable
fun MainFloatingActionButton(
    onWriteNewPostClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()

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

@Composable
fun PinCard(
    pinPost: PinPost,
    onRemoveItem: () -> Unit = {},
    currentUserId: String = ""
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
                        text = pinPost.body,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentUserId.equals(pinPost.uid)) {
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