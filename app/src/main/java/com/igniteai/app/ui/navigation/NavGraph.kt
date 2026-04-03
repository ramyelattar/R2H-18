package com.igniteai.app.ui.navigation

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.igniteai.app.data.repository.FantasyQuestion
import com.igniteai.app.feature.anticipation.AnticipationViewModel
import com.igniteai.app.feature.anticipation.CountdownLockScreen
import com.igniteai.app.feature.anticipation.TeaseSequenceScreen
import com.igniteai.app.feature.audio.AudioPlayerScreen
import com.igniteai.app.feature.audio.AudioViewModel
import com.igniteai.app.feature.auth.AuthGateScreen
import com.igniteai.app.feature.auth.AuthGateViewModel
import com.igniteai.app.feature.challenge.ChallengeScreen
import com.igniteai.app.feature.challenge.ChallengeViewModel
import com.igniteai.app.feature.content.ContentViewModel
import com.igniteai.app.feature.content.DareScreen
import com.igniteai.app.feature.content.TextMessageScreen
import com.igniteai.app.feature.control.ControlViewModel
import com.igniteai.app.feature.control.ControllerScreen
import com.igniteai.app.feature.control.ReceiverScreen
import com.igniteai.app.feature.heartrate.HeartRateScreen
import com.igniteai.app.feature.heartrate.HeartRateViewModel
import com.igniteai.app.feature.home.HomeScreen
import com.igniteai.app.feature.home.HomeViewModel
import com.igniteai.app.feature.onboarding.BiometricSetupScreen
import com.igniteai.app.feature.onboarding.FantasyQuestionnaireScreen
import com.igniteai.app.feature.onboarding.OnboardingViewModel
import com.igniteai.app.feature.onboarding.PairingScreen
import com.igniteai.app.feature.onboarding.PartnerSetupScreen
import com.igniteai.app.feature.onboarding.PinSetupScreen
import com.igniteai.app.feature.onboarding.WelcomeScreen
import com.igniteai.app.feature.payment.PaymentScreen
import com.igniteai.app.feature.payment.PaymentViewModel
import com.igniteai.app.feature.scenario.ScenarioScreen
import com.igniteai.app.feature.scenario.ScenarioViewModel
import com.igniteai.app.feature.session.ConsentGateScreen
import com.igniteai.app.feature.session.CoolDownScreen
import com.igniteai.app.feature.session.SessionScreen
import com.igniteai.app.feature.session.SessionViewModel
import com.igniteai.app.feature.settings.SettingsScreen
import com.igniteai.app.feature.settings.SettingsViewModel
import com.igniteai.app.feature.vault.VaultScreen
import com.igniteai.app.feature.vault.VaultUnlockScreen
import com.igniteai.app.ui.theme.AbyssBlack
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

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

@Composable
fun R2H18NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.WELCOME,
    onboardingViewModel: OnboardingViewModel? = null,
    sessionViewModel: SessionViewModel? = null,
    homeViewModel: HomeViewModel? = null,
    contentViewModel: ContentViewModel? = null,
    audioViewModel: AudioViewModel? = null,
    anticipationViewModel: AnticipationViewModel? = null,
    settingsViewModel: SettingsViewModel? = null,
    authGateViewModel: AuthGateViewModel? = null,
    paymentViewModel: PaymentViewModel? = null,
    scenarioViewModel: ScenarioViewModel? = null,
    controlViewModel: ControlViewModel? = null,
    heartRateViewModel: HeartRateViewModel? = null,
    challengeViewModel: ChallengeViewModel? = null,
    vaultItems: List<com.igniteai.app.data.model.VaultItem> = emptyList(),
    vaultUnlocked: Boolean = false,
    onVaultUnlockRequest: () -> Unit = {},
    onVaultAddItem: (String, String, String) -> Unit = { _, _, _ -> },
    onVaultDeleteItem: (String) -> Unit = {},
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
                onConfirm = { onboardingViewModel.confirmPin() },
            )

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

            if (state.step == OnboardingViewModel.OnboardingStep.FANTASY_QUESTIONNAIRE &&
                state.pairingStatus == OnboardingViewModel.PairingStatus.CONNECTED
            ) {
                navController.navigate(Routes.FANTASY_QUESTIONNAIRE) {
                    popUpTo(Routes.WELCOME) { inclusive = true }
                }
            }
        }

        composable(Routes.FANTASY_QUESTIONNAIRE) {
            val context = LocalContext.current
            val questions = remember(context) { loadFantasyQuestions(context) }

            FantasyQuestionnaireScreen(
                questions = questions,
                onComplete = {
                    onboardingViewModel?.completeOnboarding()
                    navController.navigate(Routes.AUTH_GATE) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
                onSkipAll = {
                    onboardingViewModel?.completeOnboarding()
                    navController.navigate(Routes.AUTH_GATE) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
            )
        }

        // ── Auth Gate ─────────────────────────────────────
        composable(Routes.AUTH_GATE) {
            val uiState by authGateViewModel!!.uiState.collectAsState()

            AuthGateScreen(
                uiState = uiState,
                onRequestBiometric = { authGateViewModel.requestBiometric() },
                onUsePinInstead = { authGateViewModel.showPinEntry() },
                onPinDigit = { authGateViewModel.onPinDigit(it) },
                onPinBackspace = { authGateViewModel.onPinBackspace() },
            )

            if (uiState.state == AuthGateViewModel.AuthState.UNLOCKED) {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.AUTH_GATE) { inclusive = true }
                }
            }
        }

        // ── Main ────────────────────────────────────────────
        composable(Routes.HOME) {
            val uiState by homeViewModel!!.uiState.collectAsState()

            HomeScreen(
                uiState = uiState,
                onCompleteDare = { homeViewModel.completeDare() },
                onSkipDare = { homeViewModel.skipDare() },
                onFavoriteDare = { homeViewModel.favoriteDare() },
                onBlockDare = { homeViewModel.blockDare() },
                onStartSession = {
                    sessionViewModel?.initiateSession()
                    navController.navigate(Routes.CONSENT_GATE)
                },
                onOpenVault = {
                    navController.navigate(Routes.VAULT_UNLOCK)
                },
                onOpenSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
            )
        }

        composable(Routes.SETTINGS) {
            val uiState by settingsViewModel!!.uiState.collectAsState()

            SettingsScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onToneChanged = { settingsViewModel.setTonePreference(it) },
                onVoiceGenderChanged = { settingsViewModel.setVoiceGender(it) },
                onNotificationsToggled = { settingsViewModel.setNotificationsEnabled(it) },
                onSessionTimeLimitChanged = { settingsViewModel.setSessionTimeLimit(it) },
                onDenyDelayChanged = { settingsViewModel.setDenyDelayDuration(it) },
                onPavlovianSoundToggled = { settingsViewModel.setPavlovianSoundEnabled(it) },
                onPavlovianHapticToggled = { settingsViewModel.setPavlovianHapticEnabled(it) },
                onConditioningIntensityChanged = { settingsViewModel.setConditioningIntensity(it) },
                onVoiceSafewordToggled = { settingsViewModel.setVoiceSafewordEnabled(it) },
                onSafewordChanged = { settingsViewModel.setSafeword(it) },
                onDecoyToggled = { settingsViewModel.setDecoyEnabled(it) },
                onWipeData = { /* Will trigger PanicWipeManager from Activity */ },
            )
        }

        // ── Session ─────────────────────────────────────────
        composable(Routes.CONSENT_GATE) {
            val uiState by sessionViewModel!!.uiState.collectAsState()

            ConsentGateScreen(
                localConsented = uiState.localConsented,
                partnerConsented = uiState.partnerConsented,
                onAuthenticateLocal = { sessionViewModel.recordLocalConsent() },
                onCancel = {
                    sessionViewModel.returnToHome()
                    navController.popBackStack(Routes.HOME, inclusive = false)
                },
            )

            if (uiState.state == SessionViewModel.SessionState.ACTIVE) {
                navController.navigate(Routes.SESSION) {
                    popUpTo(Routes.CONSENT_GATE) { inclusive = true }
                }
            }
        }

        composable(Routes.SESSION) {
            val uiState by sessionViewModel!!.uiState.collectAsState()

            SessionScreen(
                uiState = uiState,
                onSafeword = { sessionViewModel.triggerSafeword() },
                onCheckInContinue = { sessionViewModel.confirmCheckIn() },
                onCheckInEnd = { sessionViewModel.declineCheckIn() },
                onEndSession = { sessionViewModel.endSession() },
            )

            if (uiState.state == SessionViewModel.SessionState.COOL_DOWN) {
                navController.navigate(Routes.COOL_DOWN) {
                    popUpTo(Routes.SESSION) { inclusive = true }
                }
            }
        }

        composable(Routes.COOL_DOWN) {
            val uiState by sessionViewModel!!.uiState.collectAsState()

            CoolDownScreen(
                safewordTriggered = uiState.safewordTriggered,
                sessionDurationMinutes = uiState.sessionDurationMinutes,
                onReturnHome = {
                    sessionViewModel.returnToHome()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.COOL_DOWN) { inclusive = true }
                    }
                },
            )
        }

        // ── Content (Spark) ─────────────────────────────────
        composable(Routes.DARE) {
            val uiState by contentViewModel!!.uiState.collectAsState()

            androidx.compose.runtime.LaunchedEffect(Unit) {
                contentViewModel.loadContent("DARE")
            }

            DareScreen(
                uiState = uiState,
                onComplete = { contentViewModel.complete() },
                onFavorite = { contentViewModel.favorite() },
                onSkip = { contentViewModel.skip() },
                onBlock = { contentViewModel.block() },
            )
        }

        composable(Routes.TEXT_MESSAGE) {
            val uiState by contentViewModel!!.uiState.collectAsState()

            androidx.compose.runtime.LaunchedEffect(Unit) {
                contentViewModel.loadContent("TEXT")
            }

            TextMessageScreen(
                uiState = uiState,
                onFavorite = { contentViewModel.favorite() },
                onSkip = { contentViewModel.skip() },
                onBlock = { contentViewModel.block() },
            )
        }

        composable(Routes.AUDIO_PLAYER) {
            val uiState by audioViewModel!!.uiState.collectAsState()

            AudioPlayerScreen(
                uiState = uiState,
                onPlayPause = {
                    if (uiState.isPlaying) {
                        audioViewModel.stopAll()
                    } else {
                        audioViewModel.speakText(
                            uiState.currentText.ifBlank { "Take a deep breath with me." },
                        )
                    }
                },
                onStop = { audioViewModel.stopAll() },
                onToggleGender = { audioViewModel.toggleVoiceGender() },
                onVoiceVolumeChange = { audioViewModel.setVoiceVolume(it) },
                onSoundscapeVolumeChange = { audioViewModel.setSoundscapeVolume(it) },
                onBreathTap = { audioViewModel.onBreathTap() },
            )
        }

        // ── Anticipation ────────────────────────────────────
        composable(Routes.TEASE_SEQUENCE) {
            val uiState by anticipationViewModel!!.uiState.collectAsState()

            TeaseSequenceScreen(
                state = uiState.teaseSequence,
            )
        }

        composable(Routes.COUNTDOWN_LOCK) {
            val uiState by anticipationViewModel!!.uiState.collectAsState()
            uiState.lockedContent.firstOrNull()?.let { locked ->
                CountdownLockScreen(
                    lockedContent = locked,
                    onUnlocked = {
                        navController.navigate(Routes.DARE)
                    },
                )
            } ?: PlaceholderScreen("No locked content")
        }

        // ── Vault ───────────────────────────────────────────
        composable(Routes.VAULT_UNLOCK) {
            VaultUnlockScreen(
                isUnlocked = vaultUnlocked,
                onRequestUnlock = onVaultUnlockRequest,
                onEnterVault = {
                    navController.navigate(Routes.VAULT) {
                        popUpTo(Routes.VAULT_UNLOCK) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.VAULT) {
            VaultScreen(
                items = vaultItems,
                onAddItem = onVaultAddItem,
                onDeleteItem = onVaultDeleteItem,
                onBack = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.VAULT) { inclusive = true }
                    }
                },
            )
        }

        // ── Level 2: Fire ───────────────────────────────────
        composable(Routes.PAYMENT) {
            val uiState by paymentViewModel!!.uiState.collectAsState()
            val context = LocalContext.current

            PaymentScreen(
                uiState = uiState,
                onUnlockFire = { paymentViewModel.openPaymentLink(context) },
                onReturnHome = { navController.navigate(Routes.HOME) },
            )
        }

        composable(Routes.SCENARIO) {
            val uiState by scenarioViewModel!!.uiState.collectAsState()

            ScenarioScreen(
                uiState = uiState,
                onChoose = { scenarioViewModel.chooseOption(it) },
                onFinish = { navController.popBackStack() },
            )
        }

        composable(Routes.CONTROLLER) {
            val uiState by controlViewModel!!.uiState.collectAsState()

            ControllerScreen(
                uiState = uiState,
                onTriggerHaptic = { controlViewModel.triggerHaptic(it) },
                onSendCommand = { controlViewModel.sendCommand(it) },
                onCommandTextChange = { controlViewModel.setCommandText(it) },
                onSetMode = { controlViewModel.setReceiverMode(it) },
                onSwapRoles = { controlViewModel.requestRoleSwap() },
            )
        }

        composable(Routes.RECEIVER) {
            val uiState by controlViewModel!!.uiState.collectAsState()

            ReceiverScreen(
                uiState = uiState,
            )
        }

        composable(Routes.HEART_RATE) {
            val uiState by heartRateViewModel!!.uiState.collectAsState()

            HeartRateScreen(
                uiState = uiState,
            )
        }

        composable(Routes.CHALLENGE) {
            val uiState by challengeViewModel!!.uiState.collectAsState()

            ChallengeScreen(
                uiState = uiState,
                onStart = { challengeViewModel.startChallenge() },
                onComplete = { challengeViewModel.completeChallenge() },
                onFinish = { navController.popBackStack() },
            )
        }
    }
}

private fun loadFantasyQuestions(context: Context): List<FantasyQuestion> {
    val json = context.assets
        .open("content/fantasy_questions.json")
        .bufferedReader()
        .use { it.readText() }
    return Json { ignoreUnknownKeys = true }.decodeFromString(json)
}

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
