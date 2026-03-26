package com.igniteai.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.igniteai.app.data.model.ContentItem

/**
 * Data access for the content library (dares, texts, audio scripts, scenarios).
 *
 * Used by:
 * - ContentRepository: bulk insert on first launch
 * - Adaptive algorithm: query by tone, intensity, level, tags
 * - Home screen: daily dare selection
 * - Content screens: browse and filter
 */
@Dao
interface ContentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ContentItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ContentItem)

    @Query("SELECT * FROM content_item WHERE id = :id")
    suspend fun getById(id: String): ContentItem?

    @Query("SELECT * FROM content_item WHERE type = :type")
    suspend fun getByType(type: String): List<ContentItem>

    @Query("SELECT * FROM content_item WHERE type = :type AND tone = :tone")
    suspend fun getByTypeAndTone(type: String, tone: String): List<ContentItem>

    @Query("SELECT * FROM content_item WHERE type = :type AND level = :level")
    suspend fun getByTypeAndLevel(type: String, level: String): List<ContentItem>

    @Query(
        """
        SELECT * FROM content_item
        WHERE type = :type
        AND level IN (:levels)
        AND intensity BETWEEN :minIntensity AND :maxIntensity
        ORDER BY RANDOM()
        LIMIT :limit
        """
    )
    suspend fun getRandomContent(
        type: String,
        levels: List<String>,
        minIntensity: Int = 1,
        maxIntensity: Int = 10,
        limit: Int = 1,
    ): List<ContentItem>

    @Query("SELECT COUNT(*) FROM content_item")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM content_item WHERE type = :type")
    suspend fun getCountByType(type: String): Int

    @Query("DELETE FROM content_item")
    suspend fun deleteAll()
}
