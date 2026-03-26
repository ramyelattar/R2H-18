package com.igniteai.app.feature.content

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.Icons
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
import com.igniteai.app.data.model.ContentItem
import com.igniteai.app.ui.components.EmberParticles
import com.igniteai.app.ui.components.IgniteButton
import com.igniteai.app.ui.components.IgniteCard
import com.igniteai.app.ui.theme.AbyssBlack
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.TextMuted
import com.igniteai.app.ui.theme.TextSecondary
import com.igniteai.app.ui.theme.TonePlayful
import com.igniteai.app.ui.theme.ToneRaw
import com.igniteai.app.ui.theme.ToneSensual

/**
 * Full-screen dare display within a session.
 *
 * This is the in-session dare view (different from DailyDareCard on Home).
 * Features:
 * - Animated content transitions (slide + fade)
 * - Prominent dare text with intensity flames
 * - Tone color-coding
 * - "Done" button to complete and advance
 * - Feedback bar at bottom
 * - Ember particles background scaled to intensity
 */
@Composable
fun DareScreen(
    uiState: ContentViewModel.ContentUiState,
    onComplete: () -> Unit,
    onFavorite: () -> Unit,
    onSkip: () -> Unit,
    onBlock: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AbyssBlack),
    ) {
        val intensity = uiState.currentContent?.intensity ?: 3
        EmberParticles(particleCount = 8 + intensity * 2, intensity = intensity)

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = EmberOrange)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Dare content with animated transitions
                AnimatedContent(
                    targetState = uiState.currentContent,
                    transitionSpec = {
                        (slideInHorizontally { it } + fadeIn())
                            .togetherWith(slideOutHorizontally { -it } + fadeOut())
                    },
                    modifier = Modifier.weight(1f),
                    label = "dare_transition",
                ) { dare ->
                    if (dare != null) {
                        DareContent(dare = dare, onComplete = onComplete)
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No more dares available",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary,
                            )
                        }
                    }
                }

                // Feedback bar
                ContentFeedbackBar(
                    onFavorite = onFavorite,
                    onSkip = onSkip,
                    onBlock = onBlock,
                )
            }
        }
    }
}

@Composable
private fun DareContent(
    dare: ContentItem,
    onComplete: () -> Unit,
) {
    val toneColor = when (dare.tone.uppercase()) {
        "PLAYFUL" -> TonePlayful
        "RAW" -> ToneRaw
        "SENSUAL" -> ToneSensual
        else -> EmberOrange
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Tone label
        Text(
            text = dare.tone.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelLarge,
            color = toneColor,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Intensity flames
        Row {
            repeat(dare.intensity.coerceIn(1, 10)) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = EmberOrange,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dare card
        IgniteCard(glowing = true) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (dare.title.isNotBlank()) {
                    Text(
                        text = dare.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = EmberOrange,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = dare.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        IgniteButton(
            text = "Done",
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth(0.6f),
        )
    }
}
