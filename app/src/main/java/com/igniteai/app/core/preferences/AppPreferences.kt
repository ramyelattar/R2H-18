package com.igniteai.app.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * App-wide preferences stored via Jetpack DataStore.
 *
 * DataStore is the modern replacement for SharedPreferences —
 * it's async (coroutine-based), type-safe, and doesn't block the UI thread.
 *
 * These preferences are NOT encrypted by default (DataStore doesn't support it natively).
 * Sensitive values like the PIN are encrypted via EncryptionManager before storage.
 */

// Extension to create DataStore singleton
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ignite_prefs")

class AppPreferences(private val context: Context) {

    // ── Keys ────────────────────────────────────────────────

    private object Keys {
        // Security
        val ENCRYPTED_PIN = stringPreferencesKey("encrypted_pin")
        val DECOY_ENABLED = booleanPreferencesKey("decoy_enabled")
        val DECOY_NAME = stringPreferencesKey("decoy_name") // "calc" or "notes"

        // Notifications
        val NOTIFICATION_HOUR = intPreferencesKey("notification_hour")   // 0-23
        val NOTIFICATION_MINUTE = intPreferencesKey("notification_minute") // 0-59
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")

        // Content preferences
        val TONE_PREFERENCE = stringPreferencesKey("tone_preference") // "PLAYFUL","RAW","SENSUAL","ADAPTIVE"
        val VOICE_GENDER = stringPreferencesKey("voice_gender")       // "MALE" or "FEMALE"

        // Streak
        val STREAK_COUNT = intPreferencesKey("streak_count")
        val LAST_ACTIVE_DATE = longPreferencesKey("last_active_date") // Unix day number

        // Pavlovian conditioning
        val PAVLOVIAN_SOUND_ENABLED = booleanPreferencesKey("pavlovian_sound_enabled")
        val PAVLOVIAN_HAPTIC_ENABLED = booleanPreferencesKey("pavlovian_haptic_enabled")
        val CONDITIONING_INTENSITY = stringPreferencesKey("conditioning_intensity") // "SUBTLE","MODERATE","INTENSE"

        // Session
        val SESSION_TIME_LIMIT = intPreferencesKey("session_time_limit") // minutes
        val DENY_DELAY_FREQUENCY = stringPreferencesKey("deny_delay_frequency") // "NEVER","RARE","MODERATE","FREQUENT"
        val DENY_DELAY_DURATION = intPreferencesKey("deny_delay_duration") // seconds

        // Voice safeword
        val VOICE_SAFEWORD_ENABLED = booleanPreferencesKey("voice_safeword_enabled")
        val SAFEWORD = stringPreferencesKey("safeword")

        // Content loading
        val CONTENT_LOADED_VERSION = intPreferencesKey("content_loaded_version")

        // Onboarding complete
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    }

    // ── Reads (Flow-based, reactive) ────────────────────────

    val isOnboardingComplete: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.ONBOARDING_COMPLETE] ?: false
    }

    val streakCount: Flow<Int> = context.dataStore.data.map {
        it[Keys.STREAK_COUNT] ?: 0
    }

    val tonePreference: Flow<String> = context.dataStore.data.map {
        it[Keys.TONE_PREFERENCE] ?: "ADAPTIVE"
    }

    val sessionTimeLimit: Flow<Int> = context.dataStore.data.map {
        it[Keys.SESSION_TIME_LIMIT] ?: 60 // Default: 60 minutes
    }

    val notificationHour: Flow<Int> = context.dataStore.data.map {
        it[Keys.NOTIFICATION_HOUR] ?: 20 // Default: 8 PM
    }

    val notificationMinute: Flow<Int> = context.dataStore.data.map {
        it[Keys.NOTIFICATION_MINUTE] ?: 0
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.NOTIFICATIONS_ENABLED] ?: true
    }

    val pavlovianSoundEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.PAVLOVIAN_SOUND_ENABLED] ?: true
    }

    val pavlovianHapticEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.PAVLOVIAN_HAPTIC_ENABLED] ?: true
    }

    val conditioningIntensity: Flow<String> = context.dataStore.data.map {
        it[Keys.CONDITIONING_INTENSITY] ?: "MODERATE"
    }

    val decoyEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.DECOY_ENABLED] ?: false
    }

    val voiceGender: Flow<String> = context.dataStore.data.map {
        it[Keys.VOICE_GENDER] ?: "FEMALE"
    }

    val safeword: Flow<String> = context.dataStore.data.map {
        it[Keys.SAFEWORD] ?: "red"
    }

    val voiceSafewordEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.VOICE_SAFEWORD_ENABLED] ?: false
    }

    val denyDelayDuration: Flow<Int> = context.dataStore.data.map {
        it[Keys.DENY_DELAY_DURATION] ?: 10 // Default: 10 seconds
    }

    val contentLoadedVersion: Flow<Int> = context.dataStore.data.map {
        it[Keys.CONTENT_LOADED_VERSION] ?: 0
    }

    // ── Writes (suspend functions) ──────────────────────────

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETE] = complete }
    }

    suspend fun setStreakCount(count: Int) {
        context.dataStore.edit { it[Keys.STREAK_COUNT] = count }
    }

    suspend fun setLastActiveDate(dayNumber: Long) {
        context.dataStore.edit { it[Keys.LAST_ACTIVE_DATE] = dayNumber }
    }

    suspend fun getLastActiveDate(): Long? {
        var result: Long? = null
        context.dataStore.edit { result = it[Keys.LAST_ACTIVE_DATE] }
        return result
    }

    suspend fun setTonePreference(tone: String) {
        context.dataStore.edit { it[Keys.TONE_PREFERENCE] = tone }
    }

    suspend fun setSessionTimeLimit(minutes: Int) {
        context.dataStore.edit { it[Keys.SESSION_TIME_LIMIT] = minutes }
    }

    suspend fun setNotificationTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[Keys.NOTIFICATION_HOUR] = hour
            it[Keys.NOTIFICATION_MINUTE] = minute
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setPavlovianSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PAVLOVIAN_SOUND_ENABLED] = enabled }
    }

    suspend fun setPavlovianHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PAVLOVIAN_HAPTIC_ENABLED] = enabled }
    }

    suspend fun setConditioningIntensity(intensity: String) {
        context.dataStore.edit { it[Keys.CONDITIONING_INTENSITY] = intensity }
    }

    suspend fun setDecoyEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DECOY_ENABLED] = enabled }
    }

    suspend fun setDecoyName(name: String) {
        context.dataStore.edit { it[Keys.DECOY_NAME] = name }
    }

    suspend fun setVoiceGender(gender: String) {
        context.dataStore.edit { it[Keys.VOICE_GENDER] = gender }
    }

    suspend fun setEncryptedPin(encryptedPin: String) {
        context.dataStore.edit { it[Keys.ENCRYPTED_PIN] = encryptedPin }
    }

    suspend fun getEncryptedPin(): String? {
        var result: String? = null
        context.dataStore.edit { result = it[Keys.ENCRYPTED_PIN] }
        return result
    }

    suspend fun setSafeword(word: String) {
        context.dataStore.edit { it[Keys.SAFEWORD] = word }
    }

    suspend fun setVoiceSafewordEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.VOICE_SAFEWORD_ENABLED] = enabled }
    }

    suspend fun setDenyDelayFrequency(frequency: String) {
        context.dataStore.edit { it[Keys.DENY_DELAY_FREQUENCY] = frequency }
    }

    suspend fun setDenyDelayDuration(seconds: Int) {
        context.dataStore.edit { it[Keys.DENY_DELAY_DURATION] = seconds }
    }

    suspend fun setContentLoadedVersion(version: Int) {
        context.dataStore.edit { it[Keys.CONTENT_LOADED_VERSION] = version }
    }
}
