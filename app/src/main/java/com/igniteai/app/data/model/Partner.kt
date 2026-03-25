package com.igniteai.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A partner in a couple. Each couple has exactly two partners.
 *
 * @property isLocal True if this partner is on the current device.
 *   Used to distinguish "me" from "my partner" in the UI.
 */
@Entity(
    tableName = "partner",
    foreignKeys = [
        ForeignKey(
            entity = CoupleProfile::class,
            parentColumns = ["id"],
            childColumns = ["coupleId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("coupleId")],
)
data class Partner(
    @PrimaryKey val id: String,
    val displayName: String,
    val isLocal: Boolean,           // True = this device's user
    val coupleId: String,           // FK → CoupleProfile.id
    val createdAt: Long,
)
