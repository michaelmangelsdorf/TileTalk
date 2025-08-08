package org.swirlsea.tiletalk.contacts.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.swirlsea.tiletalk.data.ContactList
import org.swirlsea.tiletalk.data.User
import org.swirlsea.tiletalk.ui.theme.dark_statusAuthorized
import org.swirlsea.tiletalk.ui.theme.dark_statusPending
import org.swirlsea.tiletalk.ui.theme.light_statusAuthorized
import org.swirlsea.tiletalk.ui.theme.light_statusPending

@Composable
fun ContactsDialog(
    contacts: ContactList,
    resolvedContacts: List<User>,
    resolvedIncoming: List<User>,
    resolvedPending: List<User>,
    onDismiss: () -> Unit,
    onSelectContact: (Int) -> Unit,
    onRequestContact: (String) -> Unit,
    onAcceptContact: (Int) -> Unit,
    onRemoveContact: (Int) -> Unit,
    onRefresh: () -> Unit
) {
    var newContactUsername by remember { mutableStateOf("") }
    val isDarkTheme = isSystemInDarkTheme()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Contacts") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    Divider()
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Refresh")
                        IconButton(onClick = onRefresh) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Contacts")
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
//                    Text(
//                        "My Contacts",
//                        style = MaterialTheme.typography.titleSmall,
//                        modifier = Modifier.padding(vertical = 8.dp)
//                    )
                }

                if (resolvedContacts.isEmpty() && resolvedPending.isEmpty()) {
                    item {
                        Text(
                            "No contacts added yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(resolvedContacts, key = { "authorized-${it.id}" }) { contact ->
                        ContactRow(
                            user = contact,
                            isAuthorized = true,
                            onSelect = { onSelectContact(contact.id) },
                            onRemove = { onRemoveContact(contact.id) }
                        )
                    }
                    items(resolvedPending, key = { "pending-${it.id}" }) { contact ->
                        ContactRow(
                            user = contact,
                            isPending = true,
                            onRemove = { onRemoveContact(contact.id) }
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    Divider()
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Incoming Requests",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (resolvedIncoming.isEmpty()) {
                    item {
                        Text(
                            "No pending incoming requests.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(resolvedIncoming, key = { it.id }) { user ->
                        ContactRow(
                            user = user,
                            isIncoming = true,
                            onAccept = { onAcceptContact(user.id) },
                            onRemove = { onRemoveContact(user.id) }
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(24.dp))
                    Divider()
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Add a New Contact:",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newContactUsername,
                            onValueChange = { newContactUsername = it },
                            label = { Text("Username") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                onRequestContact(newContactUsername)
                                newContactUsername = ""
                            },
                            enabled = newContactUsername.isNotBlank()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Contact")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun ContactRow(
    user: User,
    isAuthorized: Boolean = false,
    isPending: Boolean = false,
    isIncoming: Boolean = false,
    onAccept: () -> Unit = {},
    onRemove: () -> Unit = {},
    onSelect: () -> Unit = {}
) {
    val isDarkTheme = isSystemInDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (isAuthorized) {
            TextButton(onClick = onSelect) { Text(user.username) }
        } else {
            Text(user.username, modifier = Modifier.padding(start = 16.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            when {
                isAuthorized -> {
                    val color = if (isDarkTheme) dark_statusAuthorized else light_statusAuthorized
                    Text("Authorized", style = MaterialTheme.typography.bodySmall, color = color)
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Delete, "Remove Contact", tint = MaterialTheme.colorScheme.error)
                    }
                }
                isPending -> {
                    val color = if (isDarkTheme) dark_statusPending else light_statusPending
                    Text("Pending", style = MaterialTheme.typography.bodySmall, color = color)
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Delete, "Cancel Request", tint = MaterialTheme.colorScheme.error)
                    }
                }
                isIncoming -> {
                    IconButton(onClick = onAccept) {
                        Icon(Icons.Default.Check, "Accept Request", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Close, "Reject Request", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}