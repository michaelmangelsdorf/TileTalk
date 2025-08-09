package org.swirlsea.tiletalk.threads.ui.partials

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.swirlsea.tiletalk.DateUtils
import org.swirlsea.tiletalk.data.User
// The fix is here: Create an alias for your data class.
import org.swirlsea.tiletalk.data.Thread as AppThread

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ThreadItem(
    // Use the alias 'AppThread' here to avoid ambiguity.
    thread: AppThread,
    currentUser: User?,
    editingMessageId: Int?,
    editingMessageText: String,
    isAddingComment: Boolean,
    onDeleteThread: () -> Unit,
    onDeleteMessage: () -> Unit,
    onStartEditing: (messageId: Int, currentText: String) -> Unit,
    onCancelEditing: () -> Unit,
    onSaveEditedMessage: (newText: String) -> Unit,
    onStartAddingComment: () -> Unit,
    onCancelAddingComment: () -> Unit,
    onPostComment: (newText: String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val canComment = currentUser != null && thread.messages.none { it.authorId == currentUser.id }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(text = thread.tile.symbol ?: "", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "On ${thread.owner.username}'s grid",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Started by ${thread.starter.username}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = thread.lastMessageSnippet,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = DateUtils.formatTimestamp(thread.lastActivity),
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (currentUser?.id == thread.owner.id) {
                        IconButton(onClick = onDeleteThread) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Thread",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    thread.messages.forEach { message ->
                        MessageBubble(
                            message = message,
                            isEditing = editingMessageId == message.authorId,
                            editingText = editingMessageText,
                            onDeleteClick = onDeleteMessage,
                            onEditClick = { onStartEditing(message.authorId, message.content) },
                            onCancelClick = onCancelEditing,
                            onSaveClick = onSaveEditedMessage
                        )
                    }
                    if (canComment) {
                        Spacer(modifier = Modifier.height(16.dp))
                        if (isAddingComment) {
                            LaunchedEffect(Unit) {
                                coroutineScope.launch {
                                    bringIntoViewRequester.bringIntoView()
                                }
                            }
                            var newCommentText by remember { mutableStateOf("") }
                            OutlinedTextField(
                                value = newCommentText,
                                onValueChange = { newCommentText = it },
                                label = { Text("Add your comment...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bringIntoViewRequester(bringIntoViewRequester),
                                trailingIcon = {
                                    IconButton(
                                        onClick = { onPostComment(newCommentText) },
                                        enabled = newCommentText.isNotBlank()
                                    ) {
                                        Icon(Icons.Default.CheckCircle, "Post Comment")
                                    }
                                }
                            )
                        } else {
                            Button(
                                onClick = onStartAddingComment,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Add Comment")
                            }
                        }
                    }
                }
            }
        }
    }
}