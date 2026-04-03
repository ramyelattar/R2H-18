package com.igniteai.app.feature.audio

import android.content.Context
import androidx.annotation.RawRes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Ambient background audio layer system.
 *
 * Manages multiple looping ambient tracks that play underneath
 * the primary voice content. Creates an immersive audio environment.
 *
 * Available layers:
 * - HEARTBEAT: rhythmic thud-thud synced to calm or elevated pace
 * - BREATHING: soft inhale/exhale ambient
 * - AMBIENT: environmental sounds (rain, fireplace, ocean)
 *
 * Each layer loops independently with its own volume.
 * Crossfade smoothly transitions between ambient themes.
 */
class SoundscapeLayer(private val context: Context) {

    enum class Layer { HEARTBEAT, BREATHING, AMBIENT }

    private val players = mutableMapOf<Layer, ExoPlayer>()
    private val volumes = mutableMapOf<Layer, Float>()

    /**
     * Start playing an ambient loop on a specific layer.
     *
     * @param layer Which ambient layer
     * @param resId Raw resource ID of the audio file
     * @param volume Initial volume (0.0 - 1.0)
     */
    fun play(layer: Layer, @RawRes resId: Int, volume: Float = 0.3f) {
        // Stop existing player on this layer
        stop(layer)

        val uri = "android.resource://${context.packageName}/$resId"
        val player = ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            setMediaItem(MediaItem.fromUri(uri))
            this.volume = volume
            prepare()
            play()
        }

        players[layer] = player
        volumes[layer] = volume
    }

    /**
     * Crossfade from one layer's content to another.
     *
     * @param fromLayer Layer to fade out
     * @param toLayer Layer to fade in
     * @param toResId New audio resource for the target layer
     * @param durationMs Crossfade duration in milliseconds
     * @param scope Coroutine scope for the animation
     */
    fun crossfade(
        fromLayer: Layer,
        toLayer: Layer,
        @RawRes toResId: Int,
        durationMs: Long = 2000,
        scope: CoroutineScope,
    ) {
        val fromPlayer = players[fromLayer]
        val fromVolume = volumes[fromLayer] ?: 0.3f

        // Start the new layer at zero volume
        play(toLayer, toResId, 0f)
        val toPlayer = players[toLayer]

        // Animate volumes
        scope.launch {
            val steps = 20
            val stepDelay = durationMs / steps

            for (i in 1..steps) {
                val progress = i.toFloat() / steps

                fromPlayer?.volume = fromVolume * (1f - progress)
                toPlayer?.volume = fromVolume * progress

                delay(stepDelay)
            }

            // Fully stop the old layer
            stop(fromLayer)
            volumes[toLayer] = fromVolume
        }
    }

    /**
     * Set volume for a specific layer.
     */
    fun setVolume(layer: Layer, volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        players[layer]?.volume = clamped
        volumes[layer] = clamped
    }

    /**
     * Stop a specific layer.
     */
    fun stop(layer: Layer) {
        players[layer]?.stop()
        players[layer]?.release()
        players.remove(layer)
        volumes.remove(layer)
    }

    /**
     * Stop all layers. Used for safeword.
     */
    fun stopAll() {
        players.values.forEach { player ->
            player.stop()
            player.release()
        }
        players.clear()
        volumes.clear()
    }

    /**
     * Check if any layer is playing.
     */
    fun isPlaying(): Boolean {
        return players.values.any { it.isPlaying }
    }
}
