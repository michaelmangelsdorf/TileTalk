package org.swirlsea.tiletalk.grid.ui.partials

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.swirlsea.tiletalk.data.User

@Composable
fun ControlRow(
    loggedInUser: User?,
    currentPeerUser: User?,
    onSelectPeerClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {

        TextButton(
            onClick = onSelectPeerClick,
            enabled = loggedInUser != null
        ) {
            Text(
                text = if (loggedInUser == null) {
                    "Contact Grid"
                } else if (currentPeerUser != null) {
                    "${currentPeerUser.username}'s grid"
                } else {
                    "Select Contact"
                },
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}