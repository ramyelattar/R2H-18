package com.igniteai.app.data.repository

import com.igniteai.app.data.dao.VaultDao
import com.igniteai.app.data.model.VaultItem
import com.igniteai.app.feature.vault.VaultEncryption
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class VaultRepository(
    private val vaultDao: VaultDao,
) {
    fun getAllItems(): Flow<List<VaultItem>> = vaultDao.getAll()

    fun getItemCount(): Flow<Int> = vaultDao.getCount()

    suspend fun addItem(title: String, body: String, category: String) {
        val encrypted = VaultEncryption.encrypt(body.toByteArray(Charsets.UTF_8))
        val item = VaultItem(
            id = UUID.randomUUID().toString(),
            title = title,
            category = category,
            encryptedBody = encrypted.ciphertext,
            iv = encrypted.iv,
            createdAt = System.currentTimeMillis(),
        )
        vaultDao.insert(item)
    }

    suspend fun decryptItem(item: VaultItem): String {
        val payload = VaultEncryption.EncryptedPayload(
            iv = item.iv,
            ciphertext = item.encryptedBody,
        )
        return VaultEncryption.decrypt(payload).toString(Charsets.UTF_8)
    }

    suspend fun deleteItem(id: String) = vaultDao.delete(id)

    suspend fun wipeVault() = vaultDao.deleteAll()
}
