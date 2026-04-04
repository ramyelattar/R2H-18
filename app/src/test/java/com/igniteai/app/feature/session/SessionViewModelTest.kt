package com.igniteai.app.feature.session

import com.igniteai.app.core.preferences.SessionSettings
import com.igniteai.app.data.dao.SessionDao
import com.igniteai.app.data.model.SessionRecord
import com.igniteai.app.data.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initiateSession_movesStateToConsentGate_andLoadsConfiguredTimeLimit() = runTest {
        val repository = SessionRepository(FakeSessionDao())
        val viewModel = SessionViewModel(
            sessionRepository = repository,
            preferences = FakeSessionSettings(
                sessionTimeLimit = flowOf(45),
                denyDelayDuration = flowOf(10),
            ),
        )

        viewModel.initiateSession()
        runCurrent()

        assertEquals(SessionViewModel.SessionState.CONSENT_GATE, viewModel.uiState.value.state)
        assertEquals(45, viewModel.uiState.value.timeLimitMinutes)
        assertEquals("FREE", viewModel.uiState.value.sessionType)
    }

    @Test
    fun recordLocalConsent_withoutPartnerConsent_movesStateToWaitingPartnerConsent() = runTest {
        val repository = SessionRepository(FakeSessionDao())
        val viewModel = SessionViewModel(
            sessionRepository = repository,
            preferences = FakeSessionSettings(
                sessionTimeLimit = flowOf(30),
                denyDelayDuration = flowOf(10),
            ),
        )

        viewModel.initiateSession()
        runCurrent()

        viewModel.recordLocalConsent()
        runCurrent()

        assertEquals(
            SessionViewModel.SessionState.WAITING_PARTNER_CONSENT,
            viewModel.uiState.value.state,
        )
        assertEquals(true, viewModel.uiState.value.localConsented)
        assertEquals(false, viewModel.uiState.value.partnerConsented)
    }

    @Test
    fun recordPartnerConsent_afterLocalConsent_movesStateToActive() = runTest {
        val repository = SessionRepository(FakeSessionDao())
        val viewModel = SessionViewModel(
            sessionRepository = repository,
            preferences = FakeSessionSettings(
                sessionTimeLimit = flowOf(30),
                denyDelayDuration = flowOf(10),
            ),
        )

        viewModel.initiateSession()
        runCurrent()
        viewModel.recordLocalConsent()
        runCurrent()

        viewModel.recordPartnerConsent()
        runCurrent()

        assertEquals(SessionViewModel.SessionState.ACTIVE, viewModel.uiState.value.state)
        assertEquals(true, viewModel.uiState.value.localConsented)
        assertEquals(true, viewModel.uiState.value.partnerConsented)
        assertEquals(30 * 60_000L, repository.timeRemainingMs.value)
    }

    @Test
    fun triggerSafeword_movesStateToCoolDown_andMarksSafewordTriggered() = runTest {
        val repository = SessionRepository(FakeSessionDao())
        val viewModel = SessionViewModel(
            sessionRepository = repository,
            preferences = FakeSessionSettings(
                sessionTimeLimit = flowOf(30),
                denyDelayDuration = flowOf(10),
            ),
        )

        viewModel.initiateSession()
        runCurrent()

        viewModel.triggerSafeword()
        runCurrent()

        assertEquals(SessionViewModel.SessionState.COOL_DOWN, viewModel.uiState.value.state)
        assertEquals(true, viewModel.uiState.value.safewordTriggered)
    }

    @Test
    fun endSession_movesStateToCoolDown_withoutSafeword() = runTest {
        val repository = SessionRepository(FakeSessionDao())
        val viewModel = SessionViewModel(
            sessionRepository = repository,
            preferences = FakeSessionSettings(
                sessionTimeLimit = flowOf(30),
                denyDelayDuration = flowOf(10),
            ),
        )

        viewModel.initiateSession()
        runCurrent()

        viewModel.endSession()
        runCurrent()

        assertEquals(SessionViewModel.SessionState.COOL_DOWN, viewModel.uiState.value.state)
        assertEquals(false, viewModel.uiState.value.safewordTriggered)
    }

    private class FakeSessionSettings(
        override val sessionTimeLimit: Flow<Int>,
        override val denyDelayDuration: Flow<Int>,
    ) : SessionSettings

    private class FakeSessionDao : SessionDao {
        private val sessions = mutableListOf<SessionRecord>()

        override suspend fun insert(session: SessionRecord) {
            sessions.removeAll { it.id == session.id }
            sessions += session
        }

        override suspend fun update(session: SessionRecord) {
            sessions.removeAll { it.id == session.id }
            sessions += session
        }

        override suspend fun getById(id: String): SessionRecord? = sessions.firstOrNull { it.id == id }

        override suspend fun getLatestSession(): SessionRecord? = sessions.maxByOrNull { it.startedAt }

        override suspend fun getAllSessions(): List<SessionRecord> = sessions.toList()

        override suspend fun getActiveSession(): SessionRecord? = sessions.firstOrNull { it.endedAt == null }

        override suspend fun getSafewordCount(): Int = sessions.count { it.safewordTriggered }

        override suspend fun deleteAll() {
            sessions.clear()
        }
    }

    class MainDispatcherRule(
        private val dispatcher: TestDispatcher = StandardTestDispatcher(),
    ) : TestWatcher() {
        override fun starting(description: Description) {
            Dispatchers.setMain(dispatcher)
        }

        override fun finished(description: Description) {
            Dispatchers.resetMain()
        }
    }
}
