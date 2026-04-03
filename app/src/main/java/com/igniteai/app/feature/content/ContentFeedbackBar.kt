package com.igniteai.app.feature.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.SafewordRed
import com.igniteai.app.ui.theme.TextMuted
import com.igniteai.app.ui.theme.TextSecondary
import com.igniteai.app.ui.theme.ToneSensual

/**
 * Reusable feedback bar for all content screens.
 *
 * Three actions:
 * - Favorite (heart): saves to favorites, used by adaptive algorithm (weight +3)
 * - Skip (next): advances to next content (weight -2 in scoring)
 * - Block (stop): hides forever with confirmation dialog
 *
 * The block confirmation prevents accidental loss of content.
 * Blocked content can be unblocked in Settings (future task).
 */
@Composable
fun ContentFeedbackBar(
    onFavorite: () -> Unit,
    onSkip: () -> Unit,
    onBlock: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showBlockConfirm by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        IconButton(onClick = onFavorite) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Favorite",
                tint = ToneSensual,
                modifier = Modifier.size(32.dp),
            )
        }

        IconButton(onClick = onSkip) {
            Icon(
                imageVector = Icons.Filled.SkipNext,
                contentDescription = "Skip",
                tint = TextMuted,
                modifier = Modifier.size(32.dp),
            )
        }

        IconButton(onClick = { showBlockConfirm = true }) {
            Icon(
                imageVector = Icons.Filled.Block,
                contentDescription = "Block",
                tint = SafewordRed.copy(alpha = 0.6f),
                modifier = Modifier.size(32.dp),
            )
        }
    }

    if (showBlockConfirm) {
        AlertDialog(
            onDismissRequest = { showBlockConfirm = false },
            title = {
                Text(
                    text = "Hide Forever?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = EmberOrange,
                )
            },
            text = {
                Text(
                    text = "This content won't appear again.\nYou can undo this in Settings.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showBlockConfirm = false
                    onBlock()
                }) {
                    Text("Hide It", color = SafewordRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockConfirm = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
}
