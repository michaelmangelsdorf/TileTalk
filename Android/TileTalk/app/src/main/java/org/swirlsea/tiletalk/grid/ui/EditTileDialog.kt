
package org.swirlsea.tiletalk.grid.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.swirlsea.tiletalk.ui.DialogState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTileDialog(
    dialogState: DialogState.EditingTile,
    onDismiss: () -> Unit,
    onSave: (symbol: String, animationType: Int, flip: Boolean, callout: String, title: String) -> Unit,
    onDelete: (tileId: Int) -> Unit,
) {
    var symbol by remember { mutableStateOf(dialogState.currentSymbol ?: "") }
    var animate by remember { mutableStateOf(dialogState.animationType == 1) }
    var flip by remember { mutableStateOf(dialogState.flip) }
    var title by remember { mutableStateOf(dialogState.title ?: "") }
    var callout by remember { mutableStateOf(dialogState.callout ?: "") }

    val scrollState = rememberScrollState()

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
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row with Title and Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (dialogState.canEditSymbol) "Edit Symbol" else "Tile Details",
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


                if (dialogState.canEditSymbol) {

                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier.width(240.dp)
                    ) {
                        OutlinedTextField(
                            value = symbol,
                            onValueChange = {
                                if (it.codePointCount(0, it.length) <= 1) {
                                    symbol = it
                                }
                            },
                            label = { Text("Type an emoji") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 48.sp,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = callout,
                        onValueChange = { callout = it },
                        label = { Text("Type a call-out text") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = animate,
                                onCheckedChange = { animate = it }
                            )
                            Text(
                                text = "Animate",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = flip,
                                onCheckedChange = { flip = it }
                            )
                            Text(
                                text = "Flip",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                } else {

                    Text(
                        text = "Symbol: ${dialogState.currentSymbol ?: "None"}",
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "You can only edit tiles you have created.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dialogState.canDelete && dialogState.tileId != null) {
                        TextButton(
                            onClick = { onDelete(dialogState.tileId) },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(horizontalArrangement = Arrangement.End) {
                        if (dialogState.canEditSymbol) {
                            Button(
                                onClick = {
                                    val animationType = if (animate) 1 else 0
                                    onSave(symbol, animationType, flip, callout, title)
                                }
                            ) {
                                Text("Save")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}