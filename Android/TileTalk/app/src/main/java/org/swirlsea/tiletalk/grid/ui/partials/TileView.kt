package org.swirlsea.tiletalk.grid.ui.partials

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.swirlsea.tiletalk.grid.TileUiState
import org.swirlsea.tiletalk.grid.ui.animatedSymbolMotion
import org.swirlsea.tiletalk.parseMessageForLink
import org.swirlsea.tiletalk.ui.theme.dark_symbolCardBackground
import org.swirlsea.tiletalk.ui.theme.dark_symbolCardSelectedBackground
import org.swirlsea.tiletalk.ui.theme.dark_symbolCardSelectedBorder
import org.swirlsea.tiletalk.ui.theme.light_symbolCardBackground
import org.swirlsea.tiletalk.ui.theme.light_symbolCardSelectedBackground
import org.swirlsea.tiletalk.ui.theme.light_symbolCardSelectedBorder
import kotlin.random.Random

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TileView(
    tileState: TileUiState,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    var showCallout by remember { mutableStateOf(false) }

    val highlightBorderColor = if (isDarkTheme) dark_symbolCardSelectedBorder else light_symbolCardSelectedBorder
    val selectedBackgroundColor = if (isDarkTheme) dark_symbolCardSelectedBackground else light_symbolCardSelectedBackground
    val defaultBackgroundColor = if (isDarkTheme) dark_symbolCardBackground else light_symbolCardBackground

    val highlightBorder = if (isSelected) BorderStroke(2.dp, highlightBorderColor) else null
    val backgroundColor = if (isSelected) selectedBackgroundColor else defaultBackgroundColor

    val animation = animatedSymbolMotion(tileState.animationType)

    val (link, parsedText) = remember(tileState.symbol) {
        parseMessageForLink(tileState.symbol ?: "")
    }
    val uriHandler = LocalUriHandler.current

    val symbolFontSize = 48.sp

    LaunchedEffect(tileState.callout) {
        if (!tileState.callout.isNullOrBlank()) {
            while (true) {
                delay(Random.nextLong(5000, 15000)) // Random delay between 5 and 15 seconds
                showCallout = true
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(backgroundColor)
                .then(
                    if (highlightBorder != null) Modifier.border(
                        highlightBorder,
                        RoundedCornerShape(20.dp)
                    )
                    else Modifier
                )
                .combinedClickable(
                    onClick = {
                        onTap()
                    },
                    onLongClick = { onLongPress() }
                ),
            contentAlignment = Alignment.Center
        ) {

            if (tileState.symbol?.isNotBlank() == true) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = parsedText,
                        fontSize = symbolFontSize,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .graphicsLayer(
                                translationX = animation.offsetX,
                                translationY = animation.offsetY,
                                rotationZ = animation.rotationZ,
                                rotationY = if (tileState.flip) 180f else 0f
                            )
                    )
                }
            }

            if (tileState.hasNewMessages) {
                Icon(
                    imageVector = Icons.Filled.MailOutline,
                    contentDescription = "New Messages",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(12.dp)
                )
            }
        }
        if (showCallout && !tileState.callout.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-10).dp, y = (-20).dp)
            ) {
                CalloutPopup(
                    visible = true,
                    text = tileState.callout,
                    onDismiss = { showCallout = false }
                )
            }
        }
    }
}