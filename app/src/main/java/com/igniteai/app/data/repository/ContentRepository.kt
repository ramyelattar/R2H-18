package com.igniteai.app.data.repository

import android.content.Context
import com.igniteai.app.core.preferences.AppPreferences
import com.igniteai.app.data.dao.ContentDao
import com.igniteai.app.data.dao.EngagementDao
import com.igniteai.app.data.model.ContentItem
import com.igniteai.app.data.model.EngagementRecord
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * Central repository for all content: dares, texts, audio scripts, scenarios.
 *
 * Responsibilities:
 * - Loading starter content from JSON assets on first launch
 * - Querying content by type, tone, intensity, and level
 * - Recording engagement (favorites, completions, skips, blocks)
 * - Providing data for the adaptive content algorithm
 *
 * Content flow:
 *   assets/content/*.json → parse → Room (content_item table)
 *   User interaction → Room (engagement_record table)
 *   Adaptive algorithm queries both tables to pick next content
 */
class ContentRepository(
    private val contentDao: ContentDao,
    private val engagementDao: EngagementDao,
    private val preferences: AppPreferences,
) {

    companion object {
        /** Increment this when updating starter content JSON files. */
        const val CURRENT_CONTENT_VERSION = 1

        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    // ── Content Loading ─────────────────────────────────────

    /**
     * Load starter content from JSON assets into Room on first launch.
     *
     * Only runs if the stored content version is less than CURRENT_CONTENT_VERSION.
     * This is called from IgniteAIApp.onCreate() to ensure content is available
     * before any screen tries to display it.
     *
     * @param context Application context for accessing assets
     */
    suspend fun loadContentLibrary(context: Context) {
        val storedVersion = preferences.contentLoadedVersion.first()

        if (storedVersion >= CURRENT_CONTENT_VERSION) {
            return // Already loaded this version
        }

        // Clear existing content for clean reload
        contentDao.deleteAll()

        // Load dares
        val daresJson = context.assets.open("content/dares.json")
            .bufferedReader()
            .use { it.readText() }
        val dares = json.decodeFromString<List<ContentJsonItem>>(daresJson)
        contentDao.insertAll(dares.map { it.toContentItem() })

        // Load texts
        val textsJson = context.assets.open("content/texts.json")
            .bufferedReader()
            .use { it.readText() }
        val texts = json.decodeFromString<List<ContentJsonItem>>(textsJson)
        contentDao.insertAll(texts.map { it.toContentItem() })

        // Mark content as loaded
        preferences.setContentLoadedVersion(CURRENT_CONTENT_VERSION)
    }

    // ── Content Queries ─────────────────────────────────────

    /**
     * Get a random dare, respecting the user's unlocked level.
     *
     * @param hasFire Whether the user has unlocked Fire tier
     * @param minIntensity Minimum intensity filter (1-10)
     * @param maxIntensity Maximum intensity filter (1-10)
     */
    suspend fun getRandomDare(
        hasFire: Boolean = false,
        minIntensity: Int = 1,
        maxIntensity: Int = 5,
    ): ContentItem? {
        val levels = if (hasFire) listOf("SPARK", "FIRE") else listOf("SPARK")
        val blockedIds = engagementDao.getBlockedContentIds()

        val candidates = contentDao.getRandomContent(
            type = "DARE",
            levels = levels,
            minIntensity = minIntensity,
            maxIntensity = maxIntensity,
            limit = 5, // Get a few candidates to filter
        )

        return candidates.firstOrNull { it.id !in blockedIds }
    }

    /**
     * Get a random text message template.
     */
    suspend fun getRandomText(
        hasFire: Boolean = false,
        tone: String? = null,
    ): ContentItem? {
        val levels = if (hasFire) listOf("SPARK", "FIRE") else listOf("SPARK")

        return if (tone != null) {
            contentDao.getByTypeAndTone("TEXT", tone)
                .filter { it.level in levels }
                .randomOrNull()
        } else {
            contentDao.getRandomContent(
                type = "TEXT",
                levels = levels,
                limit = 1,
            ).firstOrNull()
        }
    }

    /**
     * Get all content of a specific type, filtered by level access.
     */
    suspend fun getContentByType(type: String, hasFire: Boolean = false): List<ContentItem> {
        return if (hasFire) {
            contentDao.getByType(type)
        } else {
            contentDao.getByTypeAndLevel(type, "SPARK")
        }
    }

    /**
     * Get a specific content item by ID.
     */
    suspend fun getContentById(id: String): ContentItem? {
        return contentDao.getById(id)
    }

    /**
     * Get total number of content items loaded.
     */
    suspend fun getContentCount(): Int {
        return contentDao.getCount()
    }

    // ── Engagement Tracking ─────────────────────────────────

    /**
     * Record that a content item was completed by a partner.
     */
    suspend fun recordCompletion(contentId: String, partnerId: String) {
        engagementDao.insert(
            EngagementRecord(
                id = UUID.randomUUID().toString(),
                contentId = contentId,
                action = "COMPLETED",
                timestamp = System.currentTimeMillis(),
                partnerId = partnerId,
            )
        )
    }

    /**
     * Record that a content item was skipped by a partner.
     */
    suspend fun recordSkip(contentId: String, partnerId: String) {
        engagementDao.insert(
            EngagementRecord(
                id = UUID.randomUUID().toString(),
                contentId = contentId,
                action = "SKIPPED",
                timestamp = System.currentTimeMillis(),
                partnerId = partnerId,
            )
        )
    }

    /**
     * Toggle favorite status for a content item.
     */
    suspend fun recordFavorite(contentId: String, partnerId: String) {
        engagementDao.insert(
            EngagementRecord(
                id = UUID.randomUUID().toString(),
                contentId = contentId,
                action = "FAVORITED",
                timestamp = System.currentTimeMillis(),
                partnerId = partnerId,
            )
        )
    }

    /**
     * Block a content item — it will never be shown again.
     */
    suspend fun blockContent(contentId: String, partnerId: String) {
        engagementDao.insert(
            EngagementRecord(
                id = UUID.randomUUID().toString(),
                contentId = contentId,
                action = "BLOCKED",
                timestamp = System.currentTimeMillis(),
                partnerId = partnerId,
            )
        )
    }

    /**
     * Get all favorited content IDs for a partner.
     */
    suspend fun getFavorites(partnerId: String): List<String> {
        return engagementDao.getFavoritedContentIds(partnerId)
    }
}

// ── JSON Parsing Model ──────────────────────────────────

/**
 * Intermediate model for parsing content JSON files.
 *
 * This mirrors the JSON structure in assets/content/*.json.
 * Converted to ContentItem (Room entity) via toContentItem().
 *
 * Why a separate class? Room entities use @Entity annotations
 * and kotlinx.serialization uses @Serializable — keeping them
 * separate avoids annotation conflicts and keeps each clean.
 */
@Serializable
data class ContentJsonItem(
    val id: String,
    val type: String,
    val tone: String,
    val intensity: Int,
    val level: String,
    val duration: String = "QUICK",
    val title: String = "",
    val body: String,
    val audioRef: String? = null,
    val tags: String = "",
) {
    fun toContentItem() = ContentItem(
        id = id,
        type = type,
        tone = tone,
        intensity = intensity,
        level = level,
        duration = duration,
        title = title,
        body = body,
        audioRef = audioRef,
        tags = tags,
    )
}
