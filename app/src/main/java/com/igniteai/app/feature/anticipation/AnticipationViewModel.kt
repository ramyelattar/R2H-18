package com.igniteai.app.feature.anticipation

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
 * Anticipation Engine — manages tease sequences and countdown locks.
 *
 * Tease Sequences:
 *   Scheduled series of escalating messages delivered throughout the day.
 *   Morning = low intensity (hints, suggestions). Evening = high intensity
 *   (commands, challenges). Both partners get different but coordinated
 *   messages. Builds tension across hours before a session.
 *
 * Countdown Locks:
 *   Content that is visible but locked behind a timer. The user can see
 *   a blurred preview and a countdown. As the timer approaches zero,
 *   ember particles intensify. When it hits zero — dramatic reveal.
 *   This creates the "Christmas morning" effect: the anticipation of
 *   opening is often more exciting than the gift itself.
 *
 * Deny & Delay (integration with SessionViewModel):
 *   Random pauses during active sessions. The screen dims, a countdown
 *   appears, and everything stops. The wait IS the feature.
 */
class AnticipationViewModel(
    private val contentRepository: ContentRepository,
) : ViewModel() {

    // ── Tease Sequence State ────────────────────────────────

    data class TeaseMessage(
        val id: String,
        val text: String,
        val intensity: Int,
        val scheduledTimeMs: Long,
        val delivered: Boolean = false,
    )

    data class TeaseSequenceState(
        val messages: List<TeaseMessage> = emptyList(),
        val currentMessageIndex: Int = -1,
        val isActive: Boolean = false,
        val nextTeaseInMs: Long = 0,
    )

    // ── Countdown Lock State ────────────────────────────────

    data class LockedContent(
        val contentId: String,
        val contentItem: ContentItem? = null,
        val unlockTimeMs: Long,
        val remainingMs: Long = 0,
    )

    data class AnticipationUiState(
        val teaseSequence: TeaseSequenceState = TeaseSequenceState(),
        val lockedContent: List<LockedContent> = emptyList(),
        val isLoading: Boolean = false,
    )

    private val _uiState = MutableStateFlow(AnticipationUiState())
    val uiState: StateFlow<AnticipationUiState> = _uiState

    private var teaseJob: Job? = null
    private var countdownJob: Job? = null

    // ── Tease Sequences ─────────────────────────────────────

    /**
     * Schedule a tease sequence across a time window.
     *
     * @param startTimeMs When the first tease should fire (epoch ms)
     * @param endTimeMs When the last tease should fire
     * @param messageCount Number of messages in the sequence (3-10)
     */
    fun scheduleTeaseSequence(
        startTimeMs: Long,
        endTimeMs: Long,
        messageCount: Int = 5,
    ) {
        viewModelScope.launch {
            val interval = (endTimeMs - startTimeMs) / messageCount.coerceAtLeast(1)
            val messages = mutableListOf<TeaseMessage>()

            for (i in 0 until messageCount) {
                val scheduledTime = startTimeMs + (i * interval)
                // Intensity scales from 1 to messageCount
                val intensity = ((i + 1).toFloat() / messageCount * 10).toInt().coerceIn(1, 10)

                val content = contentRepository.getRandomText(
                    hasFire = false,
                )

                messages.add(
                    TeaseMessage(
                        id = "tease_$i",
                        text = content?.body ?: "Something exciting is coming...",
                        intensity = intensity,
                        scheduledTimeMs = scheduledTime,
                    )
                )
            }

            _uiState.update {
                it.copy(
                    teaseSequence = TeaseSequenceState(
                        messages = messages,
                        isActive = true,
                        currentMessageIndex = -1,
                    ),
                )
            }

            // Start delivery loop
            startTeaseDelivery()
        }
    }

    private fun startTeaseDelivery() {
        teaseJob?.cancel()
        teaseJob = viewModelScope.launch {
            while (true) {
                val state = _uiState.value.teaseSequence
                if (!state.isActive) break

                val now = System.currentTimeMillis()
                val nextUndelivered = state.messages
                    .indexOfFirst { !it.delivered && it.scheduledTimeMs <= now }

                if (nextUndelivered >= 0) {
                    val updated = state.messages.toMutableList()
                    updated[nextUndelivered] = updated[nextUndelivered].copy(delivered = true)

                    _uiState.update {
                        it.copy(
                            teaseSequence = state.copy(
                                messages = updated,
                                currentMessageIndex = nextUndelivered,
                            ),
                        )
                    }
                }

                // Calculate time until next tease
                val nextPending = state.messages.firstOrNull { !it.delivered }
                if (nextPending != null) {
                    _uiState.update {
                        it.copy(
                            teaseSequence = it.teaseSequence.copy(
                                nextTeaseInMs = nextPending.scheduledTimeMs - now,
                            ),
                        )
                    }
                } else {
                    // All delivered
                    _uiState.update {
                        it.copy(
                            teaseSequence = it.teaseSequence.copy(isActive = false),
                        )
                    }
                    break
                }

                delay(1000)
            }
        }
    }

    /**
     * Cancel the active tease sequence.
     */
    fun cancelTeaseSequence() {
        teaseJob?.cancel()
        _uiState.update {
            it.copy(teaseSequence = TeaseSequenceState())
        }
    }

    // ── Countdown Locks ─────────────────────────────────────

    /**
     * Lock a content item behind a countdown timer.
     *
     * @param contentId ID of the content to lock
     * @param unlockAfterMs Milliseconds from now until unlock
     */
    fun lockContent(contentId: String, unlockAfterMs: Long) {
        viewModelScope.launch {
            val content = contentRepository.getContentById(contentId)
            val unlockTime = System.currentTimeMillis() + unlockAfterMs

            val locked = LockedContent(
                contentId = contentId,
                contentItem = content,
                unlockTimeMs = unlockTime,
                remainingMs = unlockAfterMs,
            )

            _uiState.update {
                it.copy(lockedContent = it.lockedContent + locked)
            }

            startCountdownUpdater()
        }
    }

    /**
     * Check if a specific content item is unlocked.
     */
    fun isUnlocked(contentId: String): Boolean {
        val locked = _uiState.value.lockedContent.find { it.contentId == contentId }
        return locked == null || locked.remainingMs <= 0
    }

    private fun startCountdownUpdater() {
        if (countdownJob?.isActive == true) return

        countdownJob = viewModelScope.launch {
            while (_uiState.value.lockedContent.any { it.remainingMs > 0 }) {
                val now = System.currentTimeMillis()
                val updated = _uiState.value.lockedContent.map { locked ->
                    locked.copy(
                        remainingMs = (locked.unlockTimeMs - now).coerceAtLeast(0),
                    )
                }

                _uiState.update { it.copy(lockedContent = updated) }
                delay(1000)
            }
        }
    }
}
