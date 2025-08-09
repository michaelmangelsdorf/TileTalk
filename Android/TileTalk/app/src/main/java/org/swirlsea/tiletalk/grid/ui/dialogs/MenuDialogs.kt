package org.swirlsea.tiletalk.grid.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.swirlsea.tiletalk.data.User
import org.swirlsea.tiletalk.grid.DialogState
import java.security.KeyPair

@Composable
fun ConfirmKeyGenerationDialog(
    dialogState: DialogState.ConfirmKeyGeneration,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Encryption Key?") },
        text = {
            Column {
                Text("No local encryption key was found for user '${dialogState.user.username}'.")
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "WARNING: Creating a new key will make any existing messages encrypted with a previous key unreadable forever.",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Do you want to proceed and create a new key?")
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Create Key")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ConfirmRekeyDialog(
    dialogState: DialogState.ConfirmRekey,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generate New Encryption Key?") },
        text = {
            Column {
                Text("An encryption key already exists for user '${dialogState.user.username}'.")
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "WARNING: Creating a new key will make any existing messages encrypted with the previous key unreadable forever.",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Do you want to proceed and create a new key?")
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Generate New Key")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } },
        title = { Text("About TileTalk") },
        text = {
            Column {
                Text("TileTalk is a collaborative grid-based communication app.")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Version 0.0.5", fontWeight = FontWeight.SemiBold)
                Text("© 2025 by SwirlSea", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got It!")
            }
        },
        title = {
            Text("Welcome to TileTalk!")
        },
        text = {
            Column {
                Spacer(Modifier.height(8.dp))
                Text("A quick guide to get you started:")
                Spacer(Modifier.height(8.dp))

                Text(buildAnnotatedString {
                    append("•  ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Create a user:\n")
                    }
                    append("Open the menu (☰) and register a username (no cost or signup).\n")
                })
                Text(buildAnnotatedString {
                    append("•  ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Build Your Grid:\n")
                    }
                    append("Long-press a tile to set its symbol. Tap on a symbol to read and write messages!\n")
                })
                Spacer(Modifier.height(4.dp))


                Spacer(Modifier.height(4.dp))
                Text(buildAnnotatedString {
                    append("•  ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Add contacts:\n")
                    }
                    append(" Connect with others and edit their grids with messages. Add contacts in the ")
                    append("Manage Users menu.")
                })

            }
        }
    )
}

@Composable
fun PrivacyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got It!")
            }
        },
        title = { Text("A Note on Privacy:") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "All messages are end-to-end encrypted.\n\nThis means that your messages are encrypted on your phone with your contact's key, before they are sent to the server.\n\nThe messages are stored in encrypted form on the app's server computer.\n\nThey will only be decrypted once your contact downloads them into their copy of the app.")
            }
        }
    )
}

@Composable
fun AdvancedSettingsDialog(
    loggedInUser: User?,
    encryptionKeyPair: KeyPair?,
    onGenerateNewKey: () -> Unit,
    onExportKeyfile: () -> Unit,
    onImportKeyfile: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Advanced Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onGenerateNewKey,
                    enabled = loggedInUser != null
                ) {
                    Text("Generate Key Pair")
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onExportKeyfile,
                    enabled = loggedInUser != null && encryptionKeyPair != null
                ) {
                    Text("Export Keyfile")
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onImportKeyfile,
                    enabled = loggedInUser != null
                ) {
                    Text("Import Keyfile")
                }
            }
        }
    }
}