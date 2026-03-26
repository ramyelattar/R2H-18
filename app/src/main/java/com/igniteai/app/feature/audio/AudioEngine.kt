package com.igniteai.app.feature.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.annotation.RawRes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale
import java.util.UUID

/**
 * Central audio orchestrator — manages all audio output in IgniteAI.
 *
 * Three audio layers play simultaneously:
 * 1. VOICE — TTS narration or pre-recorded voice clips (primary content)
 * 2. SOUNDSCAPE — ambient background loops (heartbeat, breathing, ambient)
 * 3. SIGNATURE_SOUND — Pavlovian conditioning chime (brief, triggered)
 *
 * Each layer has independent volume control. All layers stop instantly
 * on safeword (stopAll). Uses ExoPlayer for pre-recorded audio and
 * Android TTS for dynamic text-to-speech.
 *
 * Lifecycle: create() binds to a session, release() frees all resources.
 * Must call release() when session ends or activity is destroyed.
 */
class AudioEngine(private val context: Context) {

    enum class AudioLayer { VOICE, SOUNDSCAPE, SIGNATURE_SOUND }

    data class VoiceConfig(
        val gender: String = "FEMALE", // "MALE" or "FEMALE"
        val speed: Float = 0.9f,       // 0.5 - 2.0
        val pitch: Float = 1.0f,       // 0.5 - 2.0
    )

    private var voicePlayer: ExoPlayer? = null
    private var soundscapePlayer: ExoPlayer? = null
    private var signaturePlayer: ExoPlayer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private val layerVolumes = mutableMapOf(
        AudioLayer.VOICE to 1.0f,
        AudioLayer.SOUNDSCAPE to 0.3f,
        AudioLayer.SIGNATURE_SOUND to 0.8f,
    )

    // ── Lifecycle ───────────────────────────────────────────

    /**
     * Initialize all audio resources for a session.
     */
    fun create() {
        voicePlayer = ExoPlayer.Builder(context).build()
        soundscapePlayer = ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
        }
        signaturePlayer = ExoPlayer.Builder(context).build()

        tts = TextToSpeech(context) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
            if (ttsReady) {
                tts?.language = Locale.US
            }
        }
    }

    /**
     * Release all audio resources. Call when session ends.
     */
    fun release() {
        stopAll()
        voicePlayer?.release()
        soundscapePlayer?.release()
        signaturePlayer?.release()
        tts?.shutdown()
        voicePlayer = null
        soundscapePlayer = null
        signaturePlayer = null
        tts = null
        ttsReady = false
    }

    // ── Playback ────────────────────────────────────────────

    /**
     * Play a pre-recorded audio clip from res/raw on the specified layer.
     */
    fun playPrerecorded(@RawRes resId: Int, layer: AudioLayer = AudioLayer.VOICE) {
        val player = getPlayer(layer) ?: return
        val uri = "android.resource://${context.packageName}/$resId"

        player.setMediaItem(MediaItem.fromUri(uri))
        player.volume = layerVolumes[layer] ?: 1.0f
        player.prepare()
        player.play()
    }

    /**
     * Speak text using Android TTS.
     *
     * @param text The text to speak
     * @param config Voice configuration (gender, speed, pitch)
     * @param onComplete Callback when speech finishes
     */
    fun speakTts(
        text: String,
        config: VoiceConfig = VoiceConfig(),
        onComplete: (() -> Unit)? = null,
    ) {
        if (!ttsReady) return
        val engine = tts ?: return

        engine.setSpeechRate(config.speed)
        engine.setPitch(config.pitch)

        // Select voice by gender preference
        val voices = engine.voices ?: emptySet()
        val preferredVoice = voices.firstOrNull { voice ->
            val nameLower = voice.name.lowercase()
            when (config.gender) {
                "MALE" -> nameLower.contains("male") && !nameLower.contains("female")
                else -> nameLower.contains("female")
            }
        }
        if (preferredVoice != null) {
            engine.voice = preferredVoice
        }

        val utteranceId = UUID.randomUUID().toString()

        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(id: String?) {
                _isSpeaking.value = false
                onComplete?.invoke()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(id: String?) {
                _isSpeaking.value = false
            }
        })

        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    /**
     * Stop all audio immediately. Used for safeword.
     */
    fun stopAll() {
        voicePlayer?.stop()
        soundscapePlayer?.stop()
        signaturePlayer?.stop()
        tts?.stop()
        _isSpeaking.value = false
    }

    /**
     * Set volume for a specific audio layer.
     *
     * @param layer Which layer to adjust
     * @param volume 0.0 (silent) to 1.0 (full)
     */
    fun setVolume(layer: AudioLayer, volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        layerVolumes[layer] = clamped
        getPlayer(layer)?.volume = clamped
    }

    /**
     * Check if any audio is currently playing.
     */
    fun isPlaying(): Boolean {
        return (voicePlayer?.isPlaying == true) ||
            (soundscapePlayer?.isPlaying == true) ||
            _isSpeaking.value
    }

    private fun getPlayer(layer: AudioLayer): ExoPlayer? {
        return when (layer) {
            AudioLayer.VOICE -> voicePlayer
            AudioLayer.SOUNDSCAPE -> soundscapePlayer
            AudioLayer.SIGNATURE_SOUND -> signaturePlayer
        }
    }
}
