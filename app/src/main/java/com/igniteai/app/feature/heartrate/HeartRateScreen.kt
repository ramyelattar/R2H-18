package com.igniteai.app.feature.heartrate

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.igniteai.app.ui.theme.AbyssBlack
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.FlameRed
import com.igniteai.app.ui.theme.TextMuted
import com.igniteai.app.ui.theme.TextSecondary

@Composable
fun HeartRateScreen(
    uiState: HeartRateViewModel.HeartRateUiState,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AbyssBlack),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text("Heart Rate", style = MaterialTheme.typography.headlineLarge, color = EmberOrange)

            Spacer(modifier = Modifier.height(48.dp))

            if (!uiState.isAvailable) {
                Text(
                    text = "Connect a smartwatch with\nHealth Connect to see heart rate",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                )
            } else {
                // Side-by-side heart rate display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    // Local
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("You", style = MaterialTheme.typography.labelLarge, color = TextMuted)
                        Spacer(modifier = Modifier.height(12.dp))
                        HeartRateGlow(bpm = uiState.localBpm)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${uiState.localBpm ?: "--"} BPM",
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                            color = TextSecondary,
                        )
                    }

                    // Partner
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Partner", style = MaterialTheme.typography.labelLarge, color = TextMuted)
                        Spacer(modifier = Modifier.height(12.dp))
                        HeartRateGlow(bpm = uiState.partnerBpm)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${uiState.partnerBpm ?: "--"} BPM",
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                            color = TextSecondary,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (uiState.spikeDetected) {
                    Text(
                        text = "Feeling it...",
                        style = MaterialTheme.typography.headlineMedium,
                        color = FlameRed,
                    )
                }
            }
        }
    }
}
