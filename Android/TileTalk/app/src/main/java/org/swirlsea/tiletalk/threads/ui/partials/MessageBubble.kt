package org.swirlsea.tiletalk.threads.ui.partials

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.swirlsea.tiletalk.grid.DecryptedMessage
import java.util.regex.Pattern

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: DecryptedMessage,
    isEditing: Boolean,
    editingText: String,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: (newText: String) -> Unit
) {
    val isOwnMessage = message.canDelete
    val alignment = if (isOwnMessage) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (isOwnMessage) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isOwnMessage) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    var currentEditingText by remember(message.content) { mutableStateOf(message.content) }

    val (link, text) = remember(message.content) {
        parseMessageForLink(message.content)
    }
    val uriHandler = LocalUriHandler.current

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(editingText) {
        if (isEditing) {
            currentEditingText = editingText
        }
    }

    LaunchedEffect(isEditing) {
        if (isEditing) {
            coroutineScope.launch {
                bringIntoViewRequester.bringIntoView()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = LocalConfiguration.current.screenWidthDp.dp * 0.8f)
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.authorUsername,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.7f)
                )
                if (isOwnMessage && !isEditing) {
                    Row {
                        IconButton(onClick = onEditClick, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Edit, "Edit Message", tint = textColor.copy(alpha = 0.7f))
                        }
                        IconButton(onClick = onDeleteClick, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Delete, "Delete Message", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (isEditing) {
                Column(modifier = Modifier.bringIntoViewRequester(bringIntoViewRequester)) {
                    OutlinedTextField(
                        value = currentEditingText,
                        onValueChange = { currentEditingText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Editing message...") }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { onSaveClick(currentEditingText) }) {
                            Icon(Icons.Default.CheckCircle, "Save Changes")
                        }
                        IconButton(onClick = onCancelClick) {
                            Icon(Icons.Default.Close, "Cancel Edit")
                        }
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    link?.let { url ->
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🔗",
                            fontSize = 20.sp,
                            modifier = Modifier.clickable { uriHandler.openUri(url) }
                        )
                    }
                }
            }
        }
    }
}

private fun parseMessageForLink(message: String): Pair<String?, String> {
    val urlPattern = Pattern.compile(
        "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*^@!:/{};']*)",
        Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL
    )
    val matcher = urlPattern.matcher(message)
    return if (matcher.find()) {
        val url = matcher.group(0)?.trim()
        val text = message.replace(url!!, "").trim()
        Pair(url, text)
    } else {
        Pair(null, message)
    }
}