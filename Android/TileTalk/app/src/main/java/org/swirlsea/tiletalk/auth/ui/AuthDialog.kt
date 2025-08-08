package org.swirlsea.tiletalk.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthDialog(
    onDismiss: () -> Unit,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit
) {
    var userName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isRegisterMode) "Register" else "Login") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Username") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                    Text(
                        if (isRegisterMode) "Already have an account? Login"
                        else "Don't have an account? Register"
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isRegisterMode) {
                        onRegister(userName, password)
                    } else {
                        onLogin(userName, password)
                    }
                },
                // Disable button if fields are empty
                enabled = userName.isNotBlank() && password.isNotBlank()
            ) {
                Text(if (isRegisterMode) "Register" else "Login")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}