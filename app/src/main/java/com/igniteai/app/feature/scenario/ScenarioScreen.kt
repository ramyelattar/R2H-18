package com.igniteai.app.feature.scenario

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import com.igniteai.app.ui.theme.TextMuted
import com.igniteai.app.ui.theme.TextSecondary

@Composable
fun ScenarioScreen(
    uiState: ScenarioViewModel.ScenarioUiState,
    onChoose: (Int) -> Unit,
    onFinish: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AbyssBlack),
    ) {
        EmberParticles(particleCount = 10, intensity = 4)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
        ) {
            // Depth indicator
            Text(
                text = "Chapter ${uiState.depth + 1}",
                style = MaterialTheme.typography.labelLarge,
                color = TextMuted,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Story text
            AnimatedContent(
                targetState = uiState.currentNode?.text,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier.weight(1f),
                label = "story",
            ) { text ->
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Text(
                        text = text ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Start,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isComplete) {
                Text(
                    text = "The End",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MoltenGold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))
                IgniteButton(text = "Finish", onClick = onFinish, modifier = Modifier.fillMaxWidth())
            } else {
                Text(
                    text = "What happens next?",
                    style = MaterialTheme.typography.labelLarge,
                    color = EmberOrange,
                )
                Spacer(modifier = Modifier.height(12.dp))

                uiState.choices.forEachIndexed { index, choice ->
                    BranchChoiceCard(
                        text = choice,
                        index = index,
                        onClick = { onChoose(index) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
