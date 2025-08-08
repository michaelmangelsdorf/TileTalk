package org.swirlsea.tiletalk.grid.ui


import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

data class SymbolAnimationState(
    val offsetX: Float,
    val offsetY: Float,
    val rotationZ: Float
)

@Composable
fun animatedSymbolMotion(animationType: Int): SymbolAnimationState {
    return when (animationType) {
        1 -> {
            val transition = rememberInfiniteTransition(label = "symbolAnimation")
            val easing = FastOutSlowInEasing

            val offsetX by transition.animateFloat(
                initialValue = -5f,
                targetValue = 5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1200, easing = easing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "offsetX"
            )

            val offsetY by transition.animateFloat(
                initialValue = -4f,
                targetValue = 4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1500, easing = easing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "offsetY"
            )

            val rotationZ by transition.animateFloat(
                initialValue = -5f,
                targetValue = 5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1800, easing = easing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "rotationZ"
            )

            SymbolAnimationState(offsetX, offsetY, rotationZ)
        }
        else -> SymbolAnimationState(0f, 0f, 0f)
    }
}