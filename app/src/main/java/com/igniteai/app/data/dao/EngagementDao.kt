package com.igniteai.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.igniteai.app.data.model.EngagementRecord

/**
 * Data access for engagement tracking (how couples interact with content).
 *
 * The adaptive algorithm uses engagement data to calculate scores:
 *   score = (favorites × 3) + (completions × 1) - (skips × 2)
 *
 * Content with BLOCKED action is permanently excluded.
 */
@Dao
interface EngagementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: EngagementRecord)

    @Query("SELECT * FROM engagement_record WHERE contentId = :contentId ORDER BY timestamp DESC")
    suspend fun getByContentId(contentId: String): List<EngagementRecord>

    @Query("SELECT * FROM engagement_record WHERE partnerId = :partnerId ORDER BY timestamp DESC")
    suspend fun getByPartnerId(partnerId: String): List<EngagementRecord>

    /**
     * Get IDs of all content the couple has blocked (never show again).
     */
    @Query("SELECT DISTINCT contentId FROM engagement_record WHERE action = 'BLOCKED'")
    suspend fun getBlockedContentIds(): List<String>

    /**
     * Get IDs of content completed recently (within last N days) to avoid repeats.
     */
    @Query(
        """
        SELECT DISTINCT contentId FROM engagement_record
        WHERE action = 'COMPLETED'
        AND timestamp > :sinceTimestamp
        """
    )
    suspend fun getRecentlyCompletedIds(sinceTimestamp: Long): List<String>

    /**
     * Count engagement actions by type for a specific content item.
     * Used by the adaptive algorithm to calculate engagement scores.
     */
    @Query(
        """
        SELECT COUNT(*) FROM engagement_record
        WHERE contentId = :contentId AND action = :action
        """
    )
    suspend fun countByAction(contentId: String, action: String): Int

    /**
     * Get all favorited content IDs for a partner (for the favorites list).
     */
    @Query(
        """
        SELECT DISTINCT contentId FROM engagement_record
        WHERE action = 'FAVORITED' AND partnerId = :partnerId
        """
    )
    suspend fun getFavoritedContentIds(partnerId: String): List<String>

    @Query("DELETE FROM engagement_record")
    suspend fun deleteAll()
}
