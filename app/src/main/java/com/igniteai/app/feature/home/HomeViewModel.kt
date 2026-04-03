package com.igniteai.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igniteai.app.core.preferences.AppPreferences
import com.igniteai.app.data.model.ContentItem
import com.igniteai.app.data.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * ViewModel for the Home screen — the app's main hub.
 *
 * Manages:
 * - Daily dare: one fresh dare per day, cached until tomorrow
 * - Streak tracking: consecutive days of engagement
 * - Connection status: whether partner's device is reachable
 *
 * Streak logic:
 *   - Each day the user opens the app and engages with a dare, the streak increments
 *   - If a day is missed, the streak resets to 0
 *   - Streak bonus (capped at 5) increases dare intensity range
 */
class HomeViewModel(
    private val contentRepository: ContentRepository,
    private val preferences: AppPreferences,
) : ViewModel() {

    data class HomeUiState(
        val dailyDare: ContentItem? = null,
        val streakCount: Int = 0,
        val partnerName: String = "",
        val isFireUnlocked: Boolean = false,
        val isConnected: Boolean = false,
        val isLoading: Boolean = true,
        val dareCompleted: Boolean = false,
    )

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadHome()
    }

    private fun loadHome() {
        viewModelScope.launch {
            val streak = preferences.streakCount.first()

            // Update streak based on last active date
            val todayDayNumber = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
            val lastActive = preferences.getLastActiveDate()
            val updatedStreak = when {
                lastActive == null -> 0 // First time
                todayDayNumber == lastActive -> streak // Already counted today
                todayDayNumber - lastActive == 1L -> streak // Consecutive (will increment on dare completion)
                else -> 0 // Missed a day — reset
            }

            if (updatedStreak != streak) {
                preferences.setStreakCount(updatedStreak)
            }

            // Load daily dare
            val dare = contentRepository.getRandomDare(
                hasFire = false, // TODO: check license
                minIntensity = 1,
                maxIntensity = 5 + minOf(updatedStreak, 5),
            )

            _uiState.update {
                it.copy(
                    dailyDare = dare,
                    streakCount = updatedStreak,
                    isLoading = false,
                )
            }
        }
    }

    /**
     * Mark the daily dare as completed. Increments streak.
     */
    fun completeDare() {
        viewModelScope.launch {
            val dare = _uiState.value.dailyDare ?: return@launch

            contentRepository.recordCompletion(dare.id, "local")

            // Update streak
            val todayDayNumber = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
            val lastActive = preferences.getLastActiveDate()

            val newStreak = if (lastActive != null && todayDayNumber == lastActive) {
                _uiState.value.streakCount // Already counted today
            } else {
                _uiState.value.streakCount + 1
            }

            preferences.setStreakCount(newStreak)
            preferences.setLastActiveDate(todayDayNumber)

            _uiState.update {
                it.copy(
                    streakCount = newStreak,
                    dareCompleted = true,
                )
            }
        }
    }

    /**
     * Skip the current dare and load a new one.
     */
    fun skipDare() {
        viewModelScope.launch {
            val dare = _uiState.value.dailyDare ?: return@launch
            contentRepository.recordSkip(dare.id, "local")

            val newDare = contentRepository.getRandomDare(
                hasFire = _uiState.value.isFireUnlocked,
            )

            _uiState.update {
                it.copy(dailyDare = newDare, dareCompleted = false)
            }
        }
    }

    /**
     * Favorite the current dare.
     */
    fun favoriteDare() {
        viewModelScope.launch {
            val dare = _uiState.value.dailyDare ?: return@launch
            contentRepository.recordFavorite(dare.id, "local")
        }
    }

    /**
     * Block the current dare forever and load a new one.
     */
    fun blockDare() {
        viewModelScope.launch {
            val dare = _uiState.value.dailyDare ?: return@launch
            contentRepository.blockContent(dare.id, "local")

            val newDare = contentRepository.getRandomDare(
                hasFire = _uiState.value.isFireUnlocked,
            )

            _uiState.update {
                it.copy(dailyDare = newDare, dareCompleted = false)
            }
        }
    }
}
