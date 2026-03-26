package com.igniteai.app.feature.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igniteai.app.data.model.ContentItem
import com.igniteai.app.data.repository.ContentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Synchronized couple challenges with timer and scoring.
 *
 * Partners get different but coordinated instructions.
 * Timer counts down, points for speed and coordination.
 */
class ChallengeViewModel(
    private val contentRepository: ContentRepository,
) : ViewModel() {

    data class ChallengeUiState(
        val challenge: ContentItem? = null,
        val timerSeconds: Int = 0,
        val timerRunning: Boolean = false,
        val score: Int = 0,
        val partnerReady: Boolean = false,
        val isComplete: Boolean = false,
        val isLoading: Boolean = false,
    )

    private val _uiState = MutableStateFlow(ChallengeUiState())
    val uiState: StateFlow<ChallengeUiState> = _uiState

    private var timerJob: Job? = null

    fun loadChallenge() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val challenge = contentRepository.getRandomDare(hasFire = true, minIntensity = 3)
            _uiState.update {
                it.copy(challenge = challenge, timerSeconds = 120, isLoading = false)
            }
        }
    }

    fun startTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(timerRunning = true) }

        timerJob = viewModelScope.launch {
            while (_uiState.value.timerSeconds > 0 && _uiState.value.timerRunning) {
                delay(1000)
                _uiState.update { it.copy(timerSeconds = it.timerSeconds - 1) }
            }
            if (_uiState.value.timerSeconds <= 0) {
                _uiState.update { it.copy(timerRunning = false, isComplete = true) }
            }
        }
    }

    fun completeStep() {
        val timeBonus = _uiState.value.timerSeconds * 10
        _uiState.update {
            it.copy(score = it.score + 100 + timeBonus, isComplete = true, timerRunning = false)
        }
        timerJob?.cancel()
    }
}
