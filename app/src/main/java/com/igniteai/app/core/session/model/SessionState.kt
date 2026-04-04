package com.igniteai.app.core.session.model

/**
 * Closed-alpha session state machine states.
 */
sealed interface SessionState {
    data object Idle : SessionState
    data class Preparing(val draft: DraftSession) : SessionState
    data class AwaitingConsent(val draft: DraftSession) : SessionState
    data class ReadyToStart(val draft: DraftSession) : SessionState
    data class Active(val runtime: ActiveSession) : SessionState
    data class PausedByInterruption(
        val runtime: ActiveSession,
        val reason: InterruptionReason,
        val interruptedAtMs: Long,
    ) : SessionState
    data class CheckInPending(val runtime: ActiveSession, val dueAtMs: Long) : SessionState
    data class Delayed(val runtime: ActiveSession, val resumeAtMs: Long) : SessionState
    data class StoppingSafely(val finalized: FinalizedSession) : SessionState
    data class Cooldown(val finalized: FinalizedSession) : SessionState
    data class Completed(val finalized: FinalizedSession) : SessionState
    data class Aborted(val finalized: FinalizedSession) : SessionState
    data class Failed(val failure: FailureSession) : SessionState
}

fun SessionState.isFinal(): Boolean =
    this is SessionState.Completed || this is SessionState.Aborted

fun SessionState.isActiveCapable(): Boolean =
    this is SessionState.Active ||
        this is SessionState.CheckInPending ||
        this is SessionState.Delayed ||
        this is SessionState.PausedByInterruption

fun SessionState.isInterruptionSensitive(): Boolean =
    this is SessionState.Active ||
        this is SessionState.CheckInPending ||
        this is SessionState.Delayed ||
        this is SessionState.PausedByInterruption ||
        this is SessionState.StoppingSafely
