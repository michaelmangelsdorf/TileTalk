
package org.swirlsea.tiletalk.grid.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun CalloutPopup(
    visible: Boolean,
    text: String,
    onDismiss: () -> Unit
) {
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            alpha.animateTo(1f, animationSpec = tween(300))
            delay(2000) // How long the popup stays visible
            alpha.animateTo(0f, animationSpec = tween(300))
            onDismiss()
        }
    }

    if (alpha.value > 0f) {
        Box(
            modifier = Modifier
                .alpha(alpha.value)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 14.sp
            )
        }
    }
}