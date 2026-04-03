package com.igniteai.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.igniteai.app.ui.components.EmberParticles
import com.igniteai.app.ui.components.IgniteButton
import com.igniteai.app.ui.components.StreakCounter
import com.igniteai.app.ui.theme.AbyssBlack
import com.igniteai.app.ui.theme.CharcoalDark
import com.igniteai.app.ui.theme.ConnectionActive
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.TextMuted
import com.igniteai.app.ui.theme.TextSecondary

/**
 * Home Screen — the app's main hub after onboarding.
 *
 * Layout (top to bottom):
 * - Header: partner names + connection status indicator
 * - Daily dare card (the star of the show)
 * - Streak counter
 * - Action buttons: Start Session, Tease, Vault
 * - Settings FAB in bottom-right corner
 * - Background: subtle ember particles
 *
 * This is the screen users see daily. Everything is designed to
 * feel immediate — one glance shows today's dare, your streak,
 * and quick-start buttons for deeper experiences.
 */
@Composable
fun HomeScreen(
    uiState: HomeViewModel.HomeUiState,
    onCompleteDare: () -> Unit,
    onSkipDare: () -> Unit,
    onFavoriteDare: () -> Unit,
    onBlockDare: () -> Unit,
    onStartSession: () -> Unit,
    onOpenVault: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AbyssBlack),
    ) {
        // Background particles
        EmberParticles(particleCount = 12, intensity = 2)

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = EmberOrange)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // ── Header: connection status ──────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "IgniteAI",
                        style = MaterialTheme.typography.headlineLarge,
                        color = EmberOrange,
                    )

                    // Connection indicator
                    Icon(
                        imageVector = if (uiState.isConnected)
                            Icons.Filled.Bluetooth
                        else
                            Icons.Filled.BluetoothDisabled,
                        contentDescription = if (uiState.isConnected)
                            "Connected to partner"
                        else
                            "Not connected",
                        tint = if (uiState.isConnected) ConnectionActive else TextMuted,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Daily Dare ─────────────────────────────────
                val dare = uiState.dailyDare
                if (dare != null) {
                    Text(
                        text = "Today's Dare",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextMuted,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DailyDareCard(
                        dare = dare,
                        dareCompleted = uiState.dareCompleted,
                        onComplete = onCompleteDare,
                        onSkip = onSkipDare,
                        onFavorite = onFavoriteDare,
                        onBlock = onBlockDare,
                    )
                } else {
                    // No content loaded
                    Text(
                        text = "No dares available yet.\nContent is loading...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Streak ─────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    StreakCounter(streakCount = uiState.streakCount)
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ── Quick Actions ──────────────────────────────
                IgniteButton(
                    text = "Start Session",
                    onClick = onStartSession,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    IgniteButton(
                        text = "Vault",
                        onClick = onOpenVault,
                        modifier = Modifier.weight(1f),
                    )
                    IgniteButton(
                        text = "Settings",
                        onClick = onOpenSettings,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
