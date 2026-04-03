package com.igniteai.app.feature.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.igniteai.app.ui.components.EmberParticles
import com.igniteai.app.ui.components.IgniteButton
import com.igniteai.app.ui.components.PulsingGlow
import com.igniteai.app.ui.theme.AbyssBlack
import com.igniteai.app.ui.theme.DeepCharcoal
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.FlameRed
import com.igniteai.app.ui.theme.MoltenGold
import com.igniteai.app.ui.theme.TextMuted
import com.igniteai.app.ui.theme.TextSecondary

@Composable
fun AuthGateScreen(
    uiState: AuthGateViewModel.AuthUiState,
    onRequestBiometric: () -> Unit,
    onUsePinInstead: () -> Unit,
    onPinDigit: (Char) -> Unit,
    onPinBackspace: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().background(AbyssBlack),
    ) {
        EmberParticles(particleCount = 6, intensity = 2)

        AnimatedContent(
            targetState = uiState.state,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "auth_gate",
        ) { state ->
            when (state) {
                AuthGateViewModel.AuthState.LOCKED,
                AuthGateViewModel.AuthState.BIOMETRIC_PROMPT -> {
                    BiometricLockScreen(
                        onUnlock = onRequestBiometric,
                        onUsePinInstead = onUsePinInstead,
                    )
                }
                AuthGateViewModel.AuthState.PIN_ENTRY -> {
                    PinEntryScreen(
                        pin = uiState.pinInput,
                        error = uiState.pinError,
                        onDigit = onPinDigit,
                        onBackspace = onPinBackspace,
                        onUseBiometric = onRequestBiometric,
                    )
                }
                AuthGateViewModel.AuthState.UNLOCKED -> {
                    // Navigation handles transition — show nothing
                    Box(modifier = Modifier.fillMaxSize().background(AbyssBlack))
                }
            }
        }
    }
}

@Composable
private fun BiometricLockScreen(
    onUnlock: () -> Unit,
    onUsePinInstead: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        PulsingGlow(color = EmberOrange, size = 120.dp) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Unlock",
                tint = EmberOrange,
                modifier = Modifier.size(56.dp),
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "IgniteAI",
            style = MaterialTheme.typography.headlineLarge,
            color = MoltenGold,
        )

        Spacer(modifier = Modifier.height(40.dp))

        IgniteButton(
            text = "Unlock",
            onClick = onUnlock,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onUsePinInstead) {
            Text("Use PIN instead", color = TextMuted)
        }
    }
}

@Composable
private fun PinEntryScreen(
    pin: String,
    error: String?,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onUseBiometric: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Enter PIN",
            style = MaterialTheme.typography.headlineSmall,
            color = TextSecondary,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // PIN dots
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(6) { i ->
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (i < pin.length) EmberOrange else DeepCharcoal),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        error?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = FlameRed,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Number pad
        val digits = listOf(
            listOf('1', '2', '3'),
            listOf('4', '5', '6'),
            listOf('7', '8', '9'),
            listOf(' ', '0', '←'),
        )

        digits.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                row.forEach { digit ->
                    if (digit == ' ') {
                        Spacer(modifier = Modifier.size(72.dp))
                    } else {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .clickable {
                                    if (digit == '←') onBackspace()
                                    else onDigit(digit)
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = digit.toString(),
                                style = MaterialTheme.typography.headlineSmall,
                                color = TextSecondary,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onUseBiometric) {
            Text("Use biometrics", color = TextMuted)
        }
    }
}
