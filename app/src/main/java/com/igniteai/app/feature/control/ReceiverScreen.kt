package com.igniteai.app.feature.control

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
import com.igniteai.app.ui.theme.MoltenGold
import com.igniteai.app.ui.theme.TextMuted
import com.igniteai.app.ui.theme.TextSecondary

/**
 * Receiver screen — controlled entirely by the partner.
 *
 * Four modes:
 * - INSTRUCTIONS: large centered text from controller
 * - COUNTDOWN: dramatic countdown timer
 * - DARKNESS: pure black (safeword button still visible!)
 * - SURPRISE: waiting animation until controller reveals content
 */
@Composable
fun ReceiverScreen(
    uiState: ControlViewModel.ControlUiState,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AbyssBlack),
        contentAlignment = Alignment.Center,
    ) {
        when (uiState.receiverMode) {
            ControlViewModel.ReceiverMode.INSTRUCTIONS -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp),
                ) {
                    Text(
                        text = uiState.displayedCommand.ifBlank { "Waiting for instructions..." },
                        style = MaterialTheme.typography.headlineLarge,
                        color = if (uiState.displayedCommand.isBlank()) TextMuted else EmberOrange,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            ControlViewModel.ReceiverMode.COUNTDOWN -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "${uiState.countdownSeconds}",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 96.sp),
                        color = EmberOrange,
                    )
                    Text(
                        text = "Wait for it...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMuted,
                    )
                }
            }

            ControlViewModel.ReceiverMode.DARKNESS -> {
                // Pure black — nothing visible except the safeword overlay
            }

            ControlViewModel.ReceiverMode.SURPRISE -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PulsingGlow(color = MoltenGold, size = 100.dp, pulseSpeed = 2000)
                    Text(
                        text = "Something is coming...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            }
        }
    }
}
