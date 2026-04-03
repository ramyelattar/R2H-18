package com.igniteai.app.feature.scenario

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.igniteai.app.ui.components.IgniteCard
import com.igniteai.app.ui.theme.TextSecondary

@Composable
fun BranchChoiceCard(
    text: String,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
    ) {
        IgniteCard(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            glowing = true,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
