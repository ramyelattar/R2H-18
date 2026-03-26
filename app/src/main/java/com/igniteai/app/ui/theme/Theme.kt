package com.igniteai.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * IgniteAI Material 3 Theme
 *
 * Always dark — this app has no light mode. The fire/ember palette
 * is designed exclusively for dark backgrounds to maximize visual
 * impact and create an intimate, immersive atmosphere.
 */

private val IgniteColorScheme = darkColorScheme(
    // Primary — ember orange (main actions, highlights)
    primary = EmberOrange,
    onPrimary = TextPrimary,
    primaryContainer = EmberOrangeDark,
    onPrimaryContainer = TextPrimary,

    // Secondary — flame red (intensity, urgency)
    secondary = FlameRed,
    onSecondary = TextPrimary,
    secondaryContainer = FlameRedDark,
    onSecondaryContainer = TextPrimary,

    // Tertiary — molten gold (rewards, streaks, premium)
    tertiary = MoltenGold,
    onTertiary = AbyssBlack,
    tertiaryContainer = MoltenGoldDark,
    onTertiaryContainer = TextPrimary,

    // Background & surface
    background = AbyssBlack,
    onBackground = TextPrimary,
    surface = CharcoalDark,
    onSurface = TextPrimary,
    surfaceVariant = CharcoalMedium,
    onSurfaceVariant = TextSecondary,

    // Outline
    outline = CharcoalLight,
    outlineVariant = CharcoalMedium,

    // Error — safeword red
    error = SafewordRed,
    onError = TextPrimary,
    errorContainer = FlameRedDark,
    onErrorContainer = TextPrimary,
)

@Composable
fun IgniteAITheme(
    content: @Composable () -> Unit
) {
    val colorScheme = IgniteColorScheme

    // Make system bars match our dark theme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AbyssBlack.toArgb()
            window.navigationBarColor = AbyssBlack.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = IgniteTypography,
        shapes = IgniteShapes,
        content = content
    )
}
