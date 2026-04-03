package com.igniteai.app.feature.haptic

import android.os.VibrationEffect

/**
 * Predefined vibration patterns for R2H18.
 *
 * Each pattern is designed for a specific emotional context:
 * - GENTLE_PULSE: sensual, slow warmth (200ms on, 300ms off)
 * - SHARP_BURST: commanding, authoritative (50ms intense burst)
 * - SLOW_WAVE: anticipation, building tension (escalating 100→500ms)
 * - HEARTBEAT: intimate connection (thud-thud-pause rhythm)
 * - ESCALATING: progressive intensity ramp (for tease sequences)
 * - SIGNATURE: unique Pavlovian conditioning pattern (distinctive, recognizable)
 * - EMERGENCY_STOP: safeword acknowledgment (triple sharp buzz)
 *
 * Amplitude values (0-255) require `hasAmplitudeControl()` — devices
 * without amplitude control get on/off patterns only.
 */
object HapticPatterns {

    data class HapticPattern(
        val name: String,
        val timings: LongArray,      // Alternating wait/vibrate durations (ms)
        val amplitudes: IntArray,    // Amplitude per timing segment (0-255)
        val repeatIndex: Int = -1,   // -1 = no repeat, 0+ = repeat from index
    ) {
        fun toVibrationEffect(): VibrationEffect {
            return VibrationEffect.createWaveform(timings, amplitudes, repeatIndex)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is HapticPattern) return false
            return name == other.name
        }

        override fun hashCode(): Int = name.hashCode()
    }

    /** Sensual, slow warmth — for intimate moments */
    val GENTLE_PULSE = HapticPattern(
        name = "gentle_pulse",
        timings = longArrayOf(0, 200, 300, 200, 300, 200, 300),
        amplitudes = intArrayOf(0, 80, 0, 100, 0, 80, 0),
    )

    /** Commanding, authoritative burst — for directives */
    val SHARP_BURST = HapticPattern(
        name = "sharp_burst",
        timings = longArrayOf(0, 50),
        amplitudes = intArrayOf(0, 255),
    )

    /** Building tension — escalating vibration for anticipation */
    val SLOW_WAVE = HapticPattern(
        name = "slow_wave",
        timings = longArrayOf(0, 100, 200, 200, 200, 300, 200, 400, 200, 500),
        amplitudes = intArrayOf(0, 40, 0, 80, 0, 120, 0, 180, 0, 255),
    )

    /** Intimate heartbeat rhythm — thud-thud-pause */
    val HEARTBEAT = HapticPattern(
        name = "heartbeat",
        timings = longArrayOf(0, 100, 100, 100, 600),
        amplitudes = intArrayOf(0, 200, 0, 150, 0),
    )

    /** Progressive intensity ramp — for tease sequences */
    val ESCALATING = HapticPattern(
        name = "escalating",
        timings = longArrayOf(0, 100, 300, 150, 250, 200, 200, 250, 150, 300, 100, 400),
        amplitudes = intArrayOf(0, 30, 0, 60, 0, 90, 0, 130, 0, 180, 0, 255),
    )

    /** Unique Pavlovian conditioning pattern — must be distinctive and memorable */
    val SIGNATURE = HapticPattern(
        name = "signature",
        timings = longArrayOf(0, 80, 60, 80, 60, 150),
        amplitudes = intArrayOf(0, 200, 0, 200, 0, 255),
    )

    /** Safeword acknowledgment — triple sharp buzz confirms stop */
    val EMERGENCY_STOP = HapticPattern(
        name = "emergency_stop",
        timings = longArrayOf(0, 100, 80, 100, 80, 100),
        amplitudes = intArrayOf(0, 255, 0, 255, 0, 255),
    )

    /** All patterns, for iteration in settings/preview */
    val ALL = listOf(
        GENTLE_PULSE,
        SHARP_BURST,
        SLOW_WAVE,
        HEARTBEAT,
        ESCALATING,
        SIGNATURE,
        EMERGENCY_STOP,
    )
}
