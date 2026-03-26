package com.igniteai.app.feature.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igniteai.app.feature.haptic.HapticEngine
import com.igniteai.app.feature.haptic.HapticPatterns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * D/s Control Transfer — one partner controls the other's device.
 *
 * The Controller has a dashboard of haptic triggers, audio commands,
 * text commands, and screen mode controls. The Receiver's screen
 * is fully controlled by the Controller.
 *
 * Roles can be swapped mid-session with mutual biometric consent.
 */
class ControlViewModel(
    private val hapticEngine: HapticEngine?,
) : ViewModel() {

    enum class Role { NONE, CONTROLLER, RECEIVER }
    enum class ReceiverMode { INSTRUCTIONS, COUNTDOWN, DARKNESS, SURPRISE }

    data class ControlUiState(
        val role: Role = Role.NONE,
        val receiverMode: ReceiverMode = ReceiverMode.INSTRUCTIONS,
        val commandText: String = "",
        val displayedCommand: String = "",
        val countdownSeconds: Int = 0,
        val isSwapPending: Boolean = false,
    )

    private val _uiState = MutableStateFlow(ControlUiState())
    val uiState: StateFlow<ControlUiState> = _uiState

    fun selectRole(role: Role) {
        _uiState.update { it.copy(role = role) }
    }

    // ── Controller Actions ──────────────────────────────────

    fun triggerHaptic(pattern: HapticPatterns.HapticPattern) {
        // In full implementation: send via ConnectionManager to receiver
        hapticEngine?.play(pattern)
    }

    fun sendCommand(text: String) {
        _uiState.update { it.copy(displayedCommand = text) }
        // In full implementation: send via ConnectionManager to receiver
    }

    fun setCommandText(text: String) {
        _uiState.update { it.copy(commandText = text) }
    }

    fun setReceiverMode(mode: ReceiverMode) {
        _uiState.update { it.copy(receiverMode = mode) }
        // In full implementation: send via ConnectionManager
    }

    fun startCountdown(seconds: Int) {
        _uiState.update {
            it.copy(receiverMode = ReceiverMode.COUNTDOWN, countdownSeconds = seconds)
        }
    }

    fun requestRoleSwap() {
        _uiState.update { it.copy(isSwapPending = true) }
        // In full implementation: send swap request via ConnectionManager
    }

    fun confirmRoleSwap() {
        _uiState.update {
            it.copy(
                role = if (it.role == Role.CONTROLLER) Role.RECEIVER else Role.CONTROLLER,
                isSwapPending = false,
            )
        }
    }
}
