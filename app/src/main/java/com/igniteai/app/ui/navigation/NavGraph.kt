package com.igniteai.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.igniteai.app.feature.onboarding.BiometricSetupScreen
import com.igniteai.app.feature.onboarding.OnboardingViewModel
import com.igniteai.app.feature.onboarding.PairingScreen
import com.igniteai.app.feature.onboarding.PartnerSetupScreen
import com.igniteai.app.feature.onboarding.PinSetupScreen
import com.igniteai.app.feature.onboarding.WelcomeScreen
import com.igniteai.app.ui.theme.AbyssBlack

/**
 * All navigation routes in IgniteAI.
 *
 * Organized by feature area. Each route is a string constant
 * so typos are caught at compile time (via references, not magic strings).
 */
object Routes {
    // Onboarding
    const val WELCOME = "welcome"
    const val PARTNER_SETUP = "partner_setup"
    const val BIOMETRIC_SETUP = "biometric_setup"
    const val PIN_SETUP = "pin_setup"
    const val PAIRING = "pairing"
    const val FANTASY_QUESTIONNAIRE = "fantasy_questionnaire"

    // Main
    const val HOME = "home"
    const val SETTINGS = "settings"

    // Session
    const val CONSENT_GATE = "consent_gate"
    const val SESSION = "session"
    const val COOL_DOWN = "cool_down"

    // Content (Level 1: Spark)
    const val DARE = "dare"
    const val TEXT_MESSAGE = "text_message"
    const val AUDIO_PLAYER = "audio_player"

    // Anticipation (Level 1: Spark)
    const val TEASE_SEQUENCE = "tease_sequence"
    const val COUNTDOWN_LOCK = "countdown_lock"

    // Vault
    const val VAULT_UNLOCK = "vault_unlock"
    const val VAULT = "vault"

    // Level 2: Fire
    const val PAYMENT = "payment"
    const val SCENARIO = "scenario"
    const val CONTROLLER = "controller"
    const val RECEIVER = "receiver"
    const val HEART_RATE = "heart_rate"
    const val CHALLENGE = "challenge"

    // Auth
    const val AUTH_GATE = "auth_gate"
}

/**
 * Main navigation graph.
 *
 * Start destination depends on app state:
 * - No couple profile → WELCOME (onboarding)
 * - Has profile → AUTH_GATE (biometric unlock) → HOME
 *
 * Onboarding screens are wired to real composables.
 * Other screens remain placeholder until their feature tasks complete.
 *
 * @param onboardingViewModel Shared ViewModel for onboarding flow state.
 *        Pass null when onboarding is already complete (skips ViewModel creation).
 */
@Composable
fun IgniteNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.WELCOME,
    onboardingViewModel: OnboardingViewModel? = null,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        // ── Onboarding ──────────────────────────────────────
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onGetStarted = {
                    navController.navigate(Routes.PARTNER_SETUP)
                },
            )
        }

        composable(Routes.PARTNER_SETUP) {
            val state by onboardingViewModel!!.state.collectAsState()

            PartnerSetupScreen(
                partnerName = state.partnerName,
                error = state.error,
                onNameChanged = { onboardingViewModel.setPartnerName(it) },
                onContinue = {
                    onboardingViewModel.confirmPartnerName()
                    if (state.partnerName.isNotBlank()) {
                        navController.navigate(Routes.BIOMETRIC_SETUP)
                    }
                },
            )
        }

        composable(Routes.BIOMETRIC_SETUP) {
            BiometricSetupScreen(
                onEnableBiometric = {
                    // In real implementation, this triggers BiometricAuthManager.authenticate()
                    // from the Activity. For now, treat as success.
                    onboardingViewModel?.onBiometricSuccess()
                    navController.navigate(Routes.PIN_SETUP)
                },
                onSkip = {
                    onboardingViewModel?.onBiometricSkipped()
                    navController.navigate(Routes.PIN_SETUP)
                },
            )
        }

        composable(Routes.PIN_SETUP) {
            val state by onboardingViewModel!!.state.collectAsState()

            PinSetupScreen(
                pin = state.pin,
                pinConfirm = state.pinConfirm,
                pinError = state.pinError,
                onPinChanged = { onboardingViewModel.setPin(it) },
                onPinConfirmChanged = { onboardingViewModel.setPinConfirm(it) },
                onConfirm = {
                    onboardingViewModel.confirmPin()
                    // Navigation happens via state observation —
                    // when step changes to PAIRING, we navigate
                },
            )

            // React to step changes from ViewModel
            if (state.step == OnboardingViewModel.OnboardingStep.PAIRING) {
                navController.navigate(Routes.PAIRING) {
                    popUpTo(Routes.PIN_SETUP) { inclusive = true }
                }
            }
        }

        composable(Routes.PAIRING) {
            val state by onboardingViewModel!!.state.collectAsState()

            PairingScreen(
                inviteCode = state.inviteCode,
                inviteCodeInput = state.inviteCodeInput,
                pairingStatus = state.pairingStatus,
                qrPayload = state.qrPayload,
                onGenerateCode = { onboardingViewModel.generateInviteCode() },
                onInviteCodeInputChanged = { onboardingViewModel.setInviteCodeInput(it) },
                onJoinWithCode = { onboardingViewModel.joinWithInviteCode() },
                onGenerateQr = { onboardingViewModel.generateQrPayload() },
                onSkipPairing = {
                    onboardingViewModel.skipPairing()
                    navController.navigate(Routes.FANTASY_QUESTIONNAIRE) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
            )

            // Navigate when pairing completes
            if (state.step == OnboardingViewModel.OnboardingStep.FANTASY_QUESTIONNAIRE &&
                state.pairingStatus == OnboardingViewModel.PairingStatus.CONNECTED
            ) {
                navController.navigate(Routes.FANTASY_QUESTIONNAIRE) {
                    popUpTo(Routes.WELCOME) { inclusive = true }
                }
            }
        }

        composable(Routes.FANTASY_QUESTIONNAIRE) {
            // Will be implemented in Task 8
            PlaceholderScreen("Fantasy Questionnaire")
        }

        // ── Main ────────────────────────────────────────────
        composable(Routes.HOME) {
            PlaceholderScreen("Home")
        }
        composable(Routes.SETTINGS) {
            PlaceholderScreen("Settings")
        }

        // ── Session ─────────────────────────────────────────
        composable(Routes.CONSENT_GATE) {
            PlaceholderScreen("Consent Gate\nBoth partners authenticate")
        }
        composable(Routes.SESSION) {
            PlaceholderScreen("Active Session")
        }
        composable(Routes.COOL_DOWN) {
            PlaceholderScreen("Cool Down")
        }

        // ── Content (Spark) ─────────────────────────────────
        composable(Routes.DARE) {
            PlaceholderScreen("Daily Dare")
        }
        composable(Routes.TEXT_MESSAGE) {
            PlaceholderScreen("Text Messages")
        }
        composable(Routes.AUDIO_PLAYER) {
            PlaceholderScreen("Audio Player")
        }

        // ── Anticipation ────────────────────────────────────
        composable(Routes.TEASE_SEQUENCE) {
            PlaceholderScreen("Tease Sequence")
        }
        composable(Routes.COUNTDOWN_LOCK) {
            PlaceholderScreen("Countdown Lock")
        }

        // ── Vault ───────────────────────────────────────────
        composable(Routes.VAULT_UNLOCK) {
            PlaceholderScreen("Vault Unlock\nDual biometric required")
        }
        composable(Routes.VAULT) {
            PlaceholderScreen("Forbidden Vault")
        }

        // ── Level 2: Fire ───────────────────────────────────
        composable(Routes.PAYMENT) {
            PlaceholderScreen("Unlock Fire — $29")
        }
        composable(Routes.SCENARIO) {
            PlaceholderScreen("Roleplay Scenario")
        }
        composable(Routes.CONTROLLER) {
            PlaceholderScreen("Controller Mode")
        }
        composable(Routes.RECEIVER) {
            PlaceholderScreen("Receiver Mode")
        }
        composable(Routes.HEART_RATE) {
            PlaceholderScreen("Heart Rate")
        }
        composable(Routes.CHALLENGE) {
            PlaceholderScreen("Couple Challenge")
        }

        // ── Auth ────────────────────────────────────────────
        composable(Routes.AUTH_GATE) {
            PlaceholderScreen("Authentication Required")
        }
    }
}

/**
 * Temporary placeholder screen used until real screens are built.
 */
@Composable
private fun PlaceholderScreen(label: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AbyssBlack),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
