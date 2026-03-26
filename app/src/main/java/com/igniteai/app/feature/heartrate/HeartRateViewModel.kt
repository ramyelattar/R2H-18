package com.igniteai.app.feature.heartrate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Heart rate visualization — reads BPM from Health Connect
 * and shares with partner via BLE/WiFi.
 *
 * Spike detection: if heart rate increases >20% in 5 seconds,
 * triggers a haptic on the partner's device.
 */
class HeartRateViewModel : ViewModel() {

    data class HeartRateUiState(
        val localBpm: Int? = null,
        val partnerBpm: Int? = null,
        val isAvailable: Boolean = false,
        val spikeDetected: Boolean = false,
    )

    private val _uiState = MutableStateFlow(HeartRateUiState())
    val uiState: StateFlow<HeartRateUiState> = _uiState

    private val recentReadings = mutableListOf<Pair<Long, Int>>() // timestamp to BPM

    /**
     * Update local heart rate (from Health Connect polling).
     */
    fun updateLocalHeartRate(bpm: Int) {
        val now = System.currentTimeMillis()
        recentReadings.add(now to bpm)

        // Keep last 10 seconds of readings
        recentReadings.removeAll { (timestamp, _) -> now - timestamp > 10_000 }

        // Spike detection: >20% increase in 5 seconds
        val fiveSecondsAgo = recentReadings.filter { now - it.first in 4000..6000 }
        val spike = if (fiveSecondsAgo.isNotEmpty()) {
            val oldBpm = fiveSecondsAgo.first().second
            bpm > oldBpm * 1.2
        } else false

        _uiState.update {
            it.copy(localBpm = bpm, spikeDetected = spike, isAvailable = true)
        }
    }

    /**
     * Update partner's heart rate (received via sync).
     */
    fun updatePartnerHeartRate(bpm: Int) {
        _uiState.update { it.copy(partnerBpm = bpm) }
    }
}
