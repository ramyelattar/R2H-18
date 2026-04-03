package com.igniteai.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * R2H18 Fire/Ember Color Palette
 *
 * Design philosophy: Dark with fire/neon — bold, intense, unapologetic.
 * Every color evokes heat, desire, and premium exclusivity.
 */

// Primary fire palette
val EmberOrange = Color(0xFFFF6B35)
val EmberOrangeLight = Color(0xFFFF9A6C)
val EmberOrangeDark = Color(0xFFCC5529)

val FlameRed = Color(0xFFFF3D00)
val FlameRedLight = Color(0xFFFF7539)
val FlameRedDark = Color(0xFFCC3100)

val MoltenGold = Color(0xFFFFB800)
val MoltenGoldLight = Color(0xFFFFD54F)
val MoltenGoldDark = Color(0xFFCC9300)

// Background & surface
val AbyssBlack = Color(0xFF0A0A0A)
val CharcoalDark = Color(0xFF1A1A1A)
val CharcoalMedium = Color(0xFF252525)
val CharcoalLight = Color(0xFF333333)
val DeepCharcoal = CharcoalDark

// Semantic colors
val SafewordRed = Color(0xFFFF1744)         // Emergency stop — bright, unmissable
val CoolDownBlue = Color(0xFF4FC3F7)        // Post-safeword calm
val ConsentGreen = Color(0xFF69F0AE)        // Both consented
val ConnectionActive = Color(0xFF00E676)    // BLE/WiFi connected

// Text
val TextPrimary = Color(0xFFFAFAFA)
val TextSecondary = Color(0xB3FAFAFA)       // 70% opacity white
val TextMuted = Color(0x66FAFAFA)           // 40% opacity white

// Tone indicators (used on content cards)
val TonePlayful = MoltenGold               // Amber — playful & teasing
val ToneRaw = FlameRed                     // Red — raw & dominant
val ToneSensual = Color(0xFFE91E63)        // Rose — sensual & intimate

// Gradients (used programmatically, not in the color scheme)
val EmberGradientStart = EmberOrange
val EmberGradientEnd = FlameRed
val FireGradientStart = FlameRed
val FireGradientEnd = MoltenGold
