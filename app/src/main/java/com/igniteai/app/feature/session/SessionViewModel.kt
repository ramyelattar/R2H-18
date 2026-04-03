package com.igniteai.app.feature.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igniteai.app.core.preferences.SessionSettings
import com.igniteai.app.data.repository.SessionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Manages the full session lifecycle state machine.
 *
 * State transitions:
 *   IDLE → CONSENT_GATE → WAITING_PARTNER → ACTIVE → CHECK_IN → ACTIVE → COOL_DOWN
 *                                               ↓
 *                                          DENY_DELAY → ACTIVE
 *
 * From ANY active state:
 *   SAFEWORD → COOL_DOWN (within 500ms, no confirmation)
 *
 * The ViewModel coordinates:
 * - Dual biometric consent with 60-second window
 * - Session countdown timer with check-in at 50% and warning at 90%
 * - Safeword handling (instant, unconditional)
 * - Deny & Delay pauses (anticipation mechanic)
 * - Partner sync messages via ConnectionManager
 */
class SessionViewModel(
    private val sessionRepository: SessionRepository,
    private val preferences: SessionSettings,
) : ViewModel() {

    /**
     * All possible session states. The UI renders different screens
     * based on the current state.
     */
    enum class SessionState {
        IDLE,
        CONSENT_GATE,
        WAITING_PARTNER_CONSENT,
        ACTIVE,
        CHECK_IN,
        DENY_DELAY,
        COOL_DOWN,
    }

    data class SessionUiState(
        val state: SessionState = SessionState.IDLE,
        val localConsented: Boolean = false,
        val partnerConsented: Boolean = false,
        val timeRemainingMs: Long = 0L,
        val timeLimitMinutes: Int = 30,
        val sessionType: String = "FREE",
        val checkInRequired: Boolean = false,
        val denyDelayRemainingMs: Long = 0L,
        val safewordTriggered: Boolean = false,
        val sessionDurationMinutes: Int = 0,
        val error: String? = null,
    )

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState

    private var timerJob: Job? = null
    private var checkInJob: Job? = null
    private var denyDelayJob: Job? = null
    private var checkInTriggered = false

    // ── Session Start ───────────────────────────────────────

    /**
     * Initiate a new session. Moves to CONSENT_GATE state.
     */
    fun initiateSession(sessionType: String = "FREE") {
        viewModelScope.launch {
            val timeLimit = preferences.sessionTimeLimit.first()

            _uiState.update {
                it.copy(
                    state = SessionState.CONSENT_GATE,
                    sessionType = sessionType,
                    timeLimitMinutes = timeLimit,
                    localConsented = false,
                    partnerConsented = false,
                    safewordTriggered = false,
                    error = null,
                )
            }

            sessionRepository.startSession(sessionType, timeLimit)
        }
    }

    /**
     * Record local partner's biometric consent.
     */
    fun recordLocalConsent() {
        viewModelScope.launch {
            sessionRepository.recordConsent("local", isPartner1 = true)

            _uiState.update {
                it.copy(
                    localConsented = true,
                    state = if (it.partnerConsented) {
                        SessionState.ACTIVE
                    } else {
                        SessionState.WAITING_PARTNER_CONSENT
                    },
                )
            }

            if (_uiState.value.state == SessionState.ACTIVE) {
                startSessionTimer()
            }
        }
    }

    /**
     * Record partner's consent (received via BLE/WiFi sync).
     */
    fun recordPartnerConsent() {
        viewModelScope.launch {
            sessionRepository.recordConsent("remote", isPartner1 = false)

            _uiState.update {
                it.copy(
                    partnerConsented = true,
                    state = if (it.localConsented) {
                        SessionState.ACTIVE
                    } else {
                        SessionState.CONSENT_GATE
                    },
                )
            }

            if (_uiState.value.state == SessionState.ACTIVE) {
                startSessionTimer()
            }
        }
    }

    // ── Safeword ────────────────────────────────────────────

    /**
     * Trigger safeword — INSTANTLY ends session from ANY state.
     * No confirmation dialog. No delay. Safety is non-negotiable.
     */
    fun triggerSafeword() {
        timerJob?.cancel()
        checkInJob?.cancel()
        denyDelayJob?.cancel()

        viewModelScope.launch {
            sessionRepository.triggerSafeword("local")

            _uiState.update {
                it.copy(
                    state = SessionState.COOL_DOWN,
                    safewordTriggered = true,
                    sessionDurationMinutes = sessionRepository.getSessionDurationMinutes(),
                )
            }
        }
    }

    // ── Check-In ────────────────────────────────────────────

    /**
     * Show check-in dialog (triggered at 50% of session time).
     */
    private fun requestCheckIn() {
        _uiState.update {
            it.copy(
                state = SessionState.CHECK_IN,
                checkInRequired = true,
            )
        }

        // Auto-end if no response within 2 minutes
        checkInJob = viewModelScope.launch {
            delay(120_000)
            if (_uiState.value.state == SessionState.CHECK_IN) {
                endSession()
            }
        }
    }

    /**
     * Both partners confirmed check-in — resume session.
     */
    fun confirmCheckIn() {
        checkInJob?.cancel()
        _uiState.update {
            it.copy(
                state = SessionState.ACTIVE,
                checkInRequired = false,
            )
        }
    }

    /**
     * Either partner declined check-in — end session normally.
     */
    fun declineCheckIn() {
        checkInJob?.cancel()
        endSession()
    }

    // ── Deny & Delay ────────────────────────────────────────

    /**
     * Trigger a deny & delay pause (anticipation mechanic).
     * Pauses everything for a configurable duration then auto-resumes.
     */
    fun triggerDenyDelay() {
        viewModelScope.launch {
            val delayDuration = preferences.denyDelayDuration.first()
            val delayMs = delayDuration * 1000L

            _uiState.update {
                it.copy(
                    state = SessionState.DENY_DELAY,
                    denyDelayRemainingMs = delayMs,
                )
            }

            // Countdown the delay
            denyDelayJob = viewModelScope.launch {
                var remaining = delayMs
                while (remaining > 0) {
                    delay(100)
                    remaining -= 100
                    _uiState.update {
                        it.copy(denyDelayRemainingMs = remaining)
                    }
                }

                // Auto-resume
                _uiState.update {
                    it.copy(
                        state = SessionState.ACTIVE,
                        denyDelayRemainingMs = 0,
                    )
                }
            }
        }
    }

    // ── Session End ─────────────────────────────────────────

    /**
     * End session normally (time expired or user chose to end).
     */
    fun endSession() {
        timerJob?.cancel()
        checkInJob?.cancel()
        denyDelayJob?.cancel()

        viewModelScope.launch {
            sessionRepository.endSession()

            _uiState.update {
                it.copy(
                    state = SessionState.COOL_DOWN,
                    safewordTriggered = false,
                    sessionDurationMinutes = sessionRepository.getSessionDurationMinutes(),
                )
            }
        }
    }

    /**
     * Return to home after cool-down.
     */
    fun returnToHome() {
        sessionRepository.clearCurrentSession()
        _uiState.update { SessionUiState() }
    }

    // ── Timer ───────────────────────────────────────────────

    private fun startSessionTimer() {
        checkInTriggered = false
        val totalMs = _uiState.value.timeLimitMinutes * 60_000L

        timerJob = viewModelScope.launch {
            var remaining = totalMs
            sessionRepository.updateTimeRemaining(remaining)

            while (remaining > 0) {
                delay(1000)
                remaining -= 1000

                _uiState.update {
                    it.copy(timeRemainingMs = remaining)
                }
                sessionRepository.updateTimeRemaining(remaining)

                // Check-in at 50% (only once)
                val progress = 1f - (remaining.toFloat() / totalMs)
                if (progress >= 0.5f && !checkInTriggered) {
                    checkInTriggered = true
                    requestCheckIn()
                    return@launch // Timer pauses during check-in
                }
            }

            // Time's up — end session
            endSession()
        }
    }
}
