package com.igniteai.app.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.igniteai.app.ui.theme.AbyssBlack
import com.igniteai.app.ui.theme.DeepCharcoal
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.MoltenGold
import com.igniteai.app.ui.theme.TextMuted
import com.igniteai.app.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    uiState: SettingsViewModel.SettingsUiState,
    onBack: () -> Unit,
    onToneChanged: (String) -> Unit,
    onVoiceGenderChanged: (String) -> Unit,
    onNotificationsToggled: (Boolean) -> Unit,
    onSessionTimeLimitChanged: (Int) -> Unit,
    onDenyDelayChanged: (Int) -> Unit,
    onPavlovianSoundToggled: (Boolean) -> Unit,
    onPavlovianHapticToggled: (Boolean) -> Unit,
    onConditioningIntensityChanged: (String) -> Unit,
    onVoiceSafewordToggled: (Boolean) -> Unit,
    onSafewordChanged: (String) -> Unit,
    onDecoyToggled: (Boolean) -> Unit,
    onWipeData: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AbyssBlack)
            .verticalScroll(rememberScrollState()),
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextSecondary)
            }
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                color = EmberOrange,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {

            // ── Content Preferences ──────────────────
            SectionHeader("Content")

            OptionRow("Tone") {
                val tones = listOf("PLAYFUL", "RAW", "SENSUAL", "ADAPTIVE")
                tones.forEach { tone ->
                    Text(
                        text = tone.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (uiState.tonePreference == tone) EmberOrange else TextMuted,
                        modifier = Modifier
                            .clickable { onToneChanged(tone) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            OptionRow("Voice") {
                listOf("MALE", "FEMALE").forEach { gender ->
                    Text(
                        text = gender.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (uiState.voiceGender == gender) EmberOrange else TextMuted,
                        modifier = Modifier
                            .clickable { onVoiceGenderChanged(gender) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Session Settings ─────────────────────
            SectionHeader("Session")

            SliderRow(
                label = "Time Limit",
                value = uiState.sessionTimeLimitMinutes.toFloat(),
                range = 15f..120f,
                steps = 6,
                display = "${uiState.sessionTimeLimitMinutes} min",
                onValueChange = { onSessionTimeLimitChanged(it.toInt()) },
            )

            SliderRow(
                label = "Deny Delay",
                value = uiState.denyDelayDuration.toFloat(),
                range = 5f..60f,
                steps = 10,
                display = "${uiState.denyDelayDuration}s",
                onValueChange = { onDenyDelayChanged(it.toInt()) },
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Notifications ────────────────────────
            SectionHeader("Notifications")

            ToggleRow(
                label = "Daily Dare Reminder",
                checked = uiState.notificationsEnabled,
                onCheckedChange = onNotificationsToggled,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Conditioning ─────────────────────────
            SectionHeader("Conditioning")

            ToggleRow(
                label = "Signature Sound",
                checked = uiState.pavlovianSoundEnabled,
                onCheckedChange = onPavlovianSoundToggled,
            )

            ToggleRow(
                label = "Signature Haptic",
                checked = uiState.pavlovianHapticEnabled,
                onCheckedChange = onPavlovianHapticToggled,
            )

            OptionRow("Intensity") {
                listOf("SUBTLE", "MODERATE", "INTENSE").forEach { level ->
                    Text(
                        text = level.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (uiState.conditioningIntensity == level) EmberOrange else TextMuted,
                        modifier = Modifier
                            .clickable { onConditioningIntensityChanged(level) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Safety ───────────────────────────────
            SectionHeader("Safety")

            ToggleRow(
                label = "Voice Safeword",
                checked = uiState.voiceSafewordEnabled,
                onCheckedChange = onVoiceSafewordToggled,
            )

            if (uiState.voiceSafewordEnabled) {
                var editingSafeword by remember { mutableStateOf(uiState.safeword) }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Safeword", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.width(16.dp))
                    TextField(
                        value = editingSafeword,
                        onValueChange = {
                            editingSafeword = it
                            onSafewordChanged(it)
                        },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = TextSecondary,
                            unfocusedTextColor = TextSecondary,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Privacy ──────────────────────────────
            SectionHeader("Privacy")

            ToggleRow(
                label = "Decoy App Icon",
                checked = uiState.decoyEnabled,
                onCheckedChange = onDecoyToggled,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Danger Zone ──────────────────────────
            Text(
                text = "Wipe All Data",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFFF4444),
                modifier = Modifier
                    .clickable { onWipeData() }
                    .padding(vertical = 12.dp),
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MoltenGold,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = EmberOrange,
                checkedTrackColor = EmberOrange.copy(alpha = 0.3f),
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = DeepCharcoal,
            ),
        )
    }
}

@Composable
private fun OptionRow(
    label: String,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Row { content() }
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    display: String,
    onValueChange: (Float) -> Unit,
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Text(display, style = MaterialTheme.typography.bodyMedium, color = EmberOrange)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = EmberOrange,
                activeTrackColor = EmberOrange,
                inactiveTrackColor = DeepCharcoal,
            ),
        )
    }
}
