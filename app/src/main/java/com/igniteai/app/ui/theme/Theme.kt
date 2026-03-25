package com.igniteai.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Placeholder theme — will be fully built in Task 2
 * with fire/ember color palette, typography, and shapes.
 */

private val IgniteColorScheme = darkColorScheme(
    primary = Color(0xFFFF6B35),       // Ember orange
    secondary = Color(0xFFFF3D00),     // Flame red
    background = Color(0xFF0A0A0A),    // Abyss black
    surface = Color(0xFF1A1A1A),       // Charcoal
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun IgniteAITheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = IgniteColorScheme,
        content = content
    )
}
