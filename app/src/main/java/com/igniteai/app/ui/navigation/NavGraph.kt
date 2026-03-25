package com.igniteai.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
 * All screens are placeholder Text() composables for now.
 * Actual screens replace these as each feature task is completed.
 */
@Composable
fun IgniteNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.WELCOME,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        // ── Onboarding ──────────────────────────────────────
        composable(Routes.WELCOME) {
            PlaceholderScreen("Welcome to IgniteAI 🔥")
        }
        composable(Routes.PARTNER_SETUP) {
            PlaceholderScreen("Partner Setup")
        }
        composable(Routes.BIOMETRIC_SETUP) {
            PlaceholderScreen("Biometric Setup")
        }
        composable(Routes.PIN_SETUP) {
            PlaceholderScreen("PIN Setup")
        }
        composable(Routes.PAIRING) {
            PlaceholderScreen("Couple Pairing")
        }
        composable(Routes.FANTASY_QUESTIONNAIRE) {
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
