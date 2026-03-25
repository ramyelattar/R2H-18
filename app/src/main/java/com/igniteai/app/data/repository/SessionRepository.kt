package com.igniteai.app.data.repository

import com.igniteai.app.data.dao.SessionDao
import com.igniteai.app.data.model.SessionRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

/**
 * Manages session lifecycle: creation, consent tracking, safeword, and ending.
 *
 * Session flow:
 *   1. startSession() → creates SessionRecord with start time
 *   2. recordConsent(partnerId) → logs each partner's biometric consent
 *   3. bothConsented() → checks both consented within 60 seconds
 *   4. Session runs with timer...
 *   5. triggerSafeword() OR endSession() → records end time
 *
 * The 60-second consent window prevents one partner from authenticating
 * and walking away — both must be present and willing.
 */
class SessionRepository(
    private val sessionDao: SessionDao,
) {

    companion object {
        /** Both partners must consent within this window (milliseconds). */
        const val CONSENT_WINDOW_MS = 60_000L
    }

    private var _currentSession: SessionRecord? = null
    val currentSession: SessionRecord? get() = _currentSession

    private val _timeRemainingMs = MutableStateFlow(0L)
    val timeRemainingMs: StateFlow<Long> = _timeRemainingMs

    /**
     * Create a new session. Does NOT start it — consent must happen first.
     *
     * @param sessionType Type of session ("DARE", "SCENARIO", "CHALLENGE", "FREE")
     * @param timeLimitMinutes Duration limit (shorter of both partners' preferences)
     * @return The created session ID
     */
    suspend fun startSession(sessionType: String, timeLimitMinutes: Int): String {
        val session = SessionRecord(
            id = UUID.randomUUID().toString(),
            startedAt = System.currentTimeMillis(),
            sessionType = sessionType,
            timeLimitMinutes = timeLimitMinutes,
        )
        sessionDao.insert(session)
        _currentSession = session
        _timeRemainingMs.value = timeLimitMinutes * 60_000L
        return session.id
    }

    /**
     * Record one partner's biometric consent.
     *
     * @param partnerId Which partner consented
     * @param isPartner1 True for first partner, false for second
     */
    suspend fun recordConsent(partnerId: String, isPartner1: Boolean) {
        val session = _currentSession ?: return
        val now = System.currentTimeMillis()

        val updated = if (isPartner1) {
            session.copy(consentPartner1At = now)
        } else {
            session.copy(consentPartner2At = now)
        }

        sessionDao.update(updated)
        _currentSession = updated
    }

    /**
     * Check if both partners have consented within the 60-second window.
     *
     * Returns true only if:
     * - Both consent timestamps are non-null
     * - The gap between them is ≤ 60 seconds
     */
    fun bothConsented(): Boolean {
        val session = _currentSession ?: return false
        val consent1 = session.consentPartner1At ?: return false
        val consent2 = session.consentPartner2At ?: return false

        return kotlin.math.abs(consent1 - consent2) <= CONSENT_WINDOW_MS
    }

    /**
     * Trigger safeword — immediately ends the session.
     *
     * @param triggeredBy Partner ID who triggered the safeword
     */
    suspend fun triggerSafeword(triggeredBy: String) {
        val session = _currentSession ?: return
        val now = System.currentTimeMillis()

        val updated = session.copy(
            endedAt = now,
            safewordTriggered = true,
            safewordTriggeredBy = triggeredBy,
            safewordTriggeredAt = now,
        )

        sessionDao.update(updated)
        _currentSession = updated
    }

    /**
     * End session normally (time expired or user chose to end).
     */
    suspend fun endSession() {
        val session = _currentSession ?: return

        val updated = session.copy(endedAt = System.currentTimeMillis())
        sessionDao.update(updated)
        _currentSession = updated
    }

    /**
     * Update the remaining time (called by ViewModel timer).
     */
    fun updateTimeRemaining(remainingMs: Long) {
        _timeRemainingMs.value = remainingMs
    }

    /**
     * Get session duration in minutes for the current session.
     */
    fun getSessionDurationMinutes(): Int {
        val session = _currentSession ?: return 0
        val endTime = session.endedAt ?: System.currentTimeMillis()
        return ((endTime - session.startedAt) / 60_000).toInt()
    }

    /**
     * Get the most recent session for display on cool-down screen.
     */
    suspend fun getLatestSession(): SessionRecord? {
        return sessionDao.getLatestSession()
    }

    /**
     * Clear current session reference (after cool-down).
     */
    fun clearCurrentSession() {
        _currentSession = null
    }
}
