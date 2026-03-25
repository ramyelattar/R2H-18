package com.igniteai.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A partner's private fantasy questionnaire answers.
 *
 * PRIVACY: Raw answers are NEVER shown to the other partner.
 * Only the overlap (shared interests) is used by the content engine.
 *
 * Answers are stored as a JSON string for flexibility —
 * questions can be added/changed without database migration.
 */
@Entity(
    tableName = "fantasy_profile",
    foreignKeys = [
        ForeignKey(
            entity = Partner::class,
            parentColumns = ["id"],
            childColumns = ["partnerId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("partnerId")],
)
data class FantasyProfile(
    @PrimaryKey val id: String,
    val partnerId: String,          // FK → Partner.id
    val answersJson: String,        // JSON: Map<questionId, answer>
    val createdAt: Long,
    val updatedAt: Long,
)
