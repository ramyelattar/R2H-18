package com.igniteai.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.FlameRed
import com.igniteai.app.ui.theme.MoltenGold
import kotlin.math.sin
import kotlin.random.Random

/**
 * Floating ember particle effect.
 *
 * Small glowing particles drift upward and fade, creating an
 * ambient "fire nearby" atmosphere. Used as background decoration
 * on key screens (home, session start, welcome).
 *
 * @param particleCount Number of particles (performance: keep under 50)
 * @param intensity Controls opacity and speed (1-10)
 * @param modifier Modifier chain
 */
@Composable
fun EmberParticles(
    particleCount: Int = 20,
    intensity: Int = 5,
    modifier: Modifier = Modifier,
) {
    val particles = remember(particleCount) {
        List(particleCount) { EmberParticle.random() }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "embers")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 10000 - (intensity * 500).coerceAtMost(8000),
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ember_time"
    )

    val baseAlpha = (intensity / 10f).coerceIn(0.1f, 0.8f)

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            // Each particle floats upward, drifting slightly sideways
            val y = size.height * (1f - ((time + particle.timeOffset) % 1f))
            val x = particle.xFraction * size.width +
                sin((time + particle.timeOffset) * particle.driftSpeed * 6.28f).toFloat() * 30f

            // Fade out as particles reach the top
            val heightFraction = y / size.height
            val alpha = baseAlpha * heightFraction * particle.alphaMultiplier

            if (alpha > 0.01f) {
                drawCircle(
                    color = particle.color.copy(alpha = alpha),
                    radius = particle.radius,
                    center = Offset(x, y),
                )
            }
        }
    }
}

/**
 * Data class for a single ember particle with randomized properties.
 */
private data class EmberParticle(
    val xFraction: Float,       // Horizontal position (0-1 fraction of width)
    val timeOffset: Float,      // Stagger so particles don't all move together
    val radius: Float,          // Particle size in pixels
    val color: Color,           // Particle color (orange/red/gold)
    val driftSpeed: Float,      // Horizontal sway speed
    val alphaMultiplier: Float, // Per-particle opacity variation
) {
    companion object {
        private val particleColors = listOf(EmberOrange, FlameRed, MoltenGold)

        fun random(): EmberParticle = EmberParticle(
            xFraction = Random.nextFloat(),
            timeOffset = Random.nextFloat(),
            radius = Random.nextFloat() * 3f + 1f,   // 1-4px radius
            color = particleColors.random(),
            driftSpeed = Random.nextFloat() * 2f + 0.5f,
            alphaMultiplier = Random.nextFloat() * 0.5f + 0.5f, // 0.5-1.0
        )
    }
}
