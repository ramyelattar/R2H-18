package com.igniteai.app.feature.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.igniteai.app.ui.components.PulsingGlow
import com.igniteai.app.ui.theme.AbyssBlack
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.TextMuted
import com.igniteai.app.ui.theme.TextSecondary

/**
 * Full-screen deny & delay overlay.
 *
 * This is the Anticipation Engine's signature move: pause everything,
 * dim the screen, show a countdown, and let the tension build.
 *
 * While active:
 * - All audio pauses
 * - All haptics pause
 * - Screen dims to near-black with pulsing ember glow
 * - Countdown timer visible in center
 * - Auto-resumes when timer hits zero
 *
 * The user CANNOT skip this — that's the point. The wait IS the feature.
 * (Safeword still works via the always-visible overlay.)
 */
@Composable
fun DenyDelayOverlay(
    remainingMs: Long,
) {
    val seconds = (remainingMs / 1000).toInt()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AbyssBlack.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center,
    ) {
        // Pulsing ember behind the countdown
        PulsingGlow(
            color = EmberOrange,
            size = 200.dp,
            pulseSpeed = 1500,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = "Not yet...",
                style = MaterialTheme.typography.headlineLarge,
                color = EmberOrange,
                textAlign = TextAlign.Center,
            )

            Text(
                text = "$seconds",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 72.sp,
                ),
                color = EmberOrange,
                textAlign = TextAlign.Center,
            )

            Text(
                text = "Let the anticipation build",
                style = MaterialTheme.typography.bodyLarge,
                color = TextMuted,
                textAlign = TextAlign.Center,
            )
        }
    }
}
