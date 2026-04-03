package com.igniteai.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.igniteai.app.ui.theme.CharcoalDark
import com.igniteai.app.ui.theme.CharcoalLight
import com.igniteai.app.ui.theme.EmberOrange

/**
 * R2H18 styled card with subtle ember border.
 *
 * Used for dares, content, settings sections — any content block.
 * The faint ember-colored border makes cards feel warm without
 * being overwhelming.
 *
 * @param modifier Modifier chain
 * @param glowing If true, uses a brighter ember border (for featured content)
 * @param content Card content composable
 */
@Composable
fun R2H18Card(
    modifier: Modifier = Modifier,
    glowing: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = CharcoalDark,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (glowing) 8.dp else 2.dp,
        ),
        border = BorderStroke(
            width = if (glowing) 1.5.dp else 0.5.dp,
            color = if (glowing) EmberOrange.copy(alpha = 0.6f) else CharcoalLight.copy(alpha = 0.3f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content,
        )
    }
}
