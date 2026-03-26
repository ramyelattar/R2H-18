package com.igniteai.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * An item stored in the Forbidden Vault.
 *
 * Content is encrypted with a SEPARATE key from the main database,
 * so even if the main DB key is compromised, vault data stays protected.
 *
 * Types: custom dares, voice notes, saved content from sessions.
 */
@Entity(tableName = "vault_item")
data class VaultItem(
    @PrimaryKey val id: String,
    val type: String,               // "DARE", "VOICE_NOTE", "SAVED_CONTENT"
    val encryptedContent: ByteArray, // Encrypted with vault-specific key
    val createdBy: String,          // Partner ID who created it
    val createdAt: Long,
) {
    // ByteArray needs custom equals/hashCode
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VaultItem) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
