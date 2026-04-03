package com.igniteai.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igniteai.app.core.preferences.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferences: AppPreferences,
) : ViewModel() {

    data class SettingsUiState(
        val tonePreference: String = "ADAPTIVE",
        val voiceGender: String = "FEMALE",
        val notificationsEnabled: Boolean = true,
        val notificationHour: Int = 20,
        val notificationMinute: Int = 0,
        val sessionTimeLimitMinutes: Int = 60,
        val denyDelayDuration: Int = 10,
        val pavlovianSoundEnabled: Boolean = true,
        val pavlovianHapticEnabled: Boolean = true,
        val conditioningIntensity: String = "MODERATE",
        val voiceSafewordEnabled: Boolean = false,
        val safeword: String = "red",
        val decoyEnabled: Boolean = false,
    )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(
                preferences.tonePreference,
                preferences.voiceGender,
                preferences.notificationsEnabled,
                preferences.notificationHour,
                preferences.notificationMinute,
            ) { tone, gender, notifEnabled, hour, minute ->
                _uiState.update {
                    it.copy(
                        tonePreference = tone,
                        voiceGender = gender,
                        notificationsEnabled = notifEnabled,
                        notificationHour = hour,
                        notificationMinute = minute,
                    )
                }
            }.collect {}
        }

        viewModelScope.launch {
            combine(
                preferences.sessionTimeLimit,
                preferences.denyDelayDuration,
                preferences.pavlovianSoundEnabled,
                preferences.pavlovianHapticEnabled,
                preferences.conditioningIntensity,
            ) { timeLimit, denyDelay, pavSound, pavHaptic, condIntensity ->
                _uiState.update {
                    it.copy(
                        sessionTimeLimitMinutes = timeLimit,
                        denyDelayDuration = denyDelay,
                        pavlovianSoundEnabled = pavSound,
                        pavlovianHapticEnabled = pavHaptic,
                        conditioningIntensity = condIntensity,
                    )
                }
            }.collect {}
        }

        viewModelScope.launch {
            combine(
                preferences.voiceSafewordEnabled,
                preferences.safeword,
                preferences.decoyEnabled,
            ) { voiceSafe, safeword, decoy ->
                _uiState.update {
                    it.copy(
                        voiceSafewordEnabled = voiceSafe,
                        safeword = safeword,
                        decoyEnabled = decoy,
                    )
                }
            }.collect {}
        }
    }

    fun setTonePreference(tone: String) = launch { preferences.setTonePreference(tone) }
    fun setVoiceGender(gender: String) = launch { preferences.setVoiceGender(gender) }
    fun setNotificationsEnabled(enabled: Boolean) = launch { preferences.setNotificationsEnabled(enabled) }
    fun setNotificationTime(hour: Int, minute: Int) = launch { preferences.setNotificationTime(hour, minute) }
    fun setSessionTimeLimit(minutes: Int) = launch { preferences.setSessionTimeLimit(minutes) }
    fun setDenyDelayDuration(seconds: Int) = launch { preferences.setDenyDelayDuration(seconds) }
    fun setPavlovianSoundEnabled(enabled: Boolean) = launch { preferences.setPavlovianSoundEnabled(enabled) }
    fun setPavlovianHapticEnabled(enabled: Boolean) = launch { preferences.setPavlovianHapticEnabled(enabled) }
    fun setConditioningIntensity(intensity: String) = launch { preferences.setConditioningIntensity(intensity) }
    fun setVoiceSafewordEnabled(enabled: Boolean) = launch { preferences.setVoiceSafewordEnabled(enabled) }
    fun setSafeword(word: String) = launch { preferences.setSafeword(word) }
    fun setDecoyEnabled(enabled: Boolean) = launch { preferences.setDecoyEnabled(enabled) }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
