package com.igniteai.app

import android.app.Application

/**
 * IgniteAI Application class.
 *
 * Responsible for app-wide initialization:
 * - Encrypted database setup
 * - Content library loading on first launch
 * - Notification channel creation
 * - DI container initialization
 */
class IgniteAIApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Database, content loading, and DI will be initialized as modules are built
    }
}
