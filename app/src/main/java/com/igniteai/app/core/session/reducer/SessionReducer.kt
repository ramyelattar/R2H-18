package com.igniteai.app.core.session.reducer

import com.igniteai.app.core.session.effects.SessionEffect
import com.igniteai.app.core.session.model.ActiveSession
import com.igniteai.app.core.session.model.DomainError
import com.igniteai.app.core.session.model.DomainErrorCode
import com.igniteai.app.core.session.model.DraftSession
import com.igniteai.app.core.session.model.FailureReason
import com.igniteai.app.core.session.model.FailureSession
import com.igniteai.app.core.session.model.FinalizedSession
import com.igniteai.app.core.session.model.InterruptionReason
import com.igniteai.app.core.session.model.SessionEvent
import com.igniteai.app.core.session.model.SessionOutcome
import com.igniteai.app.core.session.model.SessionSnapshot
import com.igniteai.app.core.session.model.SessionState
import com.igniteai.app.core.session.model.StopReason
import com.igniteai.app.core.session.model.isFinal
import com.igniteai.app.core.session.model.isInterruptionSensitive

class SessionReducer(
    private val maxResumeAgeMs: Long = DEFAULT_MAX_RESUME_AGE_MS,
) {
    companion object {
        const val DEFAULT_MAX_RESUME_AGE_MS: Long = 5 * 60_000L
    }

    fun reduce(state: SessionState, event: SessionEvent): TransitionResult {
        if (event is SessionEvent.SafewordTriggered) return handleSafeword(state, event)

        return when (event) {
            is SessionEvent.SafewordTriggered -> handleSafeword(state, event)
            is SessionEvent.StartSessionRequested -> onStartSessionRequested(state, event)
            is SessionEvent.ContentResolved -> onContentResolved(state, event)
            is SessionEvent.ContentResolutionFailed -> onContentResolutionFailed(state, event)
            is SessionEvent.ConsentScreenEntered -> onConsentScreenEntered(state)
            is SessionEvent.ConsentGranted -> onConsentGranted(state, event)
            is SessionEvent.ConsentDenied -> onConsentDenied(state, event)
            is SessionEvent.SessionStartConfirmed -> onSessionStartConfirmed(state, event)
            is SessionEvent.TimerStarted -> if (state is SessionState.Active) TransitionResult(state) else invalid(state, event)
            is SessionEvent.TimerTick -> onTimerTick(state, event)
            is SessionEvent.CheckInDue -> onCheckInDue(state, event)
            is SessionEvent.CheckInContinue -> onCheckInContinue(state)
            is SessionEvent.CheckInDelay -> onCheckInDelay(state, event)
            is SessionEvent.CheckInDeny -> onCheckInDeny(state)
            is SessionEvent.SessionEndRequested -> onSessionEndRequested(state)
            is SessionEvent.SessionCompletedNaturally -> onSessionCompletedNaturally(state)
            is SessionEvent.EmergencyStopTriggered -> onEmergencyStop(state, event)
            is SessionEvent.AppBackgrounded -> moveToPaused(state, InterruptionReason.APP_BACKGROUNDED, event.occurredAtMs, event)
            is SessionEvent.DeviceLocked -> moveToPaused(state, InterruptionReason.DEVICE_LOCKED, event.occurredAtMs, event)
            is SessionEvent.AppForegrounded -> if (state is SessionState.PausedByInterruption) TransitionResult(state) else invalid(state, event)
            is SessionEvent.UnlockSucceeded -> onUnlockSucceeded(state, event)
            is SessionEvent.ProcessRestored -> onProcessRestored(state, event)
            is SessionEvent.ProcessDeathDetected -> onProcessDeath(state, event)
            is SessionEvent.InvalidPersistedStateDetected -> onInvalidPersistedState(state, event)
            is SessionEvent.SessionStateCorrupted -> onStateCorrupted(state, event)
            is SessionEvent.FatalErrorDetected -> onFatalError(state, event)
            is SessionEvent.DelayWindowStarted -> if (state is SessionState.Delayed) TransitionResult(state) else invalid(state, event)
            is SessionEvent.DelayWindowExpired -> onDelayWindowExpired(state, event)
            is SessionEvent.ResumeRequested -> onResumeRequested(state, event)
            is SessionEvent.ResumeRejected -> onResumeRejected(state, event)
            is SessionEvent.CooldownAcknowledged -> onCooldownAcknowledged(state)
            is SessionEvent.PersistenceSucceeded -> onPersistenceSucceeded(state)
            is SessionEvent.PersistenceFailed -> onPersistenceFailed(state, event)
        }
    }

    private fun onStartSessionRequested(
        state: SessionState,
        event: SessionEvent.StartSessionRequested,
    ): TransitionResult {
        if (state != SessionState.Idle) return invalid(state, event)
        val draft = DraftSession(
            sessionId = event.sessionId,
            sessionType = event.sessionType,
            requestedAtMs = event.requestedAtMs,
            timeLimitMs = event.timeLimitMs,
        )
        val next = SessionState.Preparing(draft)
        return TransitionResult(next, listOf(SessionEffect.PersistState(next.toSnapshot(event.requestedAtMs))))
    }

    private fun onContentResolved(
        state: SessionState,
        event: SessionEvent.ContentResolved,
    ): TransitionResult {
        if (state !is SessionState.Preparing) return invalid(state, event)
        val next = SessionState.AwaitingConsent(state.draft.copy(contentId = event.contentId))
        return TransitionResult(next, listOf(SessionEffect.PersistState(next.toSnapshot(now(state)))))
    }

    private fun onContentResolutionFailed(
        state: SessionState,
        event: SessionEvent.ContentResolutionFailed,
    ): TransitionResult {
        if (state !is SessionState.Preparing) return invalid(state, event)
        val next = SessionState.Failed(
            FailureSession(
                sessionId = state.draft.sessionId,
                fromState = "Preparing",
                reason = FailureReason.CONTENT_RESOLUTION_FAILED,
                occurredAtMs = state.draft.requestedAtMs,
            ),
        )
        return TransitionResult(
            next,
            listOf(
                SessionEffect.RedactedLog("content_resolution_failed"),
                SessionEffect.PersistState(next.toSnapshot(now(next))),
            ),
        )
    }

    private fun onConsentScreenEntered(state: SessionState): TransitionResult = when (state) {
        is SessionState.Preparing -> {
            if (state.draft.contentId == null) invalid(state, SessionEvent.ConsentScreenEntered)
            else {
                val next = SessionState.AwaitingConsent(state.draft)
                TransitionResult(next, listOf(SessionEffect.PersistState(next.toSnapshot(now(next)))))
            }
        }
        is SessionState.AwaitingConsent -> TransitionResult(state)
        else -> invalid(state, SessionEvent.ConsentScreenEntered)
    }

    private fun onConsentGranted(
        state: SessionState,
        event: SessionEvent.ConsentGranted,
    ): TransitionResult {
        if (state !is SessionState.AwaitingConsent) return invalid(state, event)
        val contentId = state.draft.contentId ?: return invalid(state, event)
        val next = SessionState.ReadyToStart(
            state.draft.copy(contentId = contentId, consentGrantedAtMs = event.consentAtMs),
        )
        return TransitionResult(next, listOf(SessionEffect.PersistState(next.toSnapshot(event.consentAtMs))))
    }

    private fun onConsentDenied(
        state: SessionState,
        event: SessionEvent.ConsentDenied,
    ): TransitionResult {
        if (state !is SessionState.AwaitingConsent) return invalid(state, event)
        val next = SessionState.Aborted(
            finalized = FinalizedSession(
                sessionId = state.draft.sessionId,
                sessionType = state.draft.sessionType,
                contentId = state.draft.contentId,
                startedAtMs = null,
                endedAtMs = state.draft.requestedAtMs,
                outcome = SessionOutcome.ABORTED,
                stopReason = StopReason.CONSENT_DENIED,
                consentGrantedAtMs = null,
                timeLimitMs = state.draft.timeLimitMs,
                remainingMs = state.draft.timeLimitMs,
            ),
        )
        return TransitionResult(
            next,
            listOf(
                SessionEffect.RedactedLog("consent_denied:${event.reason}"),
                SessionEffect.PersistState(next.toSnapshot(now(next))),
                SessionEffect.ShowCooldown,
                SessionEffect.FinalizeOutcome(SessionOutcome.ABORTED, StopReason.CONSENT_DENIED),
            ),
        )
    }

    private fun onSessionStartConfirmed(
        state: SessionState,
        event: SessionEvent.SessionStartConfirmed,
    ): TransitionResult {
        if (state !is SessionState.ReadyToStart) return invalid(state, event)
        val draft = state.draft
        val contentId = draft.contentId ?: return invalid(state, event)
        val consentAt = draft.consentGrantedAtMs ?: return invalid(state, event)
        val active = ActiveSession(
            sessionId = draft.sessionId,
            sessionType = draft.sessionType,
            contentId = contentId,
            startedAtMs = event.startedAtMs,
            consentGrantedAtMs = consentAt,
            timeLimitMs = draft.timeLimitMs,
            remainingMs = draft.timeLimitMs,
        )
        val next = SessionState.Active(active)
        return TransitionResult(
            next,
            listOf(
                SessionEffect.StartTimer(active.remainingMs),
                SessionEffect.PersistState(next.toSnapshot(event.startedAtMs)),
            ),
        )
    }

    private fun onTimerTick(state: SessionState, event: SessionEvent.TimerTick): TransitionResult {
        if (state !is SessionState.Active) return invalid(state, event)
        val remaining = event.remainingMs.coerceAtLeast(0L)
        if (remaining == 0L) {
            return onSessionCompletedNaturally(state)
        }
        val next = SessionState.Active(state.runtime.copy(remainingMs = remaining))
        return TransitionResult(next, listOf(SessionEffect.PersistState(next.toSnapshot(now(next)))))
    }

    private fun onCheckInDue(state: SessionState, event: SessionEvent.CheckInDue): TransitionResult {
        if (state !is SessionState.Active) return invalid(state, event)
        val next = SessionState.CheckInPending(state.runtime, dueAtMs = event.dueAtMs)
        return TransitionResult(
            next,
            listOf(
                SessionEffect.PauseProgression,
                SessionEffect.PersistState(next.toSnapshot(event.dueAtMs)),
            ),
        )
    }

    private fun onCheckInContinue(state: SessionState): TransitionResult {
        if (state !is SessionState.CheckInPending) return invalid(state, SessionEvent.CheckInContinue)
        val next = SessionState.Active(state.runtime.copy(checkInCount = state.runtime.checkInCount + 1))
        return TransitionResult(
            next,
            listOf(
                SessionEffect.ResumeProgression,
                SessionEffect.PersistState(next.toSnapshot(now(next))),
            ),
        )
    }

    private fun onCheckInDelay(state: SessionState, event: SessionEvent.CheckInDelay): TransitionResult {
        if (state !is SessionState.CheckInPending) return invalid(state, event)
        val next = SessionState.Delayed(state.runtime, resumeAtMs = event.requestedAtMs + event.delayMs)
        return TransitionResult(
            next,
            listOf(
                SessionEffect.StartDelayWindow(event.delayMs),
                SessionEffect.PersistState(next.toSnapshot(event.requestedAtMs)),
            ),
        )
    }

    private fun onCheckInDeny(state: SessionState): TransitionResult {
        if (state !is SessionState.CheckInPending) return invalid(state, SessionEvent.CheckInDeny())
        val finalized = state.runtime.toFinalized(
            endedAtMs = state.dueAtMs,
            outcome = SessionOutcome.ABORTED,
            reason = StopReason.CHECKIN_DENIED,
            remainingMs = state.runtime.remainingMs,
        )
        val next = SessionState.StoppingSafely(finalized)
        return TransitionResult(next, stopEffects(next, StopReason.CHECKIN_DENIED))
    }

    private fun onDelayWindowExpired(
        state: SessionState,
        event: SessionEvent.DelayWindowExpired,
    ): TransitionResult {
        if (state !is SessionState.Delayed) return invalid(state, event)
        val next = SessionState.CheckInPending(state.runtime, dueAtMs = event.expiredAtMs)
        return TransitionResult(next, listOf(SessionEffect.PersistState(next.toSnapshot(event.expiredAtMs))))
    }

    private fun onSessionEndRequested(state: SessionState): TransitionResult {
        if (state !is SessionState.Active) return invalid(state, SessionEvent.SessionEndRequested)
        val endedAt = state.runtime.startedAtMs + (state.runtime.timeLimitMs - state.runtime.remainingMs)
        val finalized = state.runtime.toFinalized(
            endedAtMs = endedAt,
            outcome = SessionOutcome.COMPLETED,
            reason = StopReason.NORMAL_END,
            remainingMs = state.runtime.remainingMs,
        )
        val next = SessionState.Cooldown(finalized)
        return TransitionResult(
            next,
            listOf(
                SessionEffect.StopTimer,
                SessionEffect.PersistState(next.toSnapshot(endedAt)),
                SessionEffect.ShowCooldown,
                SessionEffect.FinalizeOutcome(SessionOutcome.COMPLETED, StopReason.NORMAL_END),
            ),
        )
    }

    private fun onSessionCompletedNaturally(state: SessionState): TransitionResult {
        if (state !is SessionState.Active) return invalid(state, SessionEvent.SessionCompletedNaturally)
        val endedAt = state.runtime.startedAtMs + state.runtime.timeLimitMs
        val finalized = state.runtime.toFinalized(
            endedAtMs = endedAt,
            outcome = SessionOutcome.COMPLETED,
            reason = StopReason.NATURAL_COMPLETION,
            remainingMs = 0L,
        )
        val next = SessionState.Cooldown(finalized)
        return TransitionResult(
            next,
            listOf(
                SessionEffect.StopTimer,
                SessionEffect.PersistState(next.toSnapshot(endedAt)),
                SessionEffect.ShowCooldown,
                SessionEffect.FinalizeOutcome(SessionOutcome.COMPLETED, StopReason.NATURAL_COMPLETION),
            ),
        )
    }

    private fun onEmergencyStop(
        state: SessionState,
        event: SessionEvent.EmergencyStopTriggered,
    ): TransitionResult {
        val runtime = extractRuntime(state) ?: return invalid(state, event)
        val finalized = runtime.toFinalized(
            endedAtMs = event.occurredAtMs,
            outcome = SessionOutcome.ABORTED,
            reason = StopReason.EMERGENCY_STOP,
            remainingMs = runtime.remainingMs,
        )
        val next = SessionState.StoppingSafely(finalized)
        return TransitionResult(next, stopEffects(next, StopReason.EMERGENCY_STOP))
    }

    private fun handleSafeword(state: SessionState, event: SessionEvent.SafewordTriggered): TransitionResult = when (state) {
        is SessionState.Active,
        is SessionState.CheckInPending,
        is SessionState.Delayed,
        is SessionState.PausedByInterruption,
        -> {
            val runtime = extractRuntime(state) ?: return invalid(state, event)
            val finalized = runtime.toFinalized(
                endedAtMs = event.occurredAtMs,
                outcome = SessionOutcome.ABORTED,
                reason = StopReason.SAFEWORD,
                remainingMs = runtime.remainingMs,
            )
            val next = SessionState.StoppingSafely(finalized)
            TransitionResult(next, stopEffects(next, StopReason.SAFEWORD))
        }
        is SessionState.AwaitingConsent,
        is SessionState.ReadyToStart,
        -> {
            val draft = if (state is SessionState.AwaitingConsent) state.draft else (state as SessionState.ReadyToStart).draft
            val next = SessionState.Aborted(
                FinalizedSession(
                    sessionId = draft.sessionId,
                    sessionType = draft.sessionType,
                    contentId = draft.contentId,
                    startedAtMs = null,
                    endedAtMs = event.occurredAtMs,
                    outcome = SessionOutcome.ABORTED,
                    stopReason = StopReason.SAFEWORD,
                    consentGrantedAtMs = draft.consentGrantedAtMs,
                    timeLimitMs = draft.timeLimitMs,
                    remainingMs = draft.timeLimitMs,
                ),
            )
            TransitionResult(
                next,
                listOf(
                    SessionEffect.StopAudioHaptics,
                    SessionEffect.PersistState(next.toSnapshot(event.occurredAtMs)),
                    SessionEffect.ShowCooldown,
                    SessionEffect.FinalizeOutcome(SessionOutcome.ABORTED, StopReason.SAFEWORD),
                ),
            )
        }
        is SessionState.Cooldown,
        is SessionState.Completed,
        is SessionState.Aborted,
        -> TransitionResult(state)
        else -> invalid(state, event)
    }

    private fun moveToPaused(
        state: SessionState,
        reason: InterruptionReason,
        occurredAtMs: Long,
        originalEvent: SessionEvent,
    ): TransitionResult {
        val runtime = extractRuntime(state) ?: return invalid(state, originalEvent)
        val next = SessionState.PausedByInterruption(runtime, reason, occurredAtMs)
        return TransitionResult(
            next,
            listOf(
                SessionEffect.StopTimer,
                SessionEffect.LockSensitiveUi,
                SessionEffect.PersistState(next.toSnapshot(occurredAtMs)),
            ),
        )
    }

    private fun onUnlockSucceeded(
        state: SessionState,
        event: SessionEvent.UnlockSucceeded,
    ): TransitionResult {
        if (state !is SessionState.PausedByInterruption) return invalid(state, event)
        if (event.occurredAtMs - state.interruptedAtMs > maxResumeAgeMs) {
            return onResumeRejected(state, SessionEvent.ResumeRejected("resume_timeout"))
        }
        val next = SessionState.Active(state.runtime)
        return TransitionResult(
            next,
            listOf(
                SessionEffect.StartTimer(state.runtime.remainingMs),
                SessionEffect.PersistState(next.toSnapshot(event.occurredAtMs)),
            ),
        )
    }

    private fun onProcessRestored(
        state: SessionState,
        event: SessionEvent.ProcessRestored,
    ): TransitionResult = when (state) {
        is SessionState.Active,
        is SessionState.CheckInPending,
        is SessionState.Delayed,
        -> moveToPaused(state, InterruptionReason.PROCESS_RESTORED, event.occurredAtMs, event)
        is SessionState.PausedByInterruption -> TransitionResult(state)
        else -> invalid(state, event)
    }

    private fun onProcessDeath(
        state: SessionState,
        event: SessionEvent.ProcessDeathDetected,
    ): TransitionResult = when (state) {
        is SessionState.Active,
        is SessionState.CheckInPending,
        is SessionState.Delayed,
        is SessionState.PausedByInterruption,
        -> moveToPaused(state, InterruptionReason.PROCESS_DEATH, event.occurredAtMs, event)
        else -> invalid(state, event)
    }

    private fun onResumeRequested(
        state: SessionState,
        event: SessionEvent.ResumeRequested,
    ): TransitionResult = when (state) {
        is SessionState.Delayed -> {
            val next = SessionState.Active(state.runtime)
            TransitionResult(
                next,
                listOf(
                    SessionEffect.CancelDelayWindow,
                    SessionEffect.ResumeProgression,
                    SessionEffect.PersistState(next.toSnapshot(event.requestedAtMs)),
                ),
            )
        }
        is SessionState.PausedByInterruption -> onUnlockSucceeded(state, SessionEvent.UnlockSucceeded(event.requestedAtMs))
        else -> invalid(state, event)
    }

    private fun onResumeRejected(
        state: SessionState,
        event: SessionEvent.ResumeRejected,
    ): TransitionResult {
        if (state !is SessionState.PausedByInterruption) return invalid(state, event)
        val next = SessionState.Aborted(
            finalized = state.runtime.toFinalized(
                endedAtMs = state.interruptedAtMs,
                outcome = SessionOutcome.ABORTED,
                reason = StopReason.RESUME_REJECTED,
                remainingMs = state.runtime.remainingMs,
            ),
        )
        return TransitionResult(
            next,
            listOf(
                SessionEffect.RedactedLog("resume_rejected:${event.reason}"),
                SessionEffect.PersistState(next.toSnapshot(now(next))),
                SessionEffect.ShowCooldown,
                SessionEffect.FinalizeOutcome(SessionOutcome.ABORTED, StopReason.RESUME_REJECTED),
            ),
        )
    }

    private fun onInvalidPersistedState(
        state: SessionState,
        event: SessionEvent.InvalidPersistedStateDetected,
    ): TransitionResult {
        val runtime = extractRuntime(state) ?: return invalid(state, event)
        val next = SessionState.Aborted(
            finalized = runtime.toFinalized(
                endedAtMs = runtime.startedAtMs,
                outcome = SessionOutcome.ABORTED,
                reason = StopReason.INVALID_RESTORE,
                remainingMs = runtime.remainingMs,
            ),
        )
        return TransitionResult(
            next,
            listOf(
                SessionEffect.RedactedLog("invalid_persisted_state:${event.reason}"),
                SessionEffect.PersistState(next.toSnapshot(now(next))),
                SessionEffect.ShowCooldown,
                SessionEffect.FinalizeOutcome(SessionOutcome.ABORTED, StopReason.INVALID_RESTORE),
            ),
        )
    }

    private fun onStateCorrupted(
        state: SessionState,
        event: SessionEvent.SessionStateCorrupted,
    ): TransitionResult {
        if (state.isFinal()) return invalid(state, event)
        val next = abortFromState(state, StopReason.STATE_CORRUPTED, now(state))
        return TransitionResult(
            next,
            listOf(
                SessionEffect.StopTimer,
                SessionEffect.CancelDelayWindow,
                SessionEffect.CancelCheckIn,
                SessionEffect.RedactedLog("state_corrupted:${event.reason}"),
                SessionEffect.PersistState(next.toSnapshot(now(next))),
                SessionEffect.ShowCooldown,
                SessionEffect.FinalizeOutcome(SessionOutcome.ABORTED, StopReason.STATE_CORRUPTED),
            ),
        )
    }

    private fun onFatalError(
        state: SessionState,
        event: SessionEvent.FatalErrorDetected,
    ): TransitionResult {
        if (state.isFinal()) return invalid(state, event)
        val next = SessionState.Failed(
            FailureSession(
                sessionId = extractSessionId(state),
                fromState = state.javaClass.simpleName,
                reason = FailureReason.FATAL_ERROR,
                occurredAtMs = now(state),
            ),
        )
        return TransitionResult(
            next,
            listOf(
                SessionEffect.StopTimer,
                SessionEffect.CancelDelayWindow,
                SessionEffect.CancelCheckIn,
                SessionEffect.StopAudioHaptics,
                SessionEffect.RedactedLog("fatal_error:${event.reason}"),
                SessionEffect.PersistState(next.toSnapshot(now(next))),
            ),
        )
    }

    private fun onCooldownAcknowledged(state: SessionState): TransitionResult {
        if (state !is SessionState.Cooldown) return invalid(state, SessionEvent.CooldownAcknowledged)
        val next: SessionState = when (state.finalized.outcome) {
            SessionOutcome.COMPLETED -> SessionState.Completed(state.finalized)
            SessionOutcome.ABORTED -> SessionState.Aborted(state.finalized)
        }
        return TransitionResult(next, listOf(SessionEffect.PersistState(next.toSnapshot(now(next)))))
    }

    private fun onPersistenceSucceeded(state: SessionState): TransitionResult = when (state) {
        is SessionState.StoppingSafely -> {
            val next = SessionState.Cooldown(state.finalized)
            TransitionResult(
                next,
                listOf(
                    SessionEffect.PersistState(next.toSnapshot(now(next))),
                    SessionEffect.ShowCooldown,
                ),
            )
        }
        is SessionState.Cooldown -> onCooldownAcknowledged(state)
        is SessionState.Failed -> {
            val finalized = FinalizedSession(
                sessionId = state.failure.sessionId ?: "failed-${state.failure.occurredAtMs}",
                sessionType = "UNKNOWN",
                contentId = null,
                startedAtMs = null,
                endedAtMs = state.failure.occurredAtMs,
                outcome = SessionOutcome.ABORTED,
                stopReason = StopReason.FATAL_ERROR,
                consentGrantedAtMs = null,
                timeLimitMs = 0L,
                remainingMs = 0L,
            )
            val next = SessionState.Cooldown(finalized)
            TransitionResult(
                next,
                listOf(
                    SessionEffect.PersistState(next.toSnapshot(now(next))),
                    SessionEffect.ShowCooldown,
                ),
            )
        }
        else -> invalid(state, SessionEvent.PersistenceSucceeded)
    }

    private fun onPersistenceFailed(
        state: SessionState,
        event: SessionEvent.PersistenceFailed,
    ): TransitionResult {
        if (state.isFinal()) return invalid(state, event)
        val next = SessionState.Failed(
            FailureSession(
                sessionId = extractSessionId(state),
                fromState = state.javaClass.simpleName,
                reason = FailureReason.PERSISTENCE_FAILURE,
                occurredAtMs = now(state),
            ),
        )
        return TransitionResult(
            next,
            listOf(
                SessionEffect.RedactedLog("persistence_failed:${event.reason}"),
                SessionEffect.MarkDomainError(DomainErrorCode.PERSISTENCE_FAILED, "persistence failed"),
                SessionEffect.PersistState(next.toSnapshot(now(next))),
            ),
        )
    }

    private fun invalid(state: SessionState, event: SessionEvent): TransitionResult {
        val message = "invalid_transition:${state.javaClass.simpleName}:${event.javaClass.simpleName}"
        if (state.isInterruptionSensitive()) {
            val next = abortFromState(state, StopReason.INVALID_TRANSITION, now(state))
            return TransitionResult(
                next,
                listOf(
                    SessionEffect.StopTimer,
                    SessionEffect.CancelDelayWindow,
                    SessionEffect.CancelCheckIn,
                    SessionEffect.StopAudioHaptics,
                    SessionEffect.RedactedLog(message),
                    SessionEffect.MarkDomainError(DomainErrorCode.SAFETY_COLLAPSE, message),
                    SessionEffect.PersistState(next.toSnapshot(now(next))),
                    SessionEffect.ShowCooldown,
                    SessionEffect.FinalizeOutcome(SessionOutcome.ABORTED, StopReason.INVALID_TRANSITION),
                ),
                DomainError(DomainErrorCode.INVALID_TRANSITION, message, safetyCritical = true),
            )
        }
        return TransitionResult(
            state,
            listOf(
                SessionEffect.RedactedLog(message),
                SessionEffect.MarkDomainError(DomainErrorCode.INVALID_TRANSITION, message),
            ),
            DomainError(DomainErrorCode.INVALID_TRANSITION, message, safetyCritical = false),
        )
    }

    private fun stopEffects(
        state: SessionState.StoppingSafely,
        reason: StopReason,
    ): List<SessionEffect> = listOf(
        SessionEffect.StopTimer,
        SessionEffect.CancelDelayWindow,
        SessionEffect.CancelCheckIn,
        SessionEffect.StopAudioHaptics,
        SessionEffect.PersistState(state.toSnapshot(now(state))),
        SessionEffect.FinalizeOutcome(SessionOutcome.ABORTED, reason),
    )

    private fun extractRuntime(state: SessionState): ActiveSession? = when (state) {
        is SessionState.Active -> state.runtime
        is SessionState.CheckInPending -> state.runtime
        is SessionState.Delayed -> state.runtime
        is SessionState.PausedByInterruption -> state.runtime
        else -> null
    }

    private fun extractSessionId(state: SessionState): String? = when (state) {
        SessionState.Idle -> null
        is SessionState.Preparing -> state.draft.sessionId
        is SessionState.AwaitingConsent -> state.draft.sessionId
        is SessionState.ReadyToStart -> state.draft.sessionId
        is SessionState.Active -> state.runtime.sessionId
        is SessionState.PausedByInterruption -> state.runtime.sessionId
        is SessionState.CheckInPending -> state.runtime.sessionId
        is SessionState.Delayed -> state.runtime.sessionId
        is SessionState.StoppingSafely -> state.finalized.sessionId
        is SessionState.Cooldown -> state.finalized.sessionId
        is SessionState.Completed -> state.finalized.sessionId
        is SessionState.Aborted -> state.finalized.sessionId
        is SessionState.Failed -> state.failure.sessionId
    }

    private fun now(state: SessionState): Long = when (state) {
        SessionState.Idle -> 0L
        is SessionState.Preparing -> state.draft.requestedAtMs
        is SessionState.AwaitingConsent -> state.draft.consentGrantedAtMs ?: state.draft.requestedAtMs
        is SessionState.ReadyToStart -> state.draft.consentGrantedAtMs ?: state.draft.requestedAtMs
        is SessionState.Active -> state.runtime.startedAtMs
        is SessionState.PausedByInterruption -> state.interruptedAtMs
        is SessionState.CheckInPending -> state.dueAtMs
        is SessionState.Delayed -> state.resumeAtMs
        is SessionState.StoppingSafely -> state.finalized.endedAtMs
        is SessionState.Cooldown -> state.finalized.endedAtMs
        is SessionState.Completed -> state.finalized.endedAtMs
        is SessionState.Aborted -> state.finalized.endedAtMs
        is SessionState.Failed -> state.failure.occurredAtMs
    }

    private fun SessionState.toSnapshot(atMs: Long): SessionSnapshot = when (this) {
        SessionState.Idle -> SessionSnapshot("Idle", null, null, null, null, null, null, null, null, null, null, atMs)
        is SessionState.Preparing -> SessionSnapshot("Preparing", draft.sessionId, draft.sessionType, draft.contentId, null, draft.consentGrantedAtMs, draft.timeLimitMs, draft.timeLimitMs, null, null, null, atMs)
        is SessionState.AwaitingConsent -> SessionSnapshot("AwaitingConsent", draft.sessionId, draft.sessionType, draft.contentId, null, draft.consentGrantedAtMs, draft.timeLimitMs, draft.timeLimitMs, null, null, null, atMs)
        is SessionState.ReadyToStart -> SessionSnapshot("ReadyToStart", draft.sessionId, draft.sessionType, draft.contentId, null, draft.consentGrantedAtMs, draft.timeLimitMs, draft.timeLimitMs, null, null, null, atMs)
        is SessionState.Active -> SessionSnapshot("Active", runtime.sessionId, runtime.sessionType, runtime.contentId, runtime.startedAtMs, runtime.consentGrantedAtMs, runtime.timeLimitMs, runtime.remainingMs, null, null, null, atMs)
        is SessionState.PausedByInterruption -> SessionSnapshot("PausedByInterruption", runtime.sessionId, runtime.sessionType, runtime.contentId, runtime.startedAtMs, runtime.consentGrantedAtMs, runtime.timeLimitMs, runtime.remainingMs, interruptedAtMs, null, null, atMs)
        is SessionState.CheckInPending -> SessionSnapshot("CheckInPending", runtime.sessionId, runtime.sessionType, runtime.contentId, runtime.startedAtMs, runtime.consentGrantedAtMs, runtime.timeLimitMs, runtime.remainingMs, null, null, null, atMs)
        is SessionState.Delayed -> SessionSnapshot("Delayed", runtime.sessionId, runtime.sessionType, runtime.contentId, runtime.startedAtMs, runtime.consentGrantedAtMs, runtime.timeLimitMs, runtime.remainingMs, null, null, null, atMs)
        is SessionState.StoppingSafely -> finalized.toSnapshot("StoppingSafely", atMs)
        is SessionState.Cooldown -> finalized.toSnapshot("Cooldown", atMs)
        is SessionState.Completed -> finalized.toSnapshot("Completed", atMs)
        is SessionState.Aborted -> finalized.toSnapshot("Aborted", atMs)
        is SessionState.Failed -> SessionSnapshot("Failed", failure.sessionId, null, null, null, null, null, null, null, SessionOutcome.ABORTED, StopReason.FATAL_ERROR, atMs)
    }

    private fun FinalizedSession.toSnapshot(stateType: String, atMs: Long): SessionSnapshot =
        SessionSnapshot(stateType, sessionId, sessionType, contentId, startedAtMs, consentGrantedAtMs, timeLimitMs, remainingMs, null, outcome, stopReason, atMs)

    private fun ActiveSession.toFinalized(
        endedAtMs: Long,
        outcome: SessionOutcome,
        reason: StopReason,
        remainingMs: Long,
    ): FinalizedSession = FinalizedSession(
        sessionId = sessionId,
        sessionType = sessionType,
        contentId = contentId,
        startedAtMs = startedAtMs,
        endedAtMs = endedAtMs,
        outcome = outcome,
        stopReason = reason,
        consentGrantedAtMs = consentGrantedAtMs,
        timeLimitMs = timeLimitMs,
        remainingMs = remainingMs,
    )

    private fun abortFromState(state: SessionState, reason: StopReason, atMs: Long): SessionState.Aborted {
        val runtime = extractRuntime(state)
        return if (runtime != null) {
            SessionState.Aborted(runtime.toFinalized(atMs, SessionOutcome.ABORTED, reason, runtime.remainingMs))
        } else {
            SessionState.Aborted(
                FinalizedSession(
                    sessionId = extractSessionId(state) ?: "aborted-$atMs",
                    sessionType = "UNKNOWN",
                    contentId = null,
                    startedAtMs = null,
                    endedAtMs = atMs,
                    outcome = SessionOutcome.ABORTED,
                    stopReason = reason,
                    consentGrantedAtMs = null,
                    timeLimitMs = 0L,
                    remainingMs = 0L,
                ),
            )
        }
    }
}
