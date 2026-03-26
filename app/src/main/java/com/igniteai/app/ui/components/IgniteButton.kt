package com.igniteai.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.igniteai.app.ui.theme.EmberGradientEnd
import com.igniteai.app.ui.theme.EmberGradientStart
import com.igniteai.app.ui.theme.SafewordRed

/**
 * IgniteAI primary button with animated ember glow border.
 *
 * The gradient border slowly shifts, creating a "burning" effect
 * that makes buttons feel alive and inviting.
 *
 * @param text Button label
 * @param onClick Click handler
 * @param modifier Modifier chain
 * @param enabled Whether the button is enabled
 * @param isEmergency If true, uses SafewordRed (for stop button)
 */
@Composable
fun IgniteButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isEmergency: Boolean = false,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ember_glow")

    // Animate the gradient offset to create a shimmering effect
    val glowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glow_offset"
    )

    val gradientColors = if (isEmergency) {
        listOf(SafewordRed, SafewordRed.copy(alpha = 0.7f), SafewordRed)
    } else {
        listOf(EmberGradientStart, EmberGradientEnd, EmberGradientStart)
    }

    val gradientBrush = Brush.linearGradient(
        colors = gradientColors,
        start = Offset(glowOffset, 0f),
        end = Offset(glowOffset + 500f, 500f)
    )

    // Outer glow container
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(gradientBrush)
            .padding(2.dp), // This creates the glowing border effect
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isEmergency) SafewordRed else MaterialTheme.colorScheme.surface,
                contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = Color.White.copy(alpha = 0.4f),
            ),
            contentPadding = PaddingValues(horizontal = 24.dp),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
