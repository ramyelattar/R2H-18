package com.igniteai.app.core.session.store

import com.igniteai.app.core.session.model.DomainError
import com.igniteai.app.core.session.model.SessionEvent
import com.igniteai.app.core.session.model.SessionState
import com.igniteai.app.core.session.reducer.SessionReducer
import com.igniteai.app.core.session.reducer.TransitionResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SessionStateStore(
    initialState: SessionState = SessionState.Idle,
    private val reducer: SessionReducer = SessionReducer(),
    private val effectExecutor: SessionEffectExecutor = NoopSessionEffectExecutor,
) {
    private val mutex = Mutex()

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    private val _errors = MutableSharedFlow<DomainError>(extraBufferCapacity = 32)
    val errors: SharedFlow<DomainError> = _errors.asSharedFlow()

    suspend fun dispatch(event: SessionEvent): TransitionResult = mutex.withLock {
        val result = reducer.reduce(_state.value, event)
        _state.value = result.newState
        result.domainError?.let { _errors.tryEmit(it) }
        result.effects.forEach { effectExecutor.execute(it, result.newState) }
        result
    }

    suspend fun reset(newState: SessionState = SessionState.Idle) = mutex.withLock {
        _state.value = newState
    }
}
