package com.igniteai.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tracks how the couple interacted with a content item.
 *
 * The adaptive algorithm uses these records to calculate
 * engagement scores and personalize content selection.
 *
 * Actions and their score weights:
 * - FAVORITED: +3 (strong positive signal)
 * - COMPLETED: +1 (mild positive)
 * - SKIPPED: -2 (negative — show less like this)
 * - BLOCKED: permanent exclusion (never show again)
 */
@Entity(
    tableName = "engagement_record",
    foreignKeys = [
        ForeignKey(
            entity = ContentItem::class,
            parentColumns = ["id"],
            childColumns = ["contentId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("contentId")],
)
data class EngagementRecord(
    @PrimaryKey val id: String,
    val contentId: String,          // FK → ContentItem.id
    val action: String,             // "COMPLETED", "SKIPPED", "FAVORITED", "BLOCKED"
    val timestamp: Long,
    val partnerId: String,          // Which partner performed the action
)
