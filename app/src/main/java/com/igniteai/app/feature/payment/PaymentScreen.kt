package com.igniteai.app.feature.payment

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.igniteai.app.ui.components.EmberParticles
import com.igniteai.app.ui.components.IgniteButton
import com.igniteai.app.ui.components.PulsingGlow
import com.igniteai.app.ui.theme.AbyssBlack
import com.igniteai.app.ui.theme.ConsentGreen
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.FlameRed
import com.igniteai.app.ui.theme.MoltenGold
import com.igniteai.app.ui.theme.TextSecondary

@Composable
fun PaymentScreen(
    uiState: PaymentViewModel.PaymentUiState,
    onUnlockFire: () -> Unit,
    onReturnHome: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AbyssBlack),
    ) {
        EmberParticles(particleCount = 20, intensity = 6)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (uiState.state) {
                PaymentViewModel.PaymentState.SUCCESS -> {
                    Spacer(modifier = Modifier.height(80.dp))
                    PulsingGlow(color = MoltenGold, size = 150.dp, pulseSpeed = 800)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Fire Unlocked!", style = MaterialTheme.typography.displayLarge, color = MoltenGold)
                    Spacer(modifier = Modifier.height(32.dp))
                    IgniteButton(text = "Let's Go", onClick = onReturnHome, modifier = Modifier.fillMaxWidth())
                }

                PaymentViewModel.PaymentState.VERIFYING -> {
                    Spacer(modifier = Modifier.weight(1f))
                    CircularProgressIndicator(color = EmberOrange)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Verifying payment...", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                    Spacer(modifier = Modifier.weight(1f))
                }

                else -> {
                    // Feature list + purchase
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = FlameRed,
                        modifier = Modifier.size(64.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Unlock Fire", style = MaterialTheme.typography.displayLarge, color = FlameRed)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("$29 — One-time purchase. Own it forever.", style = MaterialTheme.typography.bodyLarge, color = TextSecondary, textAlign = TextAlign.Center)

                    Spacer(modifier = Modifier.height(32.dp))

                    val features = listOf(
                        "Branching roleplay scenarios",
                        "D/s control transfer mode",
                        "Heart rate visualization",
                        "Synchronized couple challenges",
                        "Higher intensity content (6-10)",
                        "Advanced Pavlovian conditioning",
                    )
                    features.forEach { feature ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.Check, null, tint = ConsentGreen, modifier = Modifier.size(20.dp))
                            Text(feature, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (uiState.isFireUnlocked) {
                        Text("Already unlocked!", style = MaterialTheme.typography.headlineMedium, color = ConsentGreen)
                    } else {
                        IgniteButton(text = "Unlock Fire — $29", onClick = onUnlockFire, modifier = Modifier.fillMaxWidth())
                    }

                    if (uiState.error != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(uiState.error, style = MaterialTheme.typography.bodyLarge, color = FlameRed)
                    }
                }
            }
        }
    }
}
