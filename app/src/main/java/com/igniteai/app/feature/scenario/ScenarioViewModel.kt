package com.igniteai.app.feature.scenario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igniteai.app.data.dao.ScenarioDao
import com.igniteai.app.data.model.ScenarioNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * ViewModel for branching roleplay scenarios.
 *
 * Navigates a tree of ScenarioNodes where each node presents
 * narrative text and 2-3 choices. Partner decisions alternate:
 * odd-depth nodes are Partner A's choice, even-depth are Partner B's.
 */
class ScenarioViewModel(
    private val scenarioDao: ScenarioDao,
) : ViewModel() {

    data class ScenarioUiState(
        val currentNode: ScenarioNode? = null,
        val choices: List<String> = emptyList(),
        val depth: Int = 0,
        val isComplete: Boolean = false,
        val scenarioTitle: String = "",
        val isLoading: Boolean = false,
    )

    private val _uiState = MutableStateFlow(ScenarioUiState())
    val uiState: StateFlow<ScenarioUiState> = _uiState

    private val json = Json { ignoreUnknownKeys = true }

    fun startScenario(scenarioId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val root = scenarioDao.getRootNode(scenarioId)
            if (root != null) {
                val choices = parseChoices(root.choicesJson)
                _uiState.update {
                    it.copy(
                        currentNode = root,
                        choices = choices,
                        depth = 0,
                        isComplete = choices.isEmpty(),
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun chooseOption(choiceIndex: Int) {
        viewModelScope.launch {
            val currentNode = _uiState.value.currentNode ?: return@launch
            val children = scenarioDao.getChildren(currentNode.id)
            val nextNode = children.find { it.choiceIndex == choiceIndex }

            if (nextNode != null) {
                val choices = parseChoices(nextNode.choicesJson)
                _uiState.update {
                    it.copy(
                        currentNode = nextNode,
                        choices = choices,
                        depth = it.depth + 1,
                        isComplete = choices.isEmpty(),
                    )
                }
            } else {
                _uiState.update { it.copy(isComplete = true) }
            }
        }
    }

    private fun parseChoices(choicesJson: String?): List<String> {
        if (choicesJson.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<String>>(choicesJson)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
