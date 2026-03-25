package com.igniteai.app.core.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Wraps Android's BiometricPrompt API into simple callback-based auth.
 *
 * Handles the complexity of BiometricPrompt (executor, callback object,
 * PromptInfo builder) and exposes a clean interface:
 *
 *   biometricAuth.authenticate(activity, "Session Consent") {
 *       onSuccess = { startSession() }
 *       onFailure = { showError(it) }
 *       onFallbackPin = { showPinScreen() }
 *   }
 *
 * Falls back to PIN if biometric hardware is unavailable.
 */
class BiometricAuthManager {

    /**
     * Check if biometric authentication is available on this device.
     */
    fun isBiometricAvailable(activity: FragmentActivity): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Show biometric prompt. Calls [onSuccess] on authentication,
     * [onFailure] on error, or [onFallbackPin] if biometrics unavailable.
     *
     * @param activity The FragmentActivity hosting the prompt
     * @param title Prompt title (e.g., "Session Consent", "Unlock App")
     * @param subtitle Optional subtitle
     * @param onSuccess Called when authentication succeeds
     * @param onFailure Called with error message on failure
     * @param onFallbackPin Called when biometric is unavailable — show PIN instead
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String = "Authenticate to continue",
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit,
        onFallbackPin: () -> Unit,
    ) {
        // Check if biometrics are available
        if (!isBiometricAvailable(activity)) {
            onFallbackPin()
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                when (errorCode) {
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> onFallbackPin()
                    BiometricPrompt.ERROR_USER_CANCELED -> onFailure("Authentication cancelled")
                    BiometricPrompt.ERROR_LOCKOUT,
                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> onFallbackPin()
                    else -> onFailure(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Biometric didn't match — prompt stays open for retry
                // (Android handles this automatically)
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Use PIN instead")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
    }
}
