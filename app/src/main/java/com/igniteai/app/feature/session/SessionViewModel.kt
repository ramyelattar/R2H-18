package com.igniteai.app.feature.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igniteai.app.core.preferences.SessionSettings
import com.igniteai.app.core.session.effects.SessionEffect
import com.igniteai.app.core.session.model.SessionEvent
import com.igniteai.app.core.session.model.SessionState as CoreSessionState
import com.igniteai.app.core.session.model.StopReason
import com.igniteai.app.core.session.reducer.SessionReducer
import com.igniteai.app.core.session.store.SessionEffectExecutor
import com.igniteai.app.core.session.store.SessionStateStore
import com.igniteai.app.data.repository.SessionRepository
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI-facing session coordinator.
 *
 * Public API stays stable for existing UI/navigation while session transitions
 * are delegated to the core reducer/store.
 */
class SessionViewModel(
    private val sessionRepository: SessionRepository,
    private val preferences: SessionSettings,
) : ViewModel() {

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
    private var delayJob: Job? = null

    private var localConsented: Boolean = false
    private var partnerConsented: Boolean = false
    private var checkInTriggered: Boolean = false
    private var delayRemainingMs: Long = 0L

    private val sessionStore = SessionStateStore(
        initialState = CoreSessionState.Idle,
        reducer = SessionReducer(),
        effectExecutor = object : SessionEffectExecutor {
            override suspend fun execute(effect: SessionEffect, newState: CoreSessionState) {
                when (effect) {
                    is SessionEffect.StartTimer -> startSessionTimer(effect.remainingMs)
                    SessionEffect.StopTimer -> timerJob?.cancel()
                    SessionEffect.PauseProgression -> timerJob?.cancel()
                    SessionEffect.ResumeProgression -> {
                        remainingFromCoreState(newState)?.let { startSessionTimer(it) }
                    }
                    is SessionEffect.StartDelayWindow -> startDelayWindow(effect.delayMs)
                    SessionEffect.CancelDelayWindow -> {
                        delayJob?.cancel()
                        delayRemainingMs = 0L
                    }
                    is SessionEffect.PersistState -> {
                        effect.snapshot.remainingMs?.let { sessionRepository.updateTimeRemaining(it) }
                    }
                    is SessionEffect.FinalizeOutcome -> {
                        when (effect.reason) {
                            StopReason.SAFEWORD,
                            StopReason.EMERGENCY_STOP,
                            -> sessionRepository.triggerSafeword("local")

                            else -> sessionRepository.endSession()
                        }
                        _uiState.update {
                            it.copy(
                                safewordTriggered = effect.reason == StopReason.SAFEWORD || effect.reason == StopReason.EMERGENCY_STOP,
                                sessionDurationMinutes = sessionRepository.getSessionDurationMinutes(),
                            )
                        }
                    }
                    is SessionEffect.MarkDomainError -> {
                        _uiState.update { current -> current.copy(error = effect.message) }
                    }
                    SessionEffect.StopAudioHaptics,
                    SessionEffect.ShowCooldown,
                    SessionEffect.LockSensitiveUi,
                    SessionEffect.CancelCheckIn,
                    is SessionEffect.RedactedLog,
                    -> Unit
                }
            }
        },
    )

    init {
        viewModelScope.launch {
            sessionStore.state.collectLatest { core ->
                syncUiFromCoreState(core)
            }
        }
    }

    fun initiateSession(sessionType: String = "FREE") {
        viewModelScope.launch {
            val timeLimitMinutes = preferences.sessionTimeLimit.first()
            val sessionId = sessionRepository.startSession(sessionType, timeLimitMinutes)

            localConsented = false
            partnerConsented = false
            checkInTriggered = false
            delayRemainingMs = 0L

            val now = System.currentTimeMillis()
            dispatch(SessionEvent.StartSessionRequested(
                sessionId = sessionId,
                sessionType = sessionType,
                requestedAtMs = now,
                timeLimitMs = timeLimitMinutes * 60_000L,
            ))
            dispatch(SessionEvent.ContentResolved(contentId = "local-content-default"))
            dispatch(SessionEvent.ConsentScreenEntered)
        }
    }

    fun recordLocalConsent() {
        viewModelScope.launch {
            sessionRepository.recordConsent("local", isPartner1 = true)
            localConsented = true

            if (partnerConsented) {
                val now = System.currentTimeMillis()
                dispatch(SessionEvent.ConsentGranted(consentAtMs = now))
                dispatch(SessionEvent.SessionStartConfirmed(startedAtMs = now))
            } else {
                syncUiFromCoreState(sessionStore.state.value)
            }
        }
    }

    fun recordPartnerConsent() {
        viewModelScope.launch {
            sessionRepository.recordConsent("remote", isPartner1 = false)
            partnerConsented = true

            if (localConsented) {
                val now = System.currentTimeMillis()
                dispatch(SessionEvent.ConsentGranted(consentAtMs = now))
                dispatch(SessionEvent.SessionStartConfirmed(startedAtMs = now))
            } else {
                syncUiFromCoreState(sessionStore.state.value)
            }
        }
    }

    fun triggerSafeword() {
        viewModelScope.launch {
            dispatch(SessionEvent.SafewordTriggered(occurredAtMs = System.currentTimeMillis()))
        }
    }

    fun confirmCheckIn() {
        viewModelScope.launch {
            dispatch(SessionEvent.CheckInContinue)
        }
    }

    fun declineCheckIn() {
        viewModelScope.launch {
            dispatch(SessionEvent.CheckInDeny())
        }
    }

    fun triggerDenyDelay() {
        viewModelScope.launch {
            val delayMs = preferences.denyDelayDuration.first() * 1000L
            val now = System.currentTimeMillis()

            if (sessionStore.state.value is CoreSessionState.Active) {
                dispatch(SessionEvent.CheckInDue(dueAtMs = now))
            }
            dispatch(SessionEvent.CheckInDelay(delayMs = delayMs, requestedAtMs = now))
        }
    }

    fun endSession() {
        viewModelScope.launch {
            when (sessionStore.state.value) {
                is CoreSessionState.Active -> dispatch(SessionEvent.SessionEndRequested)
                is CoreSessionState.CheckInPending -> dispatch(SessionEvent.CheckInDeny("user_end_requested"))
                is CoreSessionState.Delayed -> dispatch(SessionEvent.ResumeRejected("user_end_requested"))
                is CoreSessionState.Preparing,
                is CoreSessionState.AwaitingConsent,
                is CoreSessionState.ReadyToStart,
                is CoreSessionState.PausedByInterruption,
                -> dispatch(SessionEvent.ConsentDenied("user_end_requested"))

                else -> Unit
            }
        }
    }

    fun returnToHome() {
        viewModelScope.launch {
            timerJob?.cancel()
            delayJob?.cancel()
            sessionRepository.clearCurrentSession()

            localConsented = false
            partnerConsented = false
            checkInTriggered = false
            delayRemainingMs = 0L

            sessionStore.reset()
            _uiState.value = SessionUiState()
        }
    }

    fun onAppBackgrounded() {
        viewModelScope.launch {
            val state = sessionStore.state.value
            if (state is CoreSessionState.Active || state is CoreSessionState.CheckInPending || state is CoreSessionState.Delayed) {
                dispatch(SessionEvent.AppBackgrounded(occurredAtMs = System.currentTimeMillis()))
            }
        }
    }

    fun onAppForegrounded() {
        viewModelScope.launch {
            if (sessionStore.state.value is CoreSessionState.PausedByInterruption) {
                dispatch(SessionEvent.AppForegrounded)
            }
        }
    }

    fun onDeviceLocked() {
        viewModelScope.launch {
            val state = sessionStore.state.value
            if (state is CoreSessionState.Active || state is CoreSessionState.CheckInPending || state is CoreSessionState.Delayed) {
                dispatch(SessionEvent.DeviceLocked(occurredAtMs = System.currentTimeMillis()))
            }
        }
    }

    fun onUnlockSucceededAfterInterruption() {
        viewModelScope.launch {
            if (sessionStore.state.value is CoreSessionState.PausedByInterruption) {
                dispatch(SessionEvent.UnlockSucceeded(occurredAtMs = System.currentTimeMillis()))
            }
        }
    }

    fun onProcessRestored() {
        viewModelScope.launch {
            when (sessionStore.state.value) {
                is CoreSessionState.Active,
                is CoreSessionState.CheckInPending,
                is CoreSessionState.Delayed,
                is CoreSessionState.PausedByInterruption,
                -> dispatch(SessionEvent.ProcessRestored(occurredAtMs = System.currentTimeMillis()))

                else -> Unit
            }
        }
    }

    private suspend fun dispatch(event: SessionEvent) {
        sessionStore.dispatch(event)
        normalizeStoreState()
    }

    private suspend fun normalizeStoreState() {
        while (true) {
            when (sessionStore.state.value) {
                is CoreSessionState.StoppingSafely,
                is CoreSessionState.Failed,
                -> sessionStore.dispatch(SessionEvent.PersistenceSucceeded)

                else -> return
            }
        }
    }

    private fun startSessionTimer(initialRemainingMs: Long) {
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            var remaining = initialRemainingMs

            while (remaining > 0 && sessionStore.state.value is CoreSessionState.Active) {
                delay(1000)
                remaining = (remaining - 1000).coerceAtLeast(0L)

                dispatch(SessionEvent.TimerTick(remainingMs = remaining))

                val core = sessionStore.state.value
                if (core is CoreSessionState.Active) {
                    val progress = 1f - (core.runtime.remainingMs.toFloat() / core.runtime.timeLimitMs.toFloat())
                    if (progress >= 0.5f && !checkInTriggered) {
                        checkInTriggered = true
                        dispatch(SessionEvent.CheckInDue(dueAtMs = System.currentTimeMillis()))
                    }
                }
            }
        }
    }

    private fun startDelayWindow(delayMs: Long) {
        delayJob?.cancel()
        delayRemainingMs = delayMs

        delayJob = viewModelScope.launch {
            var remaining = delayMs
            while (remaining > 0 && sessionStore.state.value is CoreSessionState.Delayed) {
                delay(100)
                remaining = (remaining - 100).coerceAtLeast(0L)
                delayRemainingMs = remaining
                syncUiFromCoreState(sessionStore.state.value)
            }

            if (sessionStore.state.value is CoreSessionState.Delayed) {
                dispatch(SessionEvent.DelayWindowExpired(expiredAtMs = System.currentTimeMillis()))
            }
        }
    }

    private fun syncUiFromCoreState(core: CoreSessionState) {
        val mappedState = when (core) {
            CoreSessionState.Idle -> SessionState.IDLE
            is CoreSessionState.Preparing,
            is CoreSessionState.AwaitingConsent,
            is CoreSessionState.ReadyToStart,
            -> if (localConsented && !partnerConsented) {
                SessionState.WAITING_PARTNER_CONSENT
            } else {
                SessionState.CONSENT_GATE
            }

            is CoreSessionState.Active -> SessionState.ACTIVE
            is CoreSessionState.CheckInPending -> SessionState.CHECK_IN
            is CoreSessionState.Delayed -> SessionState.DENY_DELAY
            is CoreSessionState.PausedByInterruption -> SessionState.ACTIVE
            is CoreSessionState.StoppingSafely,
            is CoreSessionState.Cooldown,
            is CoreSessionState.Completed,
            is CoreSessionState.Aborted,
            is CoreSessionState.Failed,
            -> SessionState.COOL_DOWN
        }

        val timeLimitMinutes = when (core) {
            is CoreSessionState.Preparing -> (core.draft.timeLimitMs / 60_000L).toInt()
            is CoreSessionState.AwaitingConsent -> (core.draft.timeLimitMs / 60_000L).toInt()
            is CoreSessionState.ReadyToStart -> (core.draft.timeLimitMs / 60_000L).toInt()
            is CoreSessionState.Active -> (core.runtime.timeLimitMs / 60_000L).toInt()
            is CoreSessionState.CheckInPending -> (core.runtime.timeLimitMs / 60_000L).toInt()
            is CoreSessionState.Delayed -> (core.runtime.timeLimitMs / 60_000L).toInt()
            is CoreSessionState.PausedByInterruption -> (core.runtime.timeLimitMs / 60_000L).toInt()
            is CoreSessionState.StoppingSafely -> (core.finalized.timeLimitMs / 60_000L).toInt()
            is CoreSessionState.Cooldown -> (core.finalized.timeLimitMs / 60_000L).toInt()
            is CoreSessionState.Completed -> (core.finalized.timeLimitMs / 60_000L).toInt()
            is CoreSessionState.Aborted -> (core.finalized.timeLimitMs / 60_000L).toInt()
            is CoreSessionState.Failed -> _uiState.value.timeLimitMinutes
            CoreSessionState.Idle -> _uiState.value.timeLimitMinutes
        }

        _uiState.update { current ->
            current.copy(
                state = mappedState,
                localConsented = localConsented,
                partnerConsented = partnerConsented,
                timeRemainingMs = remainingFromCoreState(core) ?: current.timeRemainingMs,
                timeLimitMinutes = timeLimitMinutes,
                sessionType = sessionTypeFromCoreState(core) ?: current.sessionType,
                checkInRequired = core is CoreSessionState.CheckInPending,
                denyDelayRemainingMs = if (core is CoreSessionState.Delayed) delayRemainingMs else 0L,
                error = if (core is CoreSessionState.Failed) "Session failed and was safely terminated." else current.error,
            )
        }
    }

    private fun remainingFromCoreState(core: CoreSessionState): Long? = when (core) {
        is CoreSessionState.Active -> core.runtime.remainingMs
        is CoreSessionState.CheckInPending -> core.runtime.remainingMs
        is CoreSessionState.Delayed -> core.runtime.remainingMs
        is CoreSessionState.PausedByInterruption -> core.runtime.remainingMs
        is CoreSessionState.Cooldown -> core.finalized.remainingMs
        is CoreSessionState.Completed -> core.finalized.remainingMs
        is CoreSessionState.Aborted -> core.finalized.remainingMs
        is CoreSessionState.StoppingSafely -> core.finalized.remainingMs
        else -> null
    }

    private fun sessionTypeFromCoreState(core: CoreSessionState): String? = when (core) {
        is CoreSessionState.Preparing -> core.draft.sessionType
        is CoreSessionState.AwaitingConsent -> core.draft.sessionType
        is CoreSessionState.ReadyToStart -> core.draft.sessionType
        is CoreSessionState.Active -> core.runtime.sessionType
        is CoreSessionState.CheckInPending -> core.runtime.sessionType
        is CoreSessionState.Delayed -> core.runtime.sessionType
        is CoreSessionState.PausedByInterruption -> core.runtime.sessionType
        is CoreSessionState.Cooldown -> core.finalized.sessionType
        is CoreSessionState.Completed -> core.finalized.sessionType
        is CoreSessionState.Aborted -> core.finalized.sessionType
        is CoreSessionState.StoppingSafely -> core.finalized.sessionType
        else -> null
    }
}
