package com.igniteai.app.core.session.reducer

import com.igniteai.app.core.session.effects.SessionEffect
import com.igniteai.app.core.session.model.ActiveSession
import com.igniteai.app.core.session.model.DomainErrorCode
import com.igniteai.app.core.session.model.FinalizedSession
import com.igniteai.app.core.session.model.InterruptionReason
import com.igniteai.app.core.session.model.SessionEvent
import com.igniteai.app.core.session.model.SessionOutcome
import com.igniteai.app.core.session.model.SessionState
import com.igniteai.app.core.session.model.StopReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionReducerTest {

    private val reducer = SessionReducer(maxResumeAgeMs = 1_000L)

    @Test
    fun `start session from idle moves to preparing`() {
        val result = reducer.reduce(
            SessionState.Idle,
            SessionEvent.StartSessionRequested(
                sessionId = "s1",
                sessionType = "FREE",
                requestedAtMs = 100L,
                timeLimitMs = 60_000L,
            ),
        )

        assertTrue(result.newState is SessionState.Preparing)
    }

    @Test
    fun `consent flow to active emits timer start`() {
        val preparing = reducer.reduce(
            SessionState.Idle,
            SessionEvent.StartSessionRequested("s1", "FREE", 100L, 60_000L),
        ).newState
        val awaiting = reducer.reduce(preparing, SessionEvent.ContentResolved("c1")).newState
        val ready = reducer.reduce(awaiting, SessionEvent.ConsentGranted(200L)).newState
        val activeResult = reducer.reduce(ready, SessionEvent.SessionStartConfirmed(300L))

        assertTrue(activeResult.newState is SessionState.Active)
        assertTrue(activeResult.effects.any { it is SessionEffect.StartTimer })
    }

    @Test
    fun `safeword dominates active states`() {
        val active = active()
        val checkIn = SessionState.CheckInPending(active.runtime, dueAtMs = 1_000L)
        val delayed = SessionState.Delayed(active.runtime, resumeAtMs = 2_000L)
        val paused = SessionState.PausedByInterruption(
            runtime = active.runtime,
            reason = InterruptionReason.APP_BACKGROUNDED,
            interruptedAtMs = 1_500L,
        )

        listOf<SessionState>(active, checkIn, delayed, paused).forEach { state ->
            val result = reducer.reduce(state, SessionEvent.SafewordTriggered(2_000L))
            assertTrue(result.newState is SessionState.StoppingSafely)
            assertTrue(result.effects.any { it is SessionEffect.StopTimer })
            assertTrue(result.effects.any { it is SessionEffect.StopAudioHaptics })
        }
    }

    @Test
    fun `invalid transition in active-sensitive state forces abort`() {
        val active = active()
        val result = reducer.reduce(active, SessionEvent.CheckInContinue)

        assertTrue(result.newState is SessionState.Aborted)
        assertNotNull(result.domainError)
        assertEquals(DomainErrorCode.INVALID_TRANSITION, result.domainError?.code)
        assertTrue(result.domainError?.safetyCritical == true)
    }

    @Test
    fun `invalid transition in pre-start state is rejected without abort`() {
        val awaiting = SessionState.AwaitingConsent(
            draft = com.igniteai.app.core.session.model.DraftSession(
                sessionId = "s1",
                sessionType = "FREE",
                requestedAtMs = 10L,
                timeLimitMs = 60_000L,
                contentId = "c1",
            ),
        )
        val result = reducer.reduce(awaiting, SessionEvent.TimerTick(59_000L))

        assertTrue(result.newState is SessionState.AwaitingConsent)
        assertNotNull(result.domainError)
        assertTrue(result.domainError?.safetyCritical == false)
    }

    @Test
    fun `process restore from active moves to paused`() {
        val result = reducer.reduce(active(), SessionEvent.ProcessRestored(2_000L))
        assertTrue(result.newState is SessionState.PausedByInterruption)
    }

    @Test
    fun `unlock beyond resume window aborts`() {
        val paused = SessionState.PausedByInterruption(
            runtime = active().runtime,
            reason = InterruptionReason.APP_BACKGROUNDED,
            interruptedAtMs = 100L,
        )
        val result = reducer.reduce(paused, SessionEvent.UnlockSucceeded(5_000L))

        assertTrue(result.newState is SessionState.Aborted)
    }

    @Test
    fun `cooldown acknowledges into terminal state`() {
        val cooldownCompleted = SessionState.Cooldown(
            finalized = finalized(SessionOutcome.COMPLETED, StopReason.NORMAL_END),
        )
        val cooldownAborted = SessionState.Cooldown(
            finalized = finalized(SessionOutcome.ABORTED, StopReason.SAFEWORD),
        )

        val completedResult = reducer.reduce(cooldownCompleted, SessionEvent.CooldownAcknowledged)
        val abortedResult = reducer.reduce(cooldownAborted, SessionEvent.CooldownAcknowledged)

        assertTrue(completedResult.newState is SessionState.Completed)
        assertTrue(abortedResult.newState is SessionState.Aborted)
    }

    @Test
    fun `final states stay terminal for invalid resume request`() {
        val completed = SessionState.Completed(finalized(SessionOutcome.COMPLETED, StopReason.NORMAL_END))
        val result = reducer.reduce(completed, SessionEvent.ResumeRequested(999L))

        assertTrue(result.newState is SessionState.Completed)
        assertNotNull(result.domainError)
    }

    private fun active(): SessionState.Active = SessionState.Active(
        runtime = ActiveSession(
            sessionId = "s1",
            sessionType = "FREE",
            contentId = "c1",
            startedAtMs = 1_000L,
            consentGrantedAtMs = 900L,
            timeLimitMs = 60_000L,
            remainingMs = 30_000L,
        ),
    )

    private fun finalized(outcome: SessionOutcome, reason: StopReason): FinalizedSession = FinalizedSession(
        sessionId = "s1",
        sessionType = "FREE",
        contentId = "c1",
        startedAtMs = 1_000L,
        endedAtMs = 2_000L,
        outcome = outcome,
        stopReason = reason,
        consentGrantedAtMs = 900L,
        timeLimitMs = 60_000L,
        remainingMs = 0L,
    )
}
