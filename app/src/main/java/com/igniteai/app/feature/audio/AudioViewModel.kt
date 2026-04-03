package com.igniteai.app.feature.audio

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igniteai.app.core.preferences.AppPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the audio player screen.
 *
 * Coordinates all audio subsystems:
 * - AudioEngine: voice playback (TTS + pre-recorded)
 * - SoundscapeLayer: ambient background loops
 * - BinauralMixer: stereo channel separation
 * - BreathPacer: tap-based rhythm synchronization
 */
class AudioViewModel(
    private val preferences: AppPreferences,
) : ViewModel() {

    data class AudioUiState(
        val isPlaying: Boolean = false,
        val isSpeaking: Boolean = false,
        val voiceGender: String = "FEMALE",
        val voiceVolume: Float = 1.0f,
        val soundscapeVolume: Float = 0.3f,
        val isHeadphoneConnected: Boolean = false,
        val breathPaceActive: Boolean = false,
        val breathPhase: String = "inhale", // "inhale" or "exhale"
        val currentText: String = "",
    )

    private val _uiState = MutableStateFlow(AudioUiState())
    val uiState: StateFlow<AudioUiState> = _uiState

    private var audioEngine: AudioEngine? = null
    private var binauralMixer: BinauralMixer? = null
    private var soundscapeLayer: SoundscapeLayer? = null
    private val breathPacer = BreathPacer()

    /**
     * Initialize audio systems. Call when entering audio screen.
     */
    fun initialize(context: Context) {
        audioEngine = AudioEngine(context).also { it.create() }
        binauralMixer = BinauralMixer(context)
        soundscapeLayer = SoundscapeLayer(context)

        viewModelScope.launch {
            val gender = preferences.voiceGender.first()
            _uiState.update {
                it.copy(
                    voiceGender = gender,
                    isHeadphoneConnected = binauralMixer?.isHeadphoneConnected() == true,
                )
            }
        }

        // Breath pacer timeout checker
        viewModelScope.launch {
            while (true) {
                delay(5000)
                if (breathPacer.checkTimeout()) {
                    _uiState.update { it.copy(breathPaceActive = false) }
                }
            }
        }

        // Observe speaking state
        viewModelScope.launch {
            audioEngine?.isSpeaking?.collect { speaking ->
                _uiState.update { it.copy(isSpeaking = speaking) }
            }
        }
    }

    /**
     * Speak text content via TTS.
     */
    fun speakText(text: String) {
        _uiState.update { it.copy(currentText = text, isPlaying = true) }

        audioEngine?.speakTts(
            text = text,
            config = AudioEngine.VoiceConfig(
                gender = _uiState.value.voiceGender,
            ),
            onComplete = {
                _uiState.update { it.copy(isPlaying = false) }
            },
        )
    }

    /**
     * Play a pre-recorded audio clip.
     */
    fun playPrerecorded(resId: Int) {
        _uiState.update { it.copy(isPlaying = true) }
        audioEngine?.playPrerecorded(resId)
    }

    /**
     * Stop all audio immediately.
     */
    fun stopAll() {
        audioEngine?.stopAll()
        soundscapeLayer?.stopAll()
        binauralMixer?.release()
        _uiState.update { it.copy(isPlaying = false, isSpeaking = false) }
    }

    /**
     * Toggle voice gender between MALE and FEMALE.
     */
    fun toggleVoiceGender() {
        viewModelScope.launch {
            val newGender = if (_uiState.value.voiceGender == "FEMALE") "MALE" else "FEMALE"
            preferences.setVoiceGender(newGender)
            _uiState.update { it.copy(voiceGender = newGender) }
        }
    }

    /**
     * Set voice volume.
     */
    fun setVoiceVolume(volume: Float) {
        audioEngine?.setVolume(AudioEngine.AudioLayer.VOICE, volume)
        _uiState.update { it.copy(voiceVolume = volume) }
    }

    /**
     * Set soundscape volume.
     */
    fun setSoundscapeVolume(volume: Float) {
        soundscapeLayer?.setVolume(SoundscapeLayer.Layer.AMBIENT, volume)
        _uiState.update { it.copy(soundscapeVolume = volume) }
    }

    /**
     * Record a breath tap.
     */
    fun onBreathTap() {
        breathPacer.onTap()
        _uiState.update { it.copy(breathPaceActive = breathPacer.isActive.value) }
    }

    /**
     * Release all resources. Call when leaving audio screen.
     */
    fun releaseAudio() {
        audioEngine?.release()
        soundscapeLayer?.stopAll()
        binauralMixer?.release()
        audioEngine = null
        soundscapeLayer = null
        binauralMixer = null
    }

    override fun onCleared() {
        super.onCleared()
        releaseAudio()
    }
}
