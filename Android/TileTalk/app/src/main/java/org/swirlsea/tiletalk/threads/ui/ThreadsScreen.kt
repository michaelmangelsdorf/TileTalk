package org.swirlsea.tiletalk.threads.ui

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.swirlsea.tiletalk.threads.ThreadsUiState
import org.swirlsea.tiletalk.threads.ui.partials.ThreadItem
// Keep the alias here for safety and clarity.
import org.swirlsea.tiletalk.data.Thread as AppThread

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ThreadsScreen(
    onNavigateBack: () -> Unit,
    threadsViewModel: ThreadsViewModel = viewModel(
        factory = ThreadsViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val uiState by threadsViewModel.uiState.collectAsState()
    val isLoading = uiState is ThreadsUiState.Loading

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = { threadsViewModel.loadThreads() }
    )

    LaunchedEffect(uiState) {
        (uiState as? ThreadsUiState.Success)?.scrollToThreadIndex?.let { index ->
            coroutineScope.launch {
                listState.animateScrollToItem(index)
                threadsViewModel.onScrollCompleted()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Threads") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val state = uiState) {
                is ThreadsUiState.Loading -> {
                    // Intentionally empty. The PullRefreshIndicator below handles the visual feedback.
                }
                is ThreadsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message)
                    }
                }
                is ThreadsUiState.Success -> {
                    if (state.threads.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No conversations yet.")
                        }
                    } else {
                        // This Column with imePadding is the key to fixing the keyboard issue.
                        // It adds padding at the bottom of the screen when the keyboard is visible.
                        Column(modifier = Modifier.imePadding()) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                state = listState
                            ) {
                                itemsIndexed(state.threads, key = { _, thread -> thread.tile.id }) { index, thread ->
                                    ThreadItem(
                                        thread = thread,
                                        currentUser = state.currentUser,
                                        editingMessageId = state.editingMessageId,
                                        editingMessageText = state.editingMessageText,
                                        isAddingComment = state.addingCommentToTileId == thread.tile.id,
                                        onDeleteThread = { threadsViewModel.deleteThread(thread.tile.id) },
                                        onDeleteMessage = {
                                            threadsViewModel.deleteMessage(thread.tile.owner_id, thread.tile.x_coord, thread.tile.y_coord)
                                        },
                                        onStartEditing = { msgId, currentText ->
                                            threadsViewModel.startEditingMessage(index, msgId, currentText)
                                        },
                                        onCancelEditing = { threadsViewModel.cancelEditingMessage() },
                                        onSaveEditedMessage = { newText ->
                                            threadsViewModel.saveEditedMessage(thread.tile.owner_id, thread.tile.x_coord, thread.tile.y_coord, newText)
                                        },
                                        onStartAddingComment = {
                                            threadsViewModel.startAddingComment(index, thread.tile.id)
                                        },
                                        onCancelAddingComment = { threadsViewModel.cancelAddingComment() },
                                        onPostComment = { newText ->
                                            threadsViewModel.addComment(thread.owner.id, thread.tile.x_coord, thread.tile.y_coord, newText)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}