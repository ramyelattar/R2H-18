package com.igniteai.app.feature.anticipation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.igniteai.app.ui.components.EmberParticles
import com.igniteai.app.ui.components.R2H18Card
import com.igniteai.app.ui.theme.AbyssBlack
import com.igniteai.app.ui.theme.CharcoalLight
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.MoltenGold
import com.igniteai.app.ui.theme.TextMuted
import com.igniteai.app.ui.theme.TextSecondary

/**
 * Tease sequence timeline screen.
 *
 * Shows the progression of scheduled tease messages:
 * - Timeline dots (filled = delivered, empty = upcoming)
 * - Latest delivered message displayed prominently
 * - Countdown to next tease
 * - Escalating intensity indicator
 */
@Composable
fun TeaseSequenceScreen(
    state: AnticipationViewModel.TeaseSequenceState,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AbyssBlack),
    ) {
        EmberParticles(particleCount = 10, intensity = 3)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Tease Sequence",
                style = MaterialTheme.typography.headlineLarge,
                color = EmberOrange,
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (!state.isActive && state.messages.isEmpty()) {
                Text(
                    text = "No active tease sequence.\nSchedule one to build anticipation\nthroughout the day.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
            } else {
                // Next tease countdown
                if (state.isActive && state.nextTeaseInMs > 0) {
                    val hours = (state.nextTeaseInMs / 3_600_000).toInt()
                    val minutes = ((state.nextTeaseInMs % 3_600_000) / 60_000).toInt()

                    Text(
                        text = "Next tease in: ${hours}h ${minutes}m",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MoltenGold,
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Timeline
                TimelineDots(messages = state.messages)

                Spacer(modifier = Modifier.height(24.dp))

                // Current message
                val currentMsg = if (state.currentMessageIndex >= 0) {
                    state.messages.getOrNull(state.currentMessageIndex)
                } else null

                if (currentMsg != null) {
                    R2H18Card(glowing = true) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Latest Tease",
                                style = MaterialTheme.typography.labelLarge,
                                color = MoltenGold,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = currentMsg.text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Message history
                state.messages.filter { it.delivered }.reversed().drop(1).forEach { msg ->
                    Text(
                        text = msg.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMuted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    )
                    Divider(color = CharcoalLight)
                }
            }
        }
    }
}

@Composable
private fun TimelineDots(
    messages: List<AnticipationViewModel.TeaseMessage>,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        messages.forEachIndexed { index, msg ->
            Box(
                modifier = Modifier
                    .size(if (msg.delivered) 14.dp else 10.dp)
                    .background(
                        color = if (msg.delivered) EmberOrange else TextMuted.copy(alpha = 0.3f),
                        shape = CircleShape,
                    ),
            )

            // Connecting line between dots
            if (index < messages.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(
                            if (msg.delivered) EmberOrange.copy(alpha = 0.5f)
                            else TextMuted.copy(alpha = 0.2f),
                        ),
                )
            }
        }
    }
}
