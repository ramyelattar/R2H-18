package com.igniteai.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.igniteai.app.data.model.LicenseKey

@Dao
interface LicenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(license: LicenseKey)

    @Query("SELECT * FROM license_key WHERE level = :level LIMIT 1")
    suspend fun getByLevel(level: String): LicenseKey?

    @Query("SELECT COUNT(*) > 0 FROM license_key WHERE level = :level")
    suspend fun isLevelUnlocked(level: String): Boolean

    @Query("DELETE FROM license_key")
    suspend fun deleteAll()
}
