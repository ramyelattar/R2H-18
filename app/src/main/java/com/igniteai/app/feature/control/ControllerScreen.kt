package com.igniteai.app.feature.control

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.igniteai.app.feature.haptic.HapticPatterns
import com.igniteai.app.ui.components.EmberParticles
import com.igniteai.app.ui.components.IgniteButton
import com.igniteai.app.ui.theme.AbyssBlack
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.FlameRed
import com.igniteai.app.ui.theme.TextMuted
import com.igniteai.app.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ControllerScreen(
    uiState: ControlViewModel.ControlUiState,
    onTriggerHaptic: (HapticPatterns.HapticPattern) -> Unit,
    onSendCommand: (String) -> Unit,
    onCommandTextChange: (String) -> Unit,
    onSetMode: (ControlViewModel.ReceiverMode) -> Unit,
    onSwapRoles: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AbyssBlack),
    ) {
        EmberParticles(particleCount = 8, intensity = 4)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text("Controller", style = MaterialTheme.typography.headlineLarge, color = FlameRed)
            Text("You're in control", style = MaterialTheme.typography.bodyLarge, color = TextMuted)

            Spacer(modifier = Modifier.height(24.dp))

            // Haptic triggers
            Text("Haptic Triggers", style = MaterialTheme.typography.labelLarge, color = EmberOrange)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    "Pulse" to HapticPatterns.GENTLE_PULSE,
                    "Burst" to HapticPatterns.SHARP_BURST,
                    "Wave" to HapticPatterns.SLOW_WAVE,
                    "Heartbeat" to HapticPatterns.HEARTBEAT,
                    "Escalate" to HapticPatterns.ESCALATING,
                ).forEach { (name, pattern) ->
                    IgniteButton(text = name, onClick = { onTriggerHaptic(pattern) })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Text command
            Text("Send Command", style = MaterialTheme.typography.labelLarge, color = EmberOrange)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.commandText,
                onValueChange = onCommandTextChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Type a command...", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextSecondary,
                    unfocusedTextColor = TextSecondary,
                    focusedBorderColor = EmberOrange,
                    unfocusedBorderColor = TextMuted,
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))
            IgniteButton(
                text = "Send",
                onClick = { onSendCommand(uiState.commandText) },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Receiver mode selector
            Text("Receiver Screen", style = MaterialTheme.typography.labelLarge, color = EmberOrange)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ControlViewModel.ReceiverMode.entries.forEach { mode ->
                    IgniteButton(text = mode.name, onClick = { onSetMode(mode) })
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            IgniteButton(text = "Swap Roles", onClick = onSwapRoles, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
