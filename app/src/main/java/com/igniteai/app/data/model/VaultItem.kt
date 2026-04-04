package com.igniteai.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * An item stored in the Forbidden Vault.
 *
 * Content is encrypted with a separate key from the main database.
 */
@Entity(tableName = "vault_item")
data class VaultItem(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val encryptedBody: ByteArray,
    val iv: ByteArray,
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
