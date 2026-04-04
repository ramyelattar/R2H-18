package com.igniteai.app.core.session.store

import com.igniteai.app.core.session.effects.SessionEffect
import com.igniteai.app.core.session.model.SessionState

interface SessionEffectExecutor {
    suspend fun execute(effect: SessionEffect, newState: SessionState)
}

object NoopSessionEffectExecutor : SessionEffectExecutor {
    override suspend fun execute(effect: SessionEffect, newState: SessionState) = Unit
}
