package com.igniteai.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.igniteai.app.data.model.PairingData

/**
 * Data access for pairing state and encryption keys.
 */
@Dao
interface PairingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun storePairing(data: PairingData)

    @Query("SELECT * FROM pairing_data WHERE isActive = 1 LIMIT 1")
    suspend fun getActivePairing(): PairingData?

    @Query("SELECT sharedSecret FROM pairing_data WHERE isActive = 1 LIMIT 1")
    suspend fun getSharedSecret(): ByteArray?

    @Query("UPDATE pairing_data SET isActive = 0 WHERE id = :id")
    suspend fun deactivatePairing(id: String)

    @Query("UPDATE pairing_data SET isActive = 0")
    suspend fun deactivateAll()

    @Query("DELETE FROM pairing_data")
    suspend fun deleteAll()
}
