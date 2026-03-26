package com.igniteai.app.feature.heartrate

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.FlameRed
import com.igniteai.app.ui.theme.MoltenGold

/**
 * Animated radial glow that pulses with heart rate.
 *
 * Color mapping:
 * - 60-80 BPM: warm orange (calm)
 * - 80-100 BPM: orange-red (elevated)
 * - 100-120 BPM: deep red (excited)
 * - 120+ BPM: bright red with intense glow (peak)
 */
@Composable
fun HeartRateGlow(
    bpm: Int?,
    size: Dp = 120.dp,
    modifier: Modifier = Modifier,
) {
    val effectiveBpm = bpm ?: 70
    val pulseDurationMs = if (effectiveBpm > 0) (60_000 / effectiveBpm) else 1000

    val color = when {
        effectiveBpm >= 120 -> FlameRed
        effectiveBpm >= 100 -> FlameRed.copy(red = 1f)
        effectiveBpm >= 80 -> EmberOrange
        else -> MoltenGold
    }

    val infiniteTransition = rememberInfiniteTransition(label = "heartbeat")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseDurationMs),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "heart_scale",
    )

    Canvas(
        modifier = modifier.size(size),
    ) {
        val radius = (this.size.minDimension / 2) * scale
        drawCircle(
            color = color.copy(alpha = 0.3f * scale),
            radius = radius * 1.3f,
        )
        drawCircle(
            color = color.copy(alpha = 0.6f * scale),
            radius = radius,
        )
        drawCircle(
            color = color.copy(alpha = 0.9f),
            radius = radius * 0.5f,
        )
    }
}
