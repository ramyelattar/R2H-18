package com.igniteai.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.igniteai.app.ui.theme.EmberOrange

/**
 * Pulsing radial glow effect.
 *
 * An animated circle that expands and contracts, with a radial
 * gradient that fades at the edges. Used for:
 * - Heart rate visualization (pulsing with BPM)
 * - Interactive button highlights
 * - Session active indicator
 *
 * @param color Glow color (default: ember orange)
 * @param size Diameter of the glow
 * @param pulseSpeed Milliseconds per full pulse cycle (lower = faster)
 * @param modifier Modifier chain
 */
@Composable
fun PulsingGlow(
    color: Color = EmberOrange,
    size: Dp = 80.dp,
    pulseSpeed: Int = 1500,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseSpeed),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseSpeed),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_alpha"
    )

    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val radius = (this.size.minDimension / 2f) * scale

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = alpha),
                    color.copy(alpha = alpha * 0.3f),
                    Color.Transparent,
                ),
                center = center,
                radius = radius,
            ),
            radius = radius,
            center = center,
        )
    }
}
