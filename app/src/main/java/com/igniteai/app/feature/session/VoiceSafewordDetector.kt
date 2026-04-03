package com.igniteai.app.feature.session

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Listens for a spoken safeword during active sessions.
 *
 * Uses Android's on-device SpeechRecognizer for privacy —
 * no audio data leaves the device. Continuously listens
 * and auto-restarts when recognition ends.
 *
 * The safeword match is case-insensitive and checks if any
 * recognized phrase contains the safeword as a substring,
 * tolerating natural speech ("I said red" triggers "red").
 */
class VoiceSafewordDetector(
    private val context: Context,
    private val safeword: String,
    private val onSafewordDetected: () -> Unit,
) {
    private var recognizer: SpeechRecognizer? = null
    private var isListening = false

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive

    private val recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _isActive.value = true
        }

        override fun onResults(results: Bundle?) {
            checkResults(results)
            restartListening()
        }

        override fun onPartialResults(partialResults: Bundle?) {
            checkResults(partialResults)
        }

        override fun onError(error: Int) {
            // Restart on recoverable errors (no speech, timeout, etc.)
            when (error) {
                SpeechRecognizer.ERROR_NO_MATCH,
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
                SpeechRecognizer.ERROR_CLIENT -> restartListening()
                else -> {
                    _isActive.value = false
                    // Non-recoverable: stop listening
                }
            }
        }

        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun checkResults(results: Bundle?) {
        val matches = results
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?: return

        val target = safeword.lowercase()
        if (matches.any { it.lowercase().contains(target) }) {
            stop()
            onSafewordDetected()
        }
    }

    fun start() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return
        if (isListening) return

        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(listener)
        }
        isListening = true
        recognizer?.startListening(recognitionIntent)
    }

    private fun restartListening() {
        if (!isListening) return
        recognizer?.startListening(recognitionIntent)
    }

    fun stop() {
        isListening = false
        _isActive.value = false
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null
    }
}
