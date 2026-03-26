package com.igniteai.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Record of a session — when it started, ended, and what happened.
 *
 * Used for:
 * - Consent audit trail (timestamps of both partners' consent)
 * - Session history
 * - Safeword tracking (for analytics, never shared)
 */
@Entity(tableName = "session_record")
data class SessionRecord(
    @PrimaryKey val id: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    val sessionType: String,        // "DARE", "SCENARIO", "CHALLENGE", "FREE"
    val timeLimitMinutes: Int,
    val consentPartner1At: Long? = null,
    val consentPartner2At: Long? = null,
    val safewordTriggered: Boolean = false,
    val safewordTriggeredBy: String? = null,  // Partner ID who triggered
    val safewordTriggeredAt: Long? = null,
)
