package com.igniteai.app.feature.challenge

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.igniteai.app.ui.theme.ConsentGreen
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.MoltenGold
import com.igniteai.app.ui.theme.SafewordRed
import com.igniteai.app.ui.theme.TextSecondary

/**
 * Circular countdown timer.
 *
 * Ring depletes clockwise. Color shifts:
 * green → amber → red as time runs out.
 * Pulses at 10-second warning.
 */
@Composable
fun TimerComponent(
    secondsRemaining: Int,
    totalSeconds: Int,
    size: Dp = 120.dp,
    modifier: Modifier = Modifier,
) {
    val progress = if (totalSeconds > 0) secondsRemaining.toFloat() / totalSeconds else 0f

    val color = when {
        progress > 0.5f -> ConsentGreen
        progress > 0.2f -> MoltenGold
        progress > 0.08f -> EmberOrange
        else -> SafewordRed
    }

    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 8.dp.toPx()
            val arcSize = Size(this.size.width - strokeWidth, this.size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            // Background ring
            drawArc(
                color = color.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )

            // Progress ring
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }

        Text(
            text = "${minutes}:${seconds.toString().padStart(2, '0')}",
            style = MaterialTheme.typography.headlineMedium,
            color = TextSecondary,
        )
    }
}
