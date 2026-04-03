package com.igniteai.app.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.igniteai.app.data.model.ContentItem
import com.igniteai.app.ui.components.R2H18Button
import com.igniteai.app.ui.components.R2H18Card
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.SafewordRed
import com.igniteai.app.ui.theme.TextMuted
import com.igniteai.app.ui.theme.TextSecondary
import com.igniteai.app.ui.theme.TonePlayful
import com.igniteai.app.ui.theme.ToneRaw
import com.igniteai.app.ui.theme.ToneSensual

/**
 * Daily dare card — the central engagement piece on the Home screen.
 *
 * Displays:
 * - Tone indicator (color-coded dot: amber=playful, red=raw, rose=sensual)
 * - Dare title and body text
 * - Intensity flames (1-10)
 * - Action row: Favorite, Skip, Block
 * - "Do It" completion button
 *
 * Animates in with fade + slide from below for a satisfying reveal.
 */
@Composable
fun DailyDareCard(
    dare: ContentItem,
    dareCompleted: Boolean,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    onFavorite: () -> Unit,
    onBlock: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(dare.id) {
        visible = false
        kotlinx.coroutines.delay(100)
        visible = true
    }

    val toneColor = when (dare.tone.uppercase()) {
        "PLAYFUL" -> TonePlayful
        "RAW" -> ToneRaw
        "SENSUAL" -> ToneSensual
        else -> EmberOrange
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
    ) {
        R2H18Card(
            modifier = modifier.fillMaxWidth(),
            glowing = !dareCompleted,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
            ) {
                // Top row: tone indicator + intensity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Tone badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(toneColor, CircleShape),
                        )
                        Text(
                            text = dare.tone.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelLarge,
                            color = toneColor,
                        )
                    }

                    // Intensity flames
                    Row {
                        repeat(dare.intensity.coerceIn(1, 5)) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = EmberOrange,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                if (dare.title.isNotBlank()) {
                    Text(
                        text = dare.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = EmberOrange,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Body
                Text(
                    text = dare.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Completion button
                if (!dareCompleted) {
                    R2H18Button(
                        text = "Do It",
                        onClick = onComplete,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Text(
                        text = "Completed!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = EmberOrange,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action row: Favorite | Skip | Block
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    ActionButton(
                        icon = Icons.Filled.Favorite,
                        label = "Favorite",
                        color = ToneSensual,
                        onClick = onFavorite,
                    )
                    ActionButton(
                        icon = Icons.Filled.SkipNext,
                        label = "Skip",
                        color = TextMuted,
                        onClick = onSkip,
                    )
                    ActionButton(
                        icon = Icons.Filled.Block,
                        label = "Block",
                        color = SafewordRed.copy(alpha = 0.6f),
                        onClick = onBlock,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
        )
    }
}
