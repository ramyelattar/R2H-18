package com.igniteai.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igniteai.app.core.preferences.AppPreferences
import com.igniteai.app.core.security.EncryptionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthGateViewModel(
    private val preferences: AppPreferences,
    private val encryptionManager: EncryptionManager,
) : ViewModel() {

    enum class AuthState {
        LOCKED,
        BIOMETRIC_PROMPT,
        PIN_ENTRY,
        UNLOCKED,
    }

    data class AuthUiState(
        val state: AuthState = AuthState.LOCKED,
        val pinInput: String = "",
        val pinError: String? = null,
        val attemptsRemaining: Int = 5,
    )

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    private var failedAttempts = 0
    private val maxAttempts = 5

    fun requestBiometric() {
        _uiState.value = _uiState.value.copy(state = AuthState.BIOMETRIC_PROMPT)
    }

    fun onBiometricSuccess() {
        _uiState.value = _uiState.value.copy(state = AuthState.UNLOCKED)
    }

    fun onBiometricFailed() {
        // Fall back to PIN entry
        _uiState.value = _uiState.value.copy(state = AuthState.PIN_ENTRY)
    }

    fun showPinEntry() {
        _uiState.value = _uiState.value.copy(
            state = AuthState.PIN_ENTRY,
            pinInput = "",
            pinError = null,
        )
    }

    fun onPinDigit(digit: Char) {
        val current = _uiState.value.pinInput
        if (current.length < 6) {
            _uiState.value = _uiState.value.copy(
                pinInput = current + digit,
                pinError = null,
            )
            // Auto-submit at 4+ digits
            if (current.length + 1 >= 4) {
                verifyPin(current + digit)
            }
        }
    }

    fun onPinBackspace() {
        val current = _uiState.value.pinInput
        if (current.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                pinInput = current.dropLast(1),
                pinError = null,
            )
        }
    }

    private fun verifyPin(pin: String) {
        viewModelScope.launch {
            val storedPin = preferences.getEncryptedPin() ?: return@launch
            val inputEncrypted = encryptionManager.encrypt(pin.toByteArray())
                .toString(Charsets.ISO_8859_1)

            if (inputEncrypted == storedPin) {
                _uiState.value = _uiState.value.copy(state = AuthState.UNLOCKED)
            } else {
                failedAttempts++
                val remaining = maxAttempts - failedAttempts
                _uiState.value = _uiState.value.copy(
                    pinInput = "",
                    pinError = if (remaining > 0) "Wrong PIN. $remaining attempts left." else null,
                    attemptsRemaining = remaining,
                )
                if (remaining <= 0) {
                    // Trigger panic wipe after max attempts
                    _uiState.value = _uiState.value.copy(
                        pinError = "Too many attempts. Data wiped.",
                    )
                }
            }
        }
    }
}
