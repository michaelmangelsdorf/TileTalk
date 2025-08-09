package org.swirlsea.tiletalk.grid.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.swirlsea.tiletalk.DateUtils
import org.swirlsea.tiletalk.grid.DialogState


@Composable
fun MessagesDialog(
    dialogState: DialogState.ShowingMessages,
    onDismiss: () -> Unit,
    onDeleteMessage: (ownerId: Int, x: Int, y: Int) -> Unit,
    onAddComment: (message: String) -> Unit,
    onSaveEditedMessage: (message: String) -> Unit,
    onStartEditing: (messageId: Int, currentText: String) -> Unit,
    onCancelEditing: () -> Unit
) {
    var comment by remember { mutableStateOf("") }
    var editedText by remember { mutableStateOf("") }

    LaunchedEffect(dialogState.editingMessageText) {
        editedText = dialogState.editingMessageText
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.End)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close dialog",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (dialogState.messages.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.4f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(dialogState.messages, key = { it.authorId }) { message ->
                            val isEditing = dialogState.editingMessageId == message.authorId
                            val isUndecryptable = message.authorId == -1

                            if (isUndecryptable) {
                                UndecryptableMessageCard(message.content, message.createdAt)
                            } else {
                                MessageCard(
                                    username = message.authorUsername,
                                    timestamp = message.createdAt,
                                    content = if (isEditing) editedText else message.content,
                                    isOwnMessage = message.canDelete,
                                    isEditing = isEditing,
                                    onContentChange = { newText -> editedText = newText },
                                    onEditClick = { onStartEditing(message.authorId, message.content) },
                                    onDeleteClick = { onDeleteMessage(dialogState.tileOwnerId, dialogState.x, dialogState.y) },
                                    onSaveClick = { onSaveEditedMessage(editedText) },
                                    onCancelClick = { onCancelEditing() }
                                )
                            }
                        }
                    }
                }

                if (dialogState.canComment) {
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text(if (dialogState.messages.isEmpty()) "Add the first message" else "Add a comment") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    onAddComment(comment)
                                    comment = ""
                                },
                                enabled = comment.isNotBlank()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Send Comment"
                                )
                            }
                        }
                    )
                }

                if (dialogState.messages.isEmpty() && !dialogState.canComment) {
                    Text(
                        text = "Long-press the tile to add an emoji first, and start a conversation.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun UndecryptableMessageCard(content: String, timestamp: String) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = "Undecryptable Message")
                Spacer(Modifier.width(8.dp))
                Text(
                    DateUtils.formatTimestamp(timestamp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(color = Color.LightGray)
        ) {
            Text(
                text = content,
                modifier = Modifier.padding(16.dp),
                fontStyle = FontStyle.Italic,
                fontSize = 18.sp
            )
        }
    }
}


@Composable
private fun MessageCard(
    username: String,
    timestamp: String,
    content: String,
    isOwnMessage: Boolean,
    isEditing: Boolean,
    onContentChange: (String) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column (modifier= Modifier.padding(12.dp)){
        Row(
            modifier = Modifier
                .padding (horizontal = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    username,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    DateUtils.formatTimestamp(timestamp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isOwnMessage && !isEditing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Message")
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Message", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(color = MaterialTheme.colorScheme.surfaceDim),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (isEditing) {
                    OutlinedTextField(
                        value = content,
                        onValueChange = onContentChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Editing message...") }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onSaveClick, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Save Changes")
                        }
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = onCancelClick, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel Edit")
                        }
                    }
                } else {
                    Text(content, fontSize = 20.sp, lineHeight = 28.sp)
                }
            }
        }
    }
}