package com.igniteai.app.feature.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Tap-based breath synchronization system.
 *
 * Instead of mic-based breath detection (unreliable), users tap
 * to set their breathing rhythm. The pacer learns the pattern
 * and syncs audio/haptic cues to match.
 *
 * How it works:
 * 1. User taps on each inhale (or exhale — their choice)
 * 2. Pacer measures interval between taps
 * 3. Assumes inhale = 40% of cycle, exhale = 60% (physiological default)
 * 4. Audio engine adjusts TTS pauses and ambient speed to match
 *
 * If no taps for 30 seconds, falls back to default pace (4s in, 6s out).
 */
class BreathPacer {

    data class BreathPace(
        val inhaleDurationMs: Long = 4000,
        val exhaleDurationMs: Long = 6000,
    ) {
        val cycleDurationMs: Long get() = inhaleDurationMs + exhaleDurationMs
    }

    companion object {
        val DEFAULT_PACE = BreathPace(inhaleDurationMs = 4000, exhaleDurationMs = 6000)
        private const val TAP_TIMEOUT_MS = 30_000L
        private const val MIN_CYCLE_MS = 3000L  // Fastest reasonable breath
        private const val MAX_CYCLE_MS = 15000L // Slowest reasonable breath
        private const val INHALE_RATIO = 0.4f   // 40% inhale, 60% exhale
    }

    private val _pace = MutableStateFlow(DEFAULT_PACE)
    val pace: StateFlow<BreathPace> = _pace

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive

    private var lastTapTime: Long = 0
    private val tapIntervals = mutableListOf<Long>()

    /**
     * Record a tap. Call on each user tap.
     * After 3+ taps, calculates average rhythm and updates pace.
     */
    fun onTap() {
        val now = System.currentTimeMillis()

        if (lastTapTime > 0) {
            val interval = now - lastTapTime

            // Only accept reasonable intervals
            if (interval in MIN_CYCLE_MS..MAX_CYCLE_MS) {
                tapIntervals.add(interval)
                _isActive.value = true

                // Keep last 5 intervals for rolling average
                if (tapIntervals.size > 5) {
                    tapIntervals.removeAt(0)
                }

                // Calculate pace from average interval
                if (tapIntervals.size >= 2) {
                    val avgCycle = tapIntervals.average().toLong()
                    _pace.value = BreathPace(
                        inhaleDurationMs = (avgCycle * INHALE_RATIO).toLong(),
                        exhaleDurationMs = (avgCycle * (1 - INHALE_RATIO)).toLong(),
                    )
                }
            }
        }

        lastTapTime = now
    }

    /**
     * Check if the pacer has timed out (no taps for 30s).
     * Call this periodically. Returns true if timed out and reset to default.
     */
    fun checkTimeout(): Boolean {
        if (!_isActive.value) return false

        val elapsed = System.currentTimeMillis() - lastTapTime
        if (elapsed > TAP_TIMEOUT_MS) {
            reset()
            return true
        }
        return false
    }

    /**
     * Reset to default pace. Clears tap history.
     */
    fun reset() {
        _pace.value = DEFAULT_PACE
        _isActive.value = false
        lastTapTime = 0
        tapIntervals.clear()
    }
}
