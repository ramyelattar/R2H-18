package com.igniteai.app.feature.pavlovian

import com.igniteai.app.feature.haptic.HapticPatterns

/**
 * Signature sensory assets for Pavlovian conditioning.
 *
 * These are the specific sound and haptic that get paired with
 * arousal moments. They must be:
 * - Distinctive: unlike any other app notification or system sound
 * - Brief: under 1 second, so they don't interrupt the experience
 * - Pleasant: the association should be positive
 * - Consistent: always the same, so the brain learns the pattern
 *
 * The actual audio file (ignite_chime.ogg) will be added in the
 * audio content pass. For now, TTS generates a placeholder tone
 * using AudioEngine, and the haptic pattern is already defined.
 */
object SignatureAssets {

    /**
     * The signature haptic pattern — three distinct pulses.
     * Matches HapticPatterns.SIGNATURE: short-short-long
     * (80ms, 60ms gap, 80ms, 60ms gap, 150ms)
     */
    val SIGNATURE_HAPTIC = HapticPatterns.SIGNATURE

    /**
     * Placeholder for the signature sound resource ID.
     * Will be replaced with R.raw.ignite_chime when audio files are added.
     * Until then, PavlovianManager uses a programmatic tone.
     */
    const val SIGNATURE_SOUND_RES: Int = 0 // 0 = use programmatic tone fallback

    /**
     * Duration of the signature moment in milliseconds.
     * Both sound and haptic should complete within this window.
     */
    const val SIGNATURE_DURATION_MS = 500L

    /**
     * Probability thresholds for each conditioning intensity level.
     * These control how often the signature fires at peak moments:
     * - SUBTLE: 20% of moments — barely noticeable, slow conditioning
     * - MODERATE: 50% of moments — balanced (recommended default)
     * - INTENSE: 80% of moments — strong conditioning, fast learning
     */
    fun getProbability(intensity: String): Float {
        return when (intensity.uppercase()) {
            "SUBTLE" -> 0.20f
            "MODERATE" -> 0.50f
            "INTENSE" -> 0.80f
            else -> 0.50f
        }
    }
}
