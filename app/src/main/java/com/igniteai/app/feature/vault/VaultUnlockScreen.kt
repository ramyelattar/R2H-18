package com.igniteai.app.feature.vault

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.igniteai.app.ui.components.EmberParticles
import com.igniteai.app.ui.components.R2H18Button
import com.igniteai.app.ui.components.PulsingGlow
import com.igniteai.app.ui.theme.AbyssBlack
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.MoltenGold
import com.igniteai.app.ui.theme.TextMuted

/**
 * Vault unlock gate requiring biometric authentication.
 *
 * Dramatic reveal animation when both partners authenticate.
 * The vault key is hardware-backed and requires recent auth.
 */
@Composable
fun VaultUnlockScreen(
    isUnlocked: Boolean,
    onRequestUnlock: () -> Unit,
    onEnterVault: () -> Unit,
) {
    var showReveal by remember(isUnlocked) { mutableStateOf(isUnlocked) }
    val bgAlpha by animateFloatAsState(
        targetValue = if (showReveal) 0f else 1f,
        animationSpec = tween(1200),
        label = "vault_reveal",
    )

    Box(
        modifier = Modifier.fillMaxSize().background(AbyssBlack),
    ) {
        EmberParticles(particleCount = 8, intensity = 3)

        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AnimatedVisibility(
                visible = !showReveal,
                enter = fadeIn() + scaleIn(),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PulsingGlow(color = MoltenGold, size = 140.dp) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = MoltenGold,
                            modifier = Modifier.size(64.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Forbidden Vault",
                        style = MaterialTheme.typography.headlineLarge,
                        color = EmberOrange,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Biometric authentication required\nto access private content",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    R2H18Button(
                        text = "Unlock Vault",
                        onClick = onRequestUnlock,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            AnimatedVisibility(
                visible = showReveal,
                enter = fadeIn(tween(800)) + scaleIn(tween(600)),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Access Granted",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MoltenGold,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    R2H18Button(
                        text = "Enter",
                        onClick = onEnterVault,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
