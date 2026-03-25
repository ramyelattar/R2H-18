package com.igniteai.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a paired couple. Created during onboarding.
 *
 * Each device has exactly one active CoupleProfile.
 * If a user unpairs and re-pairs, a new profile is created.
 */
@Entity(tableName = "couple_profile")
data class CoupleProfile(
    @PrimaryKey val id: String,
    val createdAt: Long,            // Unix timestamp
    val pairingMethod: String,      // "QR" or "INVITE_CODE"
    val isActive: Boolean = true,
)
