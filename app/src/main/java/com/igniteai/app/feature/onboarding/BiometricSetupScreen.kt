package com.igniteai.app.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.igniteai.app.ui.components.R2H18Button
import com.igniteai.app.ui.components.R2H18Card
import com.igniteai.app.ui.theme.AbyssBlack
import com.igniteai.app.ui.theme.ConsentGreen
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.TextMuted
import com.igniteai.app.ui.theme.TextSecondary

/**
 * Explains why biometric auth is needed and triggers enrollment.
 *
 * Key messaging: "Both partners authenticate before every session —
 * your privacy, your safety." This builds trust and explains the
 * dual-consent model before the user encounters it in practice.
 */
@Composable
fun BiometricSetupScreen(
    onEnableBiometric: () -> Unit,
    onSkip: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AbyssBlack)
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Secure Your Privacy",
                style = MaterialTheme.typography.headlineLarge,
                color = EmberOrange,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            R2H18Card(glowing = true) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Fingerprint icon placeholder (using text since we don't have vector assets yet)
                    Text(
                        text = "\uD83D\uDD12", // Lock emoji as placeholder
                        style = MaterialTheme.typography.displayLarge,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Fingerprint or Face Unlock",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ConsentGreen,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Both partners authenticate before every session — your privacy, your safety.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Nobody can open this app without your biometric. Not even if they have your phone.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            R2H18Button(
                text = "Enable Biometric",
                onClick = onEnableBiometric,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onSkip) {
                Text(
                    text = "Skip — I'll use PIN only",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}
