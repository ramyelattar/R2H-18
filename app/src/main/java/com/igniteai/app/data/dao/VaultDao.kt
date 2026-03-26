package com.igniteai.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.igniteai.app.data.model.VaultItem
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {

    @Query("SELECT * FROM vault_items ORDER BY createdAt DESC")
    fun getAll(): Flow<List<VaultItem>>

    @Query("SELECT * FROM vault_items WHERE id = :id")
    suspend fun getById(id: String): VaultItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: VaultItem)

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM vault_items")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM vault_items")
    fun getCount(): Flow<Int>
}
