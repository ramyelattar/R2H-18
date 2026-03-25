package com.igniteai.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * IgniteAI Shape System
 *
 * Rounded corners that feel smooth and premium.
 * Larger radii for cards, smaller for buttons.
 */
val IgniteShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),     // Chips, tags
    small = RoundedCornerShape(8.dp),          // Small buttons, inputs
    medium = RoundedCornerShape(12.dp),        // Standard buttons, dialogs
    large = RoundedCornerShape(20.dp),         // Cards, panels
    extraLarge = RoundedCornerShape(28.dp),    // Full-screen overlays, bottom sheets
)
