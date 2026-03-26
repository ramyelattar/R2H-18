package com.igniteai.app.feature.onboarding

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igniteai.app.core.preferences.AppPreferences
import com.igniteai.app.core.security.EncryptionManager
import com.igniteai.app.data.model.CoupleProfile
import com.igniteai.app.data.model.Partner
import com.igniteai.app.data.repository.PairingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Manages the onboarding flow state across all onboarding screens.
 *
 * Screens in order:
 *   Welcome → PartnerSetup → BiometricSetup → PinSetup → Pairing → FantasyQuestionnaire
 *
 * The ViewModel survives screen rotations and holds all data
 * entered during onboarding until it's committed to Room at the end.
 */
class OnboardingViewModel(
    private val preferences: AppPreferences,
    private val encryptionManager: EncryptionManager,
    private val pairingRepository: PairingRepository,
) : ViewModel() {

    /**
     * All state for the onboarding flow in a single immutable data class.
     * Compose observes this and re-renders when any field changes.
     */
    data class OnboardingState(
        val step: OnboardingStep = OnboardingStep.WELCOME,
        val partnerName: String = "",
        val biometricEnrolled: Boolean = false,
        val pin: String = "",
        val pinConfirm: String = "",
        val pinError: String? = null,
        val qrPayload: String? = null,
        val inviteCode: String? = null,
        val inviteCodeInput: String = "",
        val pairingStatus: PairingStatus = PairingStatus.IDLE,
        val isLoading: Boolean = false,
        val error: String? = null,
    )

    enum class OnboardingStep {
        WELCOME,
        PARTNER_SETUP,
        BIOMETRIC_SETUP,
        PIN_SETUP,
        PAIRING,
        FANTASY_QUESTIONNAIRE,
    }

    enum class PairingStatus {
        IDLE,
        GENERATING,
        WAITING_FOR_PARTNER,
        CONNECTED,
        FAILED,
    }

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state

    // Generated IDs for current user (stored when pairing completes)
    private val localPartnerId = UUID.randomUUID().toString()
    private var coupleProfileId: String? = null

    // ── Partner Setup ───────────────────────────────────────

    fun setPartnerName(name: String) {
        _state.update { it.copy(partnerName = name) }
    }

    fun confirmPartnerName() {
        if (_state.value.partnerName.isBlank()) {
            _state.update { it.copy(error = "Please enter your name") }
            return
        }
        _state.update {
            it.copy(
                step = OnboardingStep.BIOMETRIC_SETUP,
                error = null,
            )
        }
    }

    // ── Biometric Setup ─────────────────────────────────────

    fun onBiometricSuccess() {
        _state.update {
            it.copy(
                biometricEnrolled = true,
                step = OnboardingStep.PIN_SETUP,
            )
        }
    }

    fun onBiometricSkipped() {
        // Biometric not available — proceed to PIN (which becomes primary auth)
        _state.update {
            it.copy(
                biometricEnrolled = false,
                step = OnboardingStep.PIN_SETUP,
            )
        }
    }

    // ── PIN Setup ───────────────────────────────────────────

    fun setPin(pin: String) {
        _state.update { it.copy(pin = pin, pinError = null) }
    }

    fun setPinConfirm(pinConfirm: String) {
        _state.update { it.copy(pinConfirm = pinConfirm, pinError = null) }
    }

    fun confirmPin() {
        val currentState = _state.value

        if (currentState.pin.length != 6) {
            _state.update { it.copy(pinError = "PIN must be 6 digits") }
            return
        }

        if (currentState.pin != currentState.pinConfirm) {
            _state.update { it.copy(pinError = "PINs don't match") }
            return
        }

        viewModelScope.launch {
            // Encrypt and store the PIN
            val encryptedPin = encryptionManager.encrypt(
                currentState.pin.toByteArray(Charsets.UTF_8)
            )
            val encodedPin = Base64.encodeToString(encryptedPin, Base64.NO_WRAP)
            preferences.setEncryptedPin(encodedPin)

            _state.update {
                it.copy(
                    step = OnboardingStep.PAIRING,
                    pinError = null,
                )
            }
        }
    }

    // ── Pairing ─────────────────────────────────────────────

    fun generateInviteCode() {
        val code = pairingRepository.generateInviteCode()
        _state.update {
            it.copy(
                inviteCode = code,
                pairingStatus = PairingStatus.WAITING_FOR_PARTNER,
            )
        }
    }

    fun generateQrPayload() {
        val payload = pairingRepository.generateQrPayload(
            partnerId = localPartnerId,
            partnerName = _state.value.partnerName,
            publicKey = "", // Will be populated with actual key exchange in full implementation
        )
        _state.update {
            it.copy(
                qrPayload = payload,
                pairingStatus = PairingStatus.WAITING_FOR_PARTNER,
            )
        }
    }

    fun setInviteCodeInput(code: String) {
        _state.update { it.copy(inviteCodeInput = code) }
    }

    /**
     * Attempt to join via invite code. In a full implementation,
     * this would initiate BLE/WiFi discovery using the code as a filter.
     * For now, it simulates a successful pairing.
     */
    fun joinWithInviteCode() {
        viewModelScope.launch {
            _state.update { it.copy(pairingStatus = PairingStatus.GENERATING) }

            // Create the couple profile and partners
            val coupleId = UUID.randomUUID().toString()
            coupleProfileId = coupleId

            val coupleProfile = CoupleProfile(
                id = coupleId,
                createdAt = System.currentTimeMillis(),
                pairingMethod = "INVITE_CODE",
                isActive = true,
            )

            val localPartner = Partner(
                id = localPartnerId,
                displayName = _state.value.partnerName,
                isLocal = true,
                coupleId = coupleId,
                createdAt = System.currentTimeMillis(),
            )

            val remotePartner = Partner(
                id = UUID.randomUUID().toString(),
                displayName = "Partner", // Will be updated when partner connects
                isLocal = false,
                coupleId = coupleId,
                createdAt = System.currentTimeMillis(),
            )

            // Store pairing (with placeholder keys — real key exchange happens over BLE)
            pairingRepository.storePairingResult(
                coupleProfile = coupleProfile,
                localPartner = localPartner,
                remotePartner = remotePartner,
                sharedSecret = ByteArray(32), // Placeholder
                remotePublicKey = ByteArray(32), // Placeholder
                pairingMethod = "INVITE_CODE",
            )

            _state.update {
                it.copy(
                    pairingStatus = PairingStatus.CONNECTED,
                    step = OnboardingStep.FANTASY_QUESTIONNAIRE,
                )
            }
        }
    }

    /**
     * Skip pairing for solo setup (partner pairs later).
     */
    fun skipPairing() {
        viewModelScope.launch {
            val coupleId = UUID.randomUUID().toString()
            coupleProfileId = coupleId

            // Create solo profile — partner can pair later via settings
            _state.update {
                it.copy(
                    pairingStatus = PairingStatus.CONNECTED,
                    step = OnboardingStep.FANTASY_QUESTIONNAIRE,
                )
            }
        }
    }

    // ── Completion ───────────────────────────────────────────

    fun completeOnboarding() {
        viewModelScope.launch {
            preferences.setOnboardingComplete(true)
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun goToStep(step: OnboardingStep) {
        _state.update { it.copy(step = step) }
    }
}
