package com.igniteai.app.core.session.reducer

import com.igniteai.app.core.session.effects.SessionEffect
import com.igniteai.app.core.session.model.DomainError
import com.igniteai.app.core.session.model.SessionState

data class TransitionResult(
    val newState: SessionState,
    val effects: List<SessionEffect> = emptyList(),
    val domainError: DomainError? = null,
)
