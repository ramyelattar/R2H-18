package com.igniteai.app.feature.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Haptic feedback engine — plays vibration patterns during sessions.
 *
 * Wraps Android's Vibrator service with R2H18-specific patterns.
 * Supports:
 * - Single pattern playback with intensity scaling
 * - Timed sequences (pattern A at 0s, pattern B at 2s, etc.)
 * - Audio sync (trigger haptic at a specific audio timestamp)
 * - Global intensity multiplier (user preference)
 * - Instant stop for safeword
 *
 * Falls back gracefully on devices without amplitude control —
 * patterns still play as on/off without fine amplitude adjustment.
 */
class HapticEngine(context: Context) {

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private var globalIntensity: Float = 1.0f
    private var sequenceJob: Job? = null

    /** Whether this device supports fine-grained amplitude control */
    val hasAmplitudeControl: Boolean
        get() = vibrator.hasAmplitudeControl()

    /**
     * Play a haptic pattern once.
     *
     * @param pattern The pattern to play
     * @param intensity Intensity multiplier (0.0 - 1.0), stacks with global intensity
     */
    fun play(pattern: HapticPatterns.HapticPattern, intensity: Float = 1.0f) {
        if (!vibrator.hasVibrator()) return

        val effectiveIntensity = (intensity * globalIntensity).coerceIn(0f, 1f)

        if (hasAmplitudeControl) {
            // Scale amplitudes by intensity
            val scaledAmplitudes = pattern.amplitudes.map { amp ->
                (amp * effectiveIntensity).toInt().coerceIn(0, 255)
            }.toIntArray()

            val effect = VibrationEffect.createWaveform(
                pattern.timings,
                scaledAmplitudes,
                pattern.repeatIndex,
            )
            vibrator.vibrate(effect)
        } else {
            // Fallback: on/off pattern without amplitude control
            val effect = pattern.toVibrationEffect()
            vibrator.vibrate(effect)
        }
    }

    /**
     * Play a timed sequence of patterns.
     *
     * @param patterns List of (pattern, delayBeforeMs) pairs
     * @param scope Coroutine scope for sequencing
     */
    fun playSequence(
        patterns: List<Pair<HapticPatterns.HapticPattern, Long>>,
        scope: CoroutineScope,
    ) {
        sequenceJob?.cancel()
        sequenceJob = scope.launch {
            for ((pattern, delayMs) in patterns) {
                delay(delayMs)
                play(pattern)
            }
        }
    }

    /**
     * Set the global intensity multiplier.
     *
     * @param level 0.0 (off) to 1.0 (full intensity)
     */
    fun setIntensity(level: Float) {
        globalIntensity = level.coerceIn(0f, 1f)
    }

    /**
     * Stop all haptics immediately. Used for safeword.
     */
    fun stopAll() {
        sequenceJob?.cancel()
        vibrator.cancel()
    }

    /**
     * Play the emergency stop pattern (safeword acknowledgment).
     * This confirms to the user that the safeword was received.
     */
    fun playEmergencyStop() {
        play(HapticPatterns.EMERGENCY_STOP, intensity = 1.0f)
    }
}
