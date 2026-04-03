package com.igniteai.app.feature.pavlovian

import com.igniteai.app.core.preferences.AppPreferences
import com.igniteai.app.feature.audio.AudioEngine
import com.igniteai.app.feature.haptic.HapticEngine
import kotlinx.coroutines.flow.first
import kotlin.random.Random

/**
 * Pavlovian conditioning manager — pairs a signature sound+haptic
 * with moments of peak arousal during sessions.
 *
 * How it works:
 * 1. Content engine identifies "peak moments" (completing a dare,
 *    hearing a key phrase in audio, reaching a scenario climax)
 * 2. Content engine calls triggerConditioningMoment()
 * 3. PavlovianManager checks if enabled, rolls against intensity probability
 * 4. If triggered: plays signature sound + signature haptic simultaneously
 * 5. Records the trigger for analytics
 *
 * Over repeated sessions, the brain forms an association between
 * the signature sensory pattern and the arousal state. Eventually,
 * the chime alone can trigger anticipation.
 *
 * User controls:
 * - Enable/disable sound separately from haptic
 * - Intensity: SUBTLE (20%), MODERATE (50%), INTENSE (80%)
 * - Full disable: both sound and haptic off = no conditioning
 */
class PavlovianManager(
    private val audioEngine: AudioEngine?,
    private val hapticEngine: HapticEngine?,
    private val preferences: AppPreferences,
) {

    private val triggerTimestamps = mutableListOf<Long>()

    /**
     * Attempt to trigger a conditioning moment.
     *
     * Called by the content engine at moments of peak arousal.
     * May or may not actually fire, depending on:
     * - Whether sound/haptic are enabled in preferences
     * - Random roll against intensity probability
     *
     * @return true if the conditioning moment was actually triggered
     */
    suspend fun triggerConditioningMoment(): Boolean {
        val soundEnabled = preferences.pavlovianSoundEnabled.first()
        val hapticEnabled = preferences.pavlovianHapticEnabled.first()

        // If both disabled, no conditioning
        if (!soundEnabled && !hapticEnabled) return false

        val intensity = preferences.conditioningIntensity.first()
        val probability = SignatureAssets.getProbability(intensity)

        // Random roll — not every peak moment triggers conditioning
        if (Random.nextFloat() > probability) return false

        // Fire both simultaneously
        if (soundEnabled) {
            if (SignatureAssets.SIGNATURE_SOUND_RES != 0) {
                audioEngine?.playPrerecorded(
                    SignatureAssets.SIGNATURE_SOUND_RES,
                    AudioEngine.AudioLayer.SIGNATURE_SOUND,
                )
            }
            // Fallback: no audio file yet, skip sound
        }

        if (hapticEnabled) {
            hapticEngine?.play(SignatureAssets.SIGNATURE_HAPTIC)
        }

        // Record trigger for analytics
        triggerTimestamps.add(System.currentTimeMillis())

        return true
    }

    /**
     * Check if conditioning is enabled (at least one modality active).
     */
    suspend fun isEnabled(): Boolean {
        return preferences.pavlovianSoundEnabled.first() ||
            preferences.pavlovianHapticEnabled.first()
    }

    /**
     * Get the number of times conditioning has been triggered this session.
     */
    fun getSessionTriggerCount(): Int = triggerTimestamps.size

    /**
     * Reset session trigger history. Call when session ends.
     */
    fun resetSession() {
        triggerTimestamps.clear()
    }
}
