package com.igniteai.app.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Manages all encryption operations using Android Keystore.
 *
 * The Keystore is hardware-backed on modern devices — keys are stored
 * in the phone's security chip (TEE/Strongbox) and never leave it.
 * All encryption/decryption happens inside the secure hardware.
 *
 * Two key aliases:
 * - DATABASE_KEY: generates the passphrase for SQLCipher
 * - DATA_KEY: encrypts/decrypts individual data (vault contents, etc.)
 */
class EncryptionManager {

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val DATABASE_KEY_ALIAS = "igniteai_db_key"
        private const val DATA_KEY_ALIAS = "igniteai_data_key"
        private const val GCM_TAG_LENGTH = 128
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }

    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
        load(null)
    }

    /**
     * Generates a stable 32-byte key for SQLCipher database encryption.
     *
     * The key is derived from the Keystore-stored master key by encrypting
     * a fixed seed. This produces the same passphrase every time (deterministic)
     * so the database can be reopened after app restarts.
     */
    fun generateDatabaseKey(): ByteArray {
        val masterKey = getOrCreateKey(DATABASE_KEY_ALIAS)
        // Use the key's encoded form as the database passphrase.
        // Since Keystore keys can't be exported, we derive a passphrase
        // by encrypting a known value with a fixed IV.
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val fixedIv = ByteArray(12) // All zeros — deterministic for same key
        cipher.init(Cipher.ENCRYPT_MODE, masterKey, GCMParameterSpec(GCM_TAG_LENGTH, fixedIv))
        val seed = "igniteai_db_passphrase_seed".toByteArray()
        return cipher.doFinal(seed)
    }

    /**
     * Encrypts arbitrary data. Returns IV + ciphertext concatenated.
     *
     * @param plaintext Data to encrypt
     * @return ByteArray: [12-byte IV][ciphertext]
     */
    fun encrypt(plaintext: ByteArray): ByteArray {
        val key = getOrCreateKey(DATA_KEY_ALIAS)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv // GCM generates a random IV
        val ciphertext = cipher.doFinal(plaintext)
        return iv + ciphertext
    }

    /**
     * Decrypts data that was encrypted with [encrypt].
     *
     * @param data ByteArray: [12-byte IV][ciphertext]
     * @return Decrypted plaintext
     */
    fun decrypt(data: ByteArray): ByteArray {
        val key = getOrCreateKey(DATA_KEY_ALIAS)
        val iv = data.copyOfRange(0, 12)
        val ciphertext = data.copyOfRange(12, data.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return cipher.doFinal(ciphertext)
    }

    /**
     * Deletes all encryption keys. Used by PanicWipeManager.
     * After this, the database and encrypted data become unreadable.
     */
    fun deleteAllKeys() {
        if (keyStore.containsAlias(DATABASE_KEY_ALIAS)) {
            keyStore.deleteEntry(DATABASE_KEY_ALIAS)
        }
        if (keyStore.containsAlias(DATA_KEY_ALIAS)) {
            keyStore.deleteEntry(DATA_KEY_ALIAS)
        }
    }

    /**
     * Gets existing key or creates a new one in the Keystore.
     */
    private fun getOrCreateKey(alias: String): SecretKey {
        keyStore.getKey(alias, null)?.let { return it as SecretKey }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER,
        )
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(alias != DATABASE_KEY_ALIAS) // DB key needs deterministic
                .build()
        )
        return keyGenerator.generateKey()
    }
}
