package com.igniteai.app.feature.session

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.igniteai.app.ui.components.EmberParticles
import com.igniteai.app.ui.components.R2H18Button
import com.igniteai.app.ui.theme.AbyssBlack
import com.igniteai.app.ui.theme.ConsentGreen
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.TextMuted
import com.igniteai.app.ui.theme.TextSecondary

/**
 * Consent gate — both partners must biometrically authenticate
 * before a session can begin.
 *
 * Shows two consent indicators (circles) that light up green
 * when each partner authenticates. A pulsing animation on the
 * unconfirmed indicator creates gentle urgency.
 */
@Composable
fun ConsentGateScreen(
    localConsented: Boolean,
    partnerConsented: Boolean,
    onAuthenticateLocal: () -> Unit,
    onCancel: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "consent_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AbyssBlack),
    ) {
        EmberParticles(particleCount = 15, intensity = 3)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Session Requires Both Partners",
                    style = MaterialTheme.typography.headlineLarge,
                    color = EmberOrange,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Both partners must authenticate to begin.\nYour safety. Your privacy.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Consent indicators
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ConsentIndicator(
                        label = "You",
                        consented = localConsented,
                        pulseAlpha = if (!localConsented) pulseAlpha else 1f,
                    )

                    Spacer(modifier = Modifier.width(48.dp))

                    ConsentIndicator(
                        label = "Partner",
                        consented = partnerConsented,
                        pulseAlpha = if (!partnerConsented) pulseAlpha else 1f,
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                if (!localConsented) {
                    R2H18Button(
                        text = "Authenticate",
                        onClick = onAuthenticateLocal,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else if (!partnerConsented) {
                    Text(
                        text = "Waiting for your partner...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMuted,
                        modifier = Modifier.alpha(pulseAlpha),
                    )
                }
            }

            R2H18Button(
                text = "Cancel",
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ConsentIndicator(
    label: String,
    consented: Boolean,
    pulseAlpha: Float,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier
                .size(80.dp)
                .alpha(pulseAlpha),
            shape = CircleShape,
            color = if (consented) ConsentGreen else TextMuted.copy(alpha = 0.2f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (consented) "✓" else "?",
                    style = MaterialTheme.typography.headlineLarge,
                    color = if (consented) AbyssBlack else TextMuted,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (consented) ConsentGreen else TextSecondary,
        )
    }
}
