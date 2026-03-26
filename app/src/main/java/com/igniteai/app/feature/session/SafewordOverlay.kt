package com.igniteai.app.feature.session

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.igniteai.app.ui.theme.SafewordRed

/**
 * Floating safeword stop button — always visible during sessions.
 *
 * Design decisions:
 * - Bottom-center: easy thumb reach, doesn't obscure content
 * - Bright red: unmissable, even in dim lighting
 * - Subtle pulse: draws attention without being distracting
 * - NO confirmation dialog: one tap = instant stop
 * - High z-index: always on top of any session content
 *
 * This is a SAFETY CRITICAL component. It must:
 * - Always be reachable (never hidden behind dialogs)
 * - Respond within 500ms
 * - Work even during animations, audio, or haptic effects
 */
@Composable
fun SafewordOverlay(
    onSafewordTriggered: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "safeword_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "safeword_scale",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f), // Always on top
        contentAlignment = Alignment.BottomCenter,
    ) {
        FloatingActionButton(
            onClick = onSafewordTriggered,
            modifier = Modifier
                .padding(bottom = 32.dp)
                .size(72.dp)
                .scale(scale),
            shape = CircleShape,
            containerColor = SafewordRed,
            contentColor = Color.White,
        ) {
            Text(
                text = "STOP",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
        }
    }
}
