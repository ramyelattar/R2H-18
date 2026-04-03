package com.igniteai.app.feature.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igniteai.app.data.model.ContentItem
import com.igniteai.app.data.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for content consumption screens (Dare, Text, Audio).
 *
 * Manages the content queue with preloading — while the user views
 * the current item, the next one is already fetched. This creates
 * instant transitions that feel polished and intentional.
 *
 * Every interaction (complete, skip, favorite, block) feeds into
 * the engagement tracking system, which powers the adaptive algorithm.
 */
class ContentViewModel(
    private val contentRepository: ContentRepository,
) : ViewModel() {

    data class ContentUiState(
        val currentContent: ContentItem? = null,
        val nextContent: ContentItem? = null,
        val contentType: String = "DARE",
        val isLoading: Boolean = true,
        val contentHistory: List<String> = emptyList(),
    )

    private val _uiState = MutableStateFlow(ContentUiState())
    val uiState: StateFlow<ContentUiState> = _uiState

    /**
     * Load content of a specific type. Called when navigating to a content screen.
     */
    fun loadContent(type: String, hasFire: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(contentType = type, isLoading = true) }

            val current = fetchContent(type, hasFire)
            val next = fetchContent(type, hasFire)

            _uiState.update {
                it.copy(
                    currentContent = current,
                    nextContent = next,
                    isLoading = false,
                )
            }
        }
    }

    /**
     * Mark current content as completed and advance to next.
     */
    fun complete() {
        viewModelScope.launch {
            val current = _uiState.value.currentContent ?: return@launch
            contentRepository.recordCompletion(current.id, "local")
            advance()
        }
    }

    /**
     * Skip current content and advance to next.
     */
    fun skip() {
        viewModelScope.launch {
            val current = _uiState.value.currentContent ?: return@launch
            contentRepository.recordSkip(current.id, "local")
            advance()
        }
    }

    /**
     * Favorite current content (stays on current item).
     */
    fun favorite() {
        viewModelScope.launch {
            val current = _uiState.value.currentContent ?: return@launch
            contentRepository.recordFavorite(current.id, "local")
        }
    }

    /**
     * Block current content forever and advance.
     */
    fun block() {
        viewModelScope.launch {
            val current = _uiState.value.currentContent ?: return@launch
            contentRepository.blockContent(current.id, "local")
            advance()
        }
    }

    /**
     * Advance: move next → current, preload a new next.
     */
    private suspend fun advance() {
        val state = _uiState.value
        val currentId = state.currentContent?.id

        val newNext = fetchContent(state.contentType, false)

        _uiState.update {
            it.copy(
                currentContent = it.nextContent,
                nextContent = newNext,
                contentHistory = if (currentId != null)
                    it.contentHistory + currentId
                else
                    it.contentHistory,
            )
        }
    }

    private suspend fun fetchContent(type: String, hasFire: Boolean): ContentItem? {
        return when (type) {
            "DARE" -> contentRepository.getRandomDare(hasFire = hasFire)
            "TEXT" -> contentRepository.getRandomText(hasFire = hasFire)
            else -> contentRepository.getRandomDare(hasFire = hasFire)
        }
    }
}
