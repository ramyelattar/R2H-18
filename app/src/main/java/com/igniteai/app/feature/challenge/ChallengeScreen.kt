package com.igniteai.app.feature.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.igniteai.app.ui.components.EmberParticles
import com.igniteai.app.ui.components.IgniteButton
import com.igniteai.app.ui.theme.AbyssBlack
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.MoltenGold
import com.igniteai.app.ui.theme.TextSecondary

@Composable
fun ChallengeScreen(
    uiState: ChallengeViewModel.ChallengeUiState,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onFinish: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().background(AbyssBlack),
    ) {
        EmberParticles(particleCount = 12, intensity = 5)

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (uiState.isComplete) {
                Text("Challenge Complete!", style = MaterialTheme.typography.headlineLarge, color = MoltenGold)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Score: ${uiState.score}", style = MaterialTheme.typography.displayLarge, color = EmberOrange)
                Spacer(modifier = Modifier.height(32.dp))
                IgniteButton(text = "Done", onClick = onFinish, modifier = Modifier.fillMaxWidth())
            } else {
                Text("Challenge", style = MaterialTheme.typography.headlineLarge, color = EmberOrange)
                Spacer(modifier = Modifier.height(24.dp))

                TimerComponent(
                    secondsRemaining = uiState.timerSeconds,
                    totalSeconds = 120,
                    size = 140.dp,
                )

                Spacer(modifier = Modifier.height(24.dp))

                uiState.challenge?.let { challenge ->
                    Text(
                        text = challenge.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (!uiState.timerRunning) {
                    IgniteButton(text = "Start", onClick = onStart, modifier = Modifier.fillMaxWidth())
                } else {
                    IgniteButton(text = "Done!", onClick = onComplete, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
