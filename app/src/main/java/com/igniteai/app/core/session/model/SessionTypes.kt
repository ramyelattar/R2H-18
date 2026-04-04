package com.igniteai.app.core.session.model

/**
 * Final outcome for a session lifecycle.
 */
enum class SessionOutcome {
    COMPLETED,
    ABORTED,
}

/**
 * Canonical stop reasons used for persistence and history.
 */
enum class StopReason {
    NORMAL_END,
    NATURAL_COMPLETION,
    SAFEWORD,
    EMERGENCY_STOP,
    CONSENT_DENIED,
    CHECKIN_DENIED,
    INVALID_TRANSITION,
    INVALID_RESTORE,
    STATE_CORRUPTED,
    FATAL_ERROR,
    CONTENT_RESOLUTION_FAILED,
    RESUME_REJECTED,
    UNKNOWN,
}

enum class InterruptionReason {
    APP_BACKGROUNDED,
    DEVICE_LOCKED,
    PROCESS_RESTORED,
    PROCESS_DEATH,
    SYSTEM_INTERRUPTION,
}

enum class FailureReason {
    CONTENT_RESOLUTION_FAILED,
    FATAL_ERROR,
    PERSISTENCE_FAILURE,
    UNKNOWN,
}

enum class DomainErrorCode {
    INVALID_TRANSITION,
    INVALID_RESTORE,
    STATE_CORRUPTED,
    PERSISTENCE_FAILED,
    SAFETY_COLLAPSE,
}

data class DomainError(
    val code: DomainErrorCode,
    val message: String,
    val safetyCritical: Boolean,
)

data class DraftSession(
    val sessionId: String,
    val sessionType: String,
    val requestedAtMs: Long,
    val timeLimitMs: Long,
    val contentId: String? = null,
    val consentGrantedAtMs: Long? = null,
)

data class ActiveSession(
    val sessionId: String,
    val sessionType: String,
    val contentId: String,
    val startedAtMs: Long,
    val consentGrantedAtMs: Long,
    val timeLimitMs: Long,
    val remainingMs: Long,
    val checkInCount: Int = 0,
)

data class FinalizedSession(
    val sessionId: String,
    val sessionType: String,
    val contentId: String?,
    val startedAtMs: Long?,
    val endedAtMs: Long,
    val outcome: SessionOutcome,
    val stopReason: StopReason,
    val consentGrantedAtMs: Long?,
    val timeLimitMs: Long,
    val remainingMs: Long,
)

data class FailureSession(
    val sessionId: String?,
    val fromState: String,
    val reason: FailureReason,
    val occurredAtMs: Long,
)

data class SessionSnapshot(
    val stateType: String,
    val sessionId: String?,
    val sessionType: String?,
    val contentId: String?,
    val startedAtMs: Long?,
    val consentGrantedAtMs: Long?,
    val timeLimitMs: Long?,
    val remainingMs: Long?,
    val interruptionAtMs: Long?,
    val outcome: SessionOutcome?,
    val stopReason: StopReason?,
    val updatedAtMs: Long,
)
