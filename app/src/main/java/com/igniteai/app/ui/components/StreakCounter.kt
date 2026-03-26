package com.igniteai.app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.FlameRed
import com.igniteai.app.ui.theme.MoltenGold

/**
 * Heat streak counter display.
 *
 * Shows current consecutive-day streak with a fire icon
 * that grows more intense with higher streaks:
 * - 1-2 days: small ember orange flame
 * - 3-4 days: medium flame red
 * - 5+ days: large molten gold (max bonus)
 *
 * @param streakCount Number of consecutive days
 * @param modifier Modifier chain
 */
@Composable
fun StreakCounter(
    streakCount: Int,
    modifier: Modifier = Modifier,
) {
    if (streakCount <= 0) return

    val (iconSize, color) = when {
        streakCount >= 5 -> 32.dp to MoltenGold
        streakCount >= 3 -> 28.dp to FlameRed
        else -> 24.dp to EmberOrange
    }

    Row(
        modifier = modifier
            .animateContentSize(spring(stiffness = Spring.StiffnessLow))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = "Streak flame",
            tint = color,
            modifier = Modifier.size(iconSize),
        )
        Text(
            text = "x$streakCount",
            style = MaterialTheme.typography.headlineSmall,
            color = color,
        )
    }
}
