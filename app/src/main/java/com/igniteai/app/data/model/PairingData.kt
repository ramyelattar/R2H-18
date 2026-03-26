package com.igniteai.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores pairing state and encryption keys for couple sync.
 *
 * Created during the pairing flow (QR scan or invite code).
 * The sharedSecret is used to encrypt all BLE/WiFi Direct messages
 * between the two devices.
 */
@Entity(tableName = "pairing_data")
data class PairingData(
    @PrimaryKey val id: String,
    val coupleId: String,
    val localPartnerId: String,
    val remotePartnerId: String,
    val sharedSecret: ByteArray,    // Derived during key exchange (ECDH)
    val remotePublicKey: ByteArray, // Partner's public key
    val pairingMethod: String,      // "QR" or "INVITE_CODE"
    val pairedAt: Long,
    val isActive: Boolean = true,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PairingData) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
