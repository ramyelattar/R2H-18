package com.igniteai.app.core.session.effects

import com.igniteai.app.core.session.model.DomainErrorCode
import com.igniteai.app.core.session.model.SessionOutcome
import com.igniteai.app.core.session.model.SessionSnapshot
import com.igniteai.app.core.session.model.StopReason

/**
 * Explicit side effects emitted by the reducer.
 * The reducer stays pure: it only describes what should happen.
 */
sealed interface SessionEffect {
    data class PersistState(val snapshot: SessionSnapshot) : SessionEffect
    data class StartTimer(val remainingMs: Long) : SessionEffect
    data object StopTimer : SessionEffect
    data object PauseProgression : SessionEffect
    data object ResumeProgression : SessionEffect
    data object CancelCheckIn : SessionEffect
    data class StartDelayWindow(val delayMs: Long) : SessionEffect
    data object CancelDelayWindow : SessionEffect
    data object StopAudioHaptics : SessionEffect
    data object ShowCooldown : SessionEffect
    data object LockSensitiveUi : SessionEffect
    data class RedactedLog(val message: String) : SessionEffect
    data class MarkDomainError(val code: DomainErrorCode, val message: String) : SessionEffect
    data class FinalizeOutcome(val outcome: SessionOutcome, val reason: StopReason) : SessionEffect
}
