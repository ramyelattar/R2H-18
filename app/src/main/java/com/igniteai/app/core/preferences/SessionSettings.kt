package com.igniteai.app.core.preferences

import kotlinx.coroutines.flow.Flow

interface SessionSettings {
    val sessionTimeLimit: Flow<Int>
    val denyDelayDuration: Flow<Int>
}
