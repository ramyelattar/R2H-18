package com.igniteai.app.feature.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.igniteai.app.ui.components.EmberParticles
import com.igniteai.app.ui.components.R2H18Button
import com.igniteai.app.ui.theme.AbyssBlack
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.MoltenGold
import com.igniteai.app.ui.theme.SafewordRed
import com.igniteai.app.ui.theme.TextMuted
import com.igniteai.app.ui.theme.TextSecondary

/**
 * Active session screen — the main container during a live session.
 *
 * This is a composition host that layers:
 * 1. Session content (dares, scenarios, etc.) — passed as slot
 * 2. Timer bar at top
 * 3. SafewordOverlay on top (always accessible)
 * 4. CheckInDialog when triggered
 * 5. DenyDelayOverlay when triggered
 *
 * The ViewModel drives which overlays are visible based on state.
 */
@Composable
fun SessionScreen(
    uiState: SessionViewModel.SessionUiState,
    onSafeword: () -> Unit,
    onCheckInContinue: () -> Unit,
    onCheckInEnd: () -> Unit,
    onEndSession: () -> Unit,
) {
    val totalMs = uiState.timeLimitMinutes * 60_000L
    val progress = if (totalMs > 0) {
        1f - (uiState.timeRemainingMs.toFloat() / totalMs)
    } else 0f

    val minutesRemaining = (uiState.timeRemainingMs / 60_000).toInt()
    val secondsRemaining = ((uiState.timeRemainingMs % 60_000) / 1000).toInt()

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AbyssBlack),
        ) {
            EmberParticles(particleCount = 10, intensity = 3)
        }

        // Main session content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            // Timer bar
            Column {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = when {
                        progress > 0.9f -> SafewordRed
                        progress > 0.5f -> MoltenGold
                        else -> EmberOrange
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${minutesRemaining}:${secondsRemaining.toString().padStart(2, '0')} remaining",
                    style = MaterialTheme.typography.bodyLarge,
                    color = when {
                        progress > 0.9f -> SafewordRed
                        else -> TextMuted
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Session content area (placeholder — real content screens plug in here)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Session Active",
                    style = MaterialTheme.typography.headlineLarge,
                    color = EmberOrange,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Content screens will appear here\nduring ${uiState.sessionType} sessions",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(32.dp))

                R2H18Button(
                    text = "End Session",
                    onClick = onEndSession,
                    modifier = Modifier.fillMaxWidth(0.6f),
                )
            }
        }

        // Safeword overlay — ALWAYS visible during active session
        SafewordOverlay(onSafewordTriggered = onSafeword)

        // Check-in dialog
        if (uiState.state == SessionViewModel.SessionState.CHECK_IN) {
            CheckInDialog(
                onContinue = onCheckInContinue,
                onEndSession = onCheckInEnd,
            )
        }

        // Deny & Delay overlay
        if (uiState.state == SessionViewModel.SessionState.DENY_DELAY) {
            DenyDelayOverlay(remainingMs = uiState.denyDelayRemainingMs)
        }
    }
}
