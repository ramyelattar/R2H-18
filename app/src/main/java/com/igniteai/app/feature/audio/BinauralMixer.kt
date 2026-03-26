package com.igniteai.app.feature.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack

/**
 * Binaural audio mixer — splits audio into left/right channels.
 *
 * Creates an immersive "two voices" effect when wearing headphones:
 * - Left channel: commands, directives, instructions
 * - Right channel: praise, encouragement, affirmation
 *
 * This dual-channel separation creates a psychologically potent
 * experience — the listener perceives two distinct "presences."
 *
 * Safety: checks for headphone connection before enabling stereo split.
 * Without headphones, falls back to mono (both channels identical)
 * to prevent one-sided audio from a phone speaker.
 */
class BinauralMixer(private val context: Context) {

    companion object {
        private const val SAMPLE_RATE = 44100
    }

    private var audioTrack: AudioTrack? = null

    /**
     * Check if headphones are connected (wired or Bluetooth A2DP).
     */
    fun isHeadphoneConnected(): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        return devices.any { device ->
            device.type == android.media.AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                device.type == android.media.AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                device.type == android.media.AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                device.type == android.media.AudioDeviceInfo.TYPE_USB_HEADSET
        }
    }

    /**
     * Mix two mono audio buffers into a stereo output.
     *
     * @param leftChannel Audio data for left ear
     * @param rightChannel Audio data for right ear
     * @param volume Overall volume (0.0 - 1.0)
     */
    fun mixAndPlay(leftChannel: ShortArray, rightChannel: ShortArray, volume: Float = 1.0f) {
        val stereo = isHeadphoneConnected()
        val length = minOf(leftChannel.size, rightChannel.size)
        val mixed = ShortArray(length * 2)

        for (i in 0 until length) {
            if (stereo) {
                // True stereo: left → left ear, right → right ear
                mixed[i * 2] = leftChannel[i]
                mixed[i * 2 + 1] = rightChannel[i]
            } else {
                // Mono fallback: average both channels
                val avg = ((leftChannel[i].toInt() + rightChannel[i].toInt()) / 2).toShort()
                mixed[i * 2] = avg
                mixed[i * 2 + 1] = avg
            }
        }

        playBuffer(mixed, volume)
    }

    /**
     * Generate and play a simple binaural beat.
     *
     * Binaural beats create the perception of a third tone when
     * two slightly different frequencies are played in each ear.
     * The "beat" frequency = |left - right|.
     *
     * @param leftFreqHz Frequency for left ear (e.g., 200 Hz)
     * @param rightFreqHz Frequency for right ear (e.g., 210 Hz → 10 Hz beat)
     * @param durationMs Duration in milliseconds
     * @param volume 0.0 - 1.0
     */
    fun playBinauralBeat(
        leftFreqHz: Float,
        rightFreqHz: Float,
        durationMs: Long,
        volume: Float = 0.3f,
    ) {
        val numSamples = (SAMPLE_RATE * durationMs / 1000).toInt()
        val left = ShortArray(numSamples)
        val right = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            left[i] = (Short.MAX_VALUE * kotlin.math.sin(2.0 * Math.PI * leftFreqHz * t)).toInt().toShort()
            right[i] = (Short.MAX_VALUE * kotlin.math.sin(2.0 * Math.PI * rightFreqHz * t)).toInt().toShort()
        }

        mixAndPlay(left, right, volume)
    }

    /**
     * Stop playback and release resources.
     */
    fun release() {
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }

    private fun playBuffer(buffer: ShortArray, volume: Float) {
        release() // Release any existing track

        val bufferSize = buffer.size * 2 // Short = 2 bytes

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack?.apply {
            write(buffer, 0, buffer.size)
            setStereoVolume(volume, volume)
            play()
        }
    }
}
