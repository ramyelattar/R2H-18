package com.igniteai.app.feature.anticipation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.igniteai.app.ui.components.EmberParticles
import com.igniteai.app.ui.components.R2H18Button
import com.igniteai.app.ui.components.R2H18Card
import com.igniteai.app.ui.components.PulsingGlow
import com.igniteai.app.ui.theme.AbyssBlack
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.MoltenGold
import com.igniteai.app.ui.theme.TextMuted
import com.igniteai.app.ui.theme.TextSecondary

/**
 * Countdown lock screen — content visible but locked behind a timer.
 *
 * The "Christmas morning" effect: showing someone what they'll get
 * (blurred preview) and making them wait for it is often more
 * exciting than the reveal itself.
 *
 * As the timer approaches zero:
 * - Ember particles intensify
 * - Pulsing glow accelerates
 * - Countdown digits animate
 * - When unlocked: dramatic reveal → navigate to content
 */
@Composable
fun CountdownLockScreen(
    lockedContent: AnticipationViewModel.LockedContent,
    onUnlocked: (String) -> Unit,
) {
    val isUnlocked = lockedContent.remainingMs <= 0
    val totalSeconds = (lockedContent.remainingMs / 1000).toInt()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    // Intensity increases as unlock approaches (min 2, max 10)
    val particleIntensity = if (lockedContent.unlockTimeMs > 0) {
        val progress = 1f - (lockedContent.remainingMs.toFloat() /
            (lockedContent.unlockTimeMs - System.currentTimeMillis() + lockedContent.remainingMs)
                .coerceAtLeast(1))
        (2 + (progress * 8)).toInt().coerceIn(2, 10)
    } else 5

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AbyssBlack),
    ) {
        EmberParticles(
            particleCount = 10 + particleIntensity * 3,
            intensity = particleIntensity,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (isUnlocked) {
                // Unlocked — show reveal
                PulsingGlow(
                    color = MoltenGold,
                    size = 200.dp,
                    pulseSpeed = 800,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Unlocked",
                    style = MaterialTheme.typography.displayLarge,
                    color = MoltenGold,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(32.dp))

                R2H18Button(
                    text = "Reveal",
                    onClick = { onUnlocked(lockedContent.contentId) },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                // Locked — show countdown and blurred preview
                Text(
                    text = "Something is waiting...",
                    style = MaterialTheme.typography.headlineLarge,
                    color = EmberOrange,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Blurred content preview
                R2H18Card {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .blur(20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = lockedContent.contentItem?.title ?: "Locked Content",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Countdown timer
                Text(
                    text = "Unlocks in",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextMuted,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when {
                        hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
                        minutes > 0 -> "${minutes}m ${seconds}s"
                        else -> "${seconds}s"
                    },
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 48.sp,
                    ),
                    color = EmberOrange,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(16.dp))

                PulsingGlow(
                    color = EmberOrange,
                    size = 100.dp,
                    pulseSpeed = (2000 - particleIntensity * 150).coerceAtLeast(400),
                )
            }
        }
    }
}
