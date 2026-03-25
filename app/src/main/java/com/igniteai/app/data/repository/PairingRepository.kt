package com.igniteai.app.data.repository

import com.igniteai.app.data.dao.PairingDao
import com.igniteai.app.data.model.CoupleProfile
import com.igniteai.app.data.model.PairingData
import com.igniteai.app.data.model.Partner
import java.util.UUID

/**
 * Manages couple pairing state, encryption keys, and partner info.
 *
 * Responsible for:
 * - Storing pairing results after QR/invite code exchange
 * - Providing the shared encryption key for BLE/WiFi messages
 * - Handling unpair and re-pair flows
 * - Generating invite codes and QR payloads
 */
class PairingRepository(
    private val pairingDao: PairingDao,
) {

    /**
     * Store the result of a successful pairing.
     */
    suspend fun storePairingResult(
        coupleProfile: CoupleProfile,
        localPartner: Partner,
        remotePartner: Partner,
        sharedSecret: ByteArray,
        remotePublicKey: ByteArray,
        pairingMethod: String,
    ) {
        // Deactivate any existing pairing
        pairingDao.deactivateAll()

        val pairingData = PairingData(
            id = UUID.randomUUID().toString(),
            coupleId = coupleProfile.id,
            localPartnerId = localPartner.id,
            remotePartnerId = remotePartner.id,
            sharedSecret = sharedSecret,
            remotePublicKey = remotePublicKey,
            pairingMethod = pairingMethod,
            pairedAt = System.currentTimeMillis(),
            isActive = true,
        )
        pairingDao.storePairing(pairingData)
    }

    /**
     * Get the shared encryption key for P2P message encryption.
     * Returns null if not paired.
     */
    suspend fun getSharedEncryptionKey(): ByteArray? {
        return pairingDao.getSharedSecret()
    }

    /**
     * Get the active pairing data. Returns null if not paired.
     */
    suspend fun getActivePairing(): PairingData? {
        return pairingDao.getActivePairing()
    }

    /**
     * Check if this device is currently paired.
     */
    suspend fun isPaired(): Boolean {
        return pairingDao.getActivePairing() != null
    }

    /**
     * Unpair from current partner. Deactivates pairing and clears shared secret.
     */
    suspend fun unpair() {
        pairingDao.deactivateAll()
    }

    /**
     * Generate a 6-digit invite code for remote pairing.
     */
    fun generateInviteCode(): String {
        return (100000..999999).random().toString()
    }

    /**
     * Generate QR code payload containing partner info and public key.
     * The partner scans this to initiate key exchange.
     */
    fun generateQrPayload(partnerId: String, partnerName: String, publicKey: String): String {
        return """{"partnerId":"$partnerId","name":"$partnerName","publicKey":"$publicKey","app":"IgniteAI"}"""
    }
}
