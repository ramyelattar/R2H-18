package com.igniteai.app.feature.payment

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igniteai.app.data.repository.LicenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val licenseRepository: LicenseRepository,
) : ViewModel() {

    enum class PaymentState { IDLE, OPENING_BROWSER, VERIFYING, SUCCESS, FAILED }

    data class PaymentUiState(
        val state: PaymentState = PaymentState.IDLE,
        val isFireUnlocked: Boolean = false,
        val error: String? = null,
    )

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState

    init {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isFireUnlocked = licenseRepository.isFireUnlocked())
            }
        }
    }

    /**
     * Open Stripe Payment Link in browser.
     */
    fun openPaymentLink(context: Context) {
        val url = licenseRepository.buildPaymentUrl(LicenseRepository.LEVEL_FIRE)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
        _uiState.update { it.copy(state = PaymentState.OPENING_BROWSER) }
    }

    /**
     * Handle return from Stripe payment (deep link callback).
     */
    fun handlePaymentReturn(sessionId: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(state = PaymentState.VERIFYING) }

            if (sessionId != null) {
                licenseRepository.storeLicense(
                    LicenseRepository.LEVEL_FIRE,
                    sessionId,
                )
                _uiState.update {
                    it.copy(state = PaymentState.SUCCESS, isFireUnlocked = true)
                }
            } else {
                _uiState.update {
                    it.copy(state = PaymentState.FAILED, error = "Payment could not be verified")
                }
            }
        }
    }
}
