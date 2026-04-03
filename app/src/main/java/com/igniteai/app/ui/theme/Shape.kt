package com.igniteai.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * R2H18 Shape System
 *
 * Rounded corners that feel smooth and premium.
 * Larger radii for cards, smaller for buttons.
 */
val R2H18Shapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),     // Chips, tags
    small = RoundedCornerShape(10.dp),         // Small buttons, inputs
    medium = RoundedCornerShape(14.dp),        // Standard buttons, dialogs
    large = RoundedCornerShape(22.dp),         // Cards, panels
    extraLarge = RoundedCornerShape(30.dp),    // Full-screen overlays, bottom sheets
)
