package com.igniteai.app.core.session.model

/**
 * Domain events consumed by the pure reducer.
 */
sealed interface SessionEvent {
    data class StartSessionRequested(
        val sessionId: String,
        val sessionType: String,
        val requestedAtMs: Long,
        val timeLimitMs: Long,
    ) : SessionEvent

    data class ContentResolved(val contentId: String) : SessionEvent
    data class ContentResolutionFailed(val reason: String) : SessionEvent
    data object ConsentScreenEntered : SessionEvent
    data class ConsentGranted(val consentAtMs: Long) : SessionEvent
    data class ConsentDenied(val reason: String = "consent_denied") : SessionEvent
    data class SessionStartConfirmed(val startedAtMs: Long) : SessionEvent
    data object TimerStarted : SessionEvent
    data class TimerTick(val remainingMs: Long) : SessionEvent
    data class CheckInDue(val dueAtMs: Long) : SessionEvent
    data object CheckInContinue : SessionEvent
    data class CheckInDelay(val delayMs: Long, val requestedAtMs: Long) : SessionEvent
    data class CheckInDeny(val reason: String = "checkin_denied") : SessionEvent
    data object SessionEndRequested : SessionEvent
    data object SessionCompletedNaturally : SessionEvent
    data class SafewordTriggered(val occurredAtMs: Long, val source: String = "local") : SessionEvent
    data class EmergencyStopTriggered(val occurredAtMs: Long) : SessionEvent

    data class AppBackgrounded(val occurredAtMs: Long) : SessionEvent
    data object AppForegrounded : SessionEvent
    data class DeviceLocked(val occurredAtMs: Long) : SessionEvent
    data class UnlockSucceeded(val occurredAtMs: Long) : SessionEvent
    data class ProcessRestored(val occurredAtMs: Long) : SessionEvent
    data class ProcessDeathDetected(val occurredAtMs: Long) : SessionEvent
    data class InvalidPersistedStateDetected(val reason: String) : SessionEvent
    data class SessionStateCorrupted(val reason: String) : SessionEvent
    data class FatalErrorDetected(val reason: String) : SessionEvent

    data class DelayWindowStarted(val startedAtMs: Long) : SessionEvent
    data class DelayWindowExpired(val expiredAtMs: Long) : SessionEvent
    data class ResumeRequested(val requestedAtMs: Long) : SessionEvent
    data class ResumeRejected(val reason: String) : SessionEvent

    data object CooldownAcknowledged : SessionEvent
    data object PersistenceSucceeded : SessionEvent
    data class PersistenceFailed(val reason: String) : SessionEvent
}
