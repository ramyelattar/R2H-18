package com.igniteai.app.feature.session

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.igniteai.app.ui.components.IgniteButton
import com.igniteai.app.ui.components.IgniteCard
import com.igniteai.app.ui.theme.AbyssBlack
import com.igniteai.app.ui.theme.CoolDownBlue
import com.igniteai.app.ui.theme.SafewordRed
import com.igniteai.app.ui.theme.TextMuted
import com.igniteai.app.ui.theme.TextSecondary

/**
 * Post-session cool-down screen.
 *
 * A deliberate contrast to the fire theme — blue/cool tones signal
 * the transition from intensity to tenderness. Shows:
 * - Calming gradient background (blue instead of red/orange)
 * - Breathing animation circle (inhale/exhale visual cue)
 * - Gentle messaging adapted to whether safeword was used
 * - Session summary (duration)
 * - Return to Home button
 */
@Composable
fun CoolDownScreen(
    safewordTriggered: Boolean,
    sessionDurationMinutes: Int,
    onReturnHome: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathe")

    // Breathing circle — expands on "inhale", contracts on "exhale"
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000), // 4s per breath cycle
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breath_scale",
    )

    val backgroundGradient = Brush.linearGradient(
        colors = listOf(
            AbyssBlack,
            CoolDownBlue.copy(alpha = 0.15f),
            AbyssBlack,
        ),
        start = Offset(0f, 0f),
        end = Offset(0f, Float.MAX_VALUE),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Title adapts to safeword status
            Text(
                text = if (safewordTriggered) "You're Safe" else "Take a Moment",
                style = MaterialTheme.typography.headlineLarge,
                color = if (safewordTriggered) SafewordRed else CoolDownBlue,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (safewordTriggered)
                    "The safeword was used. That's exactly what it's for.\nTake all the time you need."
                else
                    "Breathe together. Be present with each other.\nThere's no rush.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Breathing circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(breathScale),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            CoolDownBlue.copy(alpha = 0.2f),
                            shape = MaterialTheme.shapes.extraLarge,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (breathScale > 0.8f) "Inhale" else "Exhale",
                        style = MaterialTheme.typography.bodyLarge,
                        color = CoolDownBlue,
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Session summary
            IgniteCard {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Session Summary",
                        style = MaterialTheme.typography.labelLarge,
                        color = CoolDownBlue,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${sessionDurationMinutes} minutes together",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 20.sp,
                        ),
                        color = TextSecondary,
                    )
                }
            }
        }

        IgniteButton(
            text = "Return Home",
            onClick = onReturnHome,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
