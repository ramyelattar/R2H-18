package com.igniteai.app.feature.audio

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.igniteai.app.ui.components.EmberParticles
import com.igniteai.app.ui.components.PulsingGlow
import com.igniteai.app.ui.theme.AbyssBlack
import com.igniteai.app.ui.theme.CharcoalDark
import com.igniteai.app.ui.theme.ConnectionActive
import com.igniteai.app.ui.theme.CoolDownBlue
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.TextMuted
import com.igniteai.app.ui.theme.TextSecondary

/**
 * Audio player screen — immersive audio experience during sessions.
 *
 * Layout:
 * - Waveform visualization (animated bars reacting to speech)
 * - Voice gender toggle (Male/Female)
 * - Volume sliders (Voice, Soundscape)
 * - Breath pacer tap zone (large circular area)
 * - Headphone/binaural status indicator
 * - Play/Stop controls
 */
@Composable
fun AudioPlayerScreen(
    uiState: AudioViewModel.AudioUiState,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onToggleGender: () -> Unit,
    onVoiceVolumeChange: (Float) -> Unit,
    onSoundscapeVolumeChange: (Float) -> Unit,
    onBreathTap: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AbyssBlack),
    ) {
        EmberParticles(particleCount = 8, intensity = 2)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // ── Header with headphone status ──────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Audio",
                    style = MaterialTheme.typography.headlineLarge,
                    color = EmberOrange,
                )

                // Headphone indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Headphones,
                        contentDescription = "Headphone status",
                        tint = if (uiState.isHeadphoneConnected) ConnectionActive else TextMuted,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = if (uiState.isHeadphoneConnected) "Binaural" else "Mono",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (uiState.isHeadphoneConnected) ConnectionActive else TextMuted,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Waveform visualization ────────────────────────
            WaveformBars(isActive = uiState.isSpeaking)

            Spacer(modifier = Modifier.height(24.dp))

            // ── Voice gender toggle ───────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Voice:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                )
                Surface(
                    onClick = onToggleGender,
                    shape = MaterialTheme.shapes.small,
                    color = CharcoalDark,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = EmberOrange,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = uiState.voiceGender,
                            style = MaterialTheme.typography.labelLarge,
                            color = EmberOrange,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Volume sliders ────────────────────────────────
            VolumeSlider(
                label = "Voice",
                value = uiState.voiceVolume,
                onValueChange = onVoiceVolumeChange,
            )

            Spacer(modifier = Modifier.height(8.dp))

            VolumeSlider(
                label = "Ambient",
                value = uiState.soundscapeVolume,
                onValueChange = onSoundscapeVolumeChange,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Breath pacer tap zone ─────────────────────────
            BreathPacerZone(
                isActive = uiState.breathPaceActive,
                onTap = onBreathTap,
            )

            Spacer(modifier = Modifier.weight(1f))

            // ── Play controls ─────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onPlayPause) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                        tint = EmberOrange,
                        modifier = Modifier.size(48.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WaveformBars(isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(12) { index ->
            val height by infiniteTransition.animateFloat(
                initialValue = if (isActive) 0.2f else 0.1f,
                targetValue = if (isActive) 0.4f + (index % 3) * 0.2f else 0.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(300 + index * 50),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "bar_$index",
            )

            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height((60 * height).dp)
                    .background(
                        EmberOrange.copy(alpha = 0.5f + height),
                        shape = MaterialTheme.shapes.extraSmall,
                    ),
            )
        }
    }
}

@Composable
private fun VolumeSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = TextMuted,
            modifier = Modifier.width(64.dp),
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = EmberOrange,
                activeTrackColor = EmberOrange,
                inactiveTrackColor = CharcoalDark,
            ),
        )
    }
}

@Composable
private fun BreathPacerZone(
    isActive: Boolean,
    onTap: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breath")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isActive) 3000 else 4000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breath_scale",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Tap to Breathe",
            style = MaterialTheme.typography.labelLarge,
            color = TextMuted,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(breathScale)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onTap,
                ),
            contentAlignment = Alignment.Center,
        ) {
            PulsingGlow(
                color = CoolDownBlue.copy(alpha = if (isActive) 0.5f else 0.2f),
                size = 100.dp,
                pulseSpeed = if (isActive) 3000 else 4000,
            )

            Text(
                text = if (breathScale > 0.9f) "In" else "Out",
                style = MaterialTheme.typography.bodyLarge,
                color = CoolDownBlue,
                textAlign = TextAlign.Center,
            )
        }
    }
}
