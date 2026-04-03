package com.igniteai.app.feature.audio

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Lightweight UI sound manager.
 *
 * Uses ToneGenerator so we don't require bundled audio assets for
 * short action SFX and a subtle looped ambience cue.
 */
object UiSoundManager {
    private val actionTone = ToneGenerator(AudioManager.STREAM_MUSIC, 45)
    private val ambienceTone = ToneGenerator(AudioManager.STREAM_MUSIC, 18)
    private val scope = CoroutineScope(Dispatchers.Default)
    private var ambienceJob: Job? = null
    private val enabled = AtomicBoolean(true)

    fun setEnabled(value: Boolean) {
        enabled.set(value)
        if (!value) stopBackgroundMusic()
    }

    fun playAction() {
        if (!enabled.get()) return
        actionTone.startTone(ToneGenerator.TONE_PROP_BEEP2, 80)
    }

    fun playSuccess() {
        if (!enabled.get()) return
        actionTone.startTone(ToneGenerator.TONE_PROP_ACK, 130)
    }

    fun playError() {
        if (!enabled.get()) return
        actionTone.startTone(ToneGenerator.TONE_PROP_NACK, 180)
    }

    /**
     * Starts a subtle repeating ambience tone to emulate background music.
     */
    fun startBackgroundMusic() {
        if (!enabled.get() || ambienceJob?.isActive == true) return
        ambienceJob = scope.launch {
            while (isActive && enabled.get()) {
                ambienceTone.startTone(ToneGenerator.TONE_CDMA_LOW_PBX_L, 650)
                delay(4200)
            }
        }
    }

    fun stopBackgroundMusic() {
        ambienceJob?.cancel()
        ambienceJob = null
    }
}
