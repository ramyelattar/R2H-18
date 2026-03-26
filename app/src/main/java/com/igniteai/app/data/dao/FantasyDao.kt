package com.igniteai.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.igniteai.app.data.model.FantasyProfile

/**
 * Data access for fantasy profiles.
 *
 * Each partner has one fantasy profile containing their questionnaire
 * answers. The FantasyRepository uses both profiles to compute
 * the couple's overlap (shared interests & boundaries).
 */
@Dao
interface FantasyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: FantasyProfile)

    @Query("SELECT * FROM fantasy_profile WHERE partnerId = :partnerId")
    suspend fun getProfile(partnerId: String): FantasyProfile?

    @Query("SELECT * FROM fantasy_profile WHERE partnerId IN (:partnerIds)")
    suspend fun getProfiles(partnerIds: List<String>): List<FantasyProfile>

    @Query("DELETE FROM fantasy_profile WHERE partnerId = :partnerId")
    suspend fun deleteProfile(partnerId: String)

    @Query("DELETE FROM fantasy_profile")
    suspend fun deleteAll()
}
