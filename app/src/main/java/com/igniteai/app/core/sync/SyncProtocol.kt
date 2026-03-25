package com.igniteai.app.core.sync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Defines all message types exchanged between paired devices.
 *
 * Every P2P message follows this structure:
 * - type: identifies the message kind (auto-handled by sealed class)
 * - timestamp: when the message was created
 * - senderId: which partner sent it
 * - payload: message-specific data
 *
 * Messages are serialized to JSON, then encrypted with the couple's
 * shared secret before transmission over BLE/WiFi Direct.
 */

private val json = Json {
    ignoreUnknownKeys = true
    classDiscriminator = "type"
}

@Serializable
sealed class SyncMessage {
    abstract val timestamp: Long
    abstract val senderId: String

    /**
     * Serialize to JSON string for transmission.
     */
    fun toJson(): String = json.encodeToString(serializer(), this)

    companion object {
        /**
         * Deserialize from JSON string.
         */
        fun fromJson(jsonString: String): SyncMessage =
            json.decodeFromString(serializer(), jsonString)
    }
}

// ── Pairing ─────────────────────────────────────────────────

@Serializable
@SerialName("pairing_request")
data class PairingRequest(
    override val timestamp: Long,
    override val senderId: String,
    val partnerName: String,
    val publicKey: String,          // Base64-encoded public key for ECDH
) : SyncMessage()

@Serializable
@SerialName("pairing_response")
data class PairingResponse(
    override val timestamp: Long,
    override val senderId: String,
    val partnerName: String,
    val publicKey: String,
    val accepted: Boolean,
) : SyncMessage()

// ── Consent ─────────────────────────────────────────────────

@Serializable
@SerialName("consent_confirm")
data class ConsentConfirm(
    override val timestamp: Long,
    override val senderId: String,
    val sessionId: String,
) : SyncMessage()

// ── Session Control ─────────────────────────────────────────

@Serializable
@SerialName("safeword_trigger")
data class SafewordTrigger(
    override val timestamp: Long,
    override val senderId: String,
    val sessionId: String,
) : SyncMessage()

@Serializable
@SerialName("check_in_request")
data class CheckInRequest(
    override val timestamp: Long,
    override val senderId: String,
    val sessionId: String,
) : SyncMessage()

@Serializable
@SerialName("check_in_response")
data class CheckInResponse(
    override val timestamp: Long,
    override val senderId: String,
    val sessionId: String,
    val continueSession: Boolean,
) : SyncMessage()

// ── Content Sync ────────────────────────────────────────────

@Serializable
@SerialName("content_sync")
data class ContentSync(
    override val timestamp: Long,
    override val senderId: String,
    val contentId: String,
    val action: String,             // "SHOW", "ADVANCE", "CHOICE"
    val data: String = "",          // Action-specific payload
) : SyncMessage()

// ── Haptic & Audio Triggers ─────────────────────────────────

@Serializable
@SerialName("haptic_trigger")
data class HapticTrigger(
    override val timestamp: Long,
    override val senderId: String,
    val patternName: String,
    val intensity: Float = 1.0f,
) : SyncMessage()

@Serializable
@SerialName("audio_trigger")
data class AudioTrigger(
    override val timestamp: Long,
    override val senderId: String,
    val audioRef: String,
    val action: String = "PLAY",    // "PLAY", "STOP"
) : SyncMessage()

// ── Heart Rate ──────────────────────────────────────────────

@Serializable
@SerialName("heart_rate_update")
data class HeartRateUpdate(
    override val timestamp: Long,
    override val senderId: String,
    val bpm: Int,
) : SyncMessage()

// ── D/s Control Transfer ────────────────────────────────────

@Serializable
@SerialName("control_command")
data class ControlCommand(
    override val timestamp: Long,
    override val senderId: String,
    val command: String,            // "SET_MODE", "SEND_TEXT", "TRIGGER_HAPTIC", "TRIGGER_AUDIO", "SWAP_ROLES"
    val payload: String = "",
) : SyncMessage()

// ── Vault ───────────────────────────────────────────────────

@Serializable
@SerialName("vault_unlock_request")
data class VaultUnlockRequest(
    override val timestamp: Long,
    override val senderId: String,
    val confirmed: Boolean,
) : SyncMessage()

// ── Pavlovian Trigger ───────────────────────────────────────

@Serializable
@SerialName("pavlovian_trigger")
data class PavlovianTrigger(
    override val timestamp: Long,
    override val senderId: String,
) : SyncMessage()

// ── Challenge Sync ──────────────────────────────────────────

@Serializable
@SerialName("challenge_sync")
data class ChallengeSync(
    override val timestamp: Long,
    override val senderId: String,
    val challengeId: String,
    val action: String,             // "START", "STEP_COMPLETE", "FINISHED"
    val score: Int = 0,
) : SyncMessage()
