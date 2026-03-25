package com.igniteai.app.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.igniteai.app.ui.components.IgniteButton
import com.igniteai.app.ui.components.IgniteCard
import com.igniteai.app.ui.theme.AbyssBlack
import com.igniteai.app.ui.theme.CharcoalDark
import com.igniteai.app.ui.theme.ConsentGreen
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.MoltenGold
import com.igniteai.app.ui.theme.TextMuted
import com.igniteai.app.ui.theme.TextSecondary

/**
 * Couple pairing screen with two methods:
 *
 * Tab 1 — "Show Code": Generate a 6-digit invite code.
 *   Share it with your partner who enters it on their device.
 *
 * Tab 2 — "Enter Code": Enter a code received from your partner.
 *
 * Future: Tab 3 — "Scan QR" with camera viewfinder.
 *
 * When pairing succeeds, encryption keys are exchanged and
 * the couple profile is created in Room.
 */
@Composable
fun PairingScreen(
    inviteCode: String?,
    inviteCodeInput: String,
    pairingStatus: OnboardingViewModel.PairingStatus,
    qrPayload: String?,
    onGenerateCode: () -> Unit,
    onInviteCodeInputChanged: (String) -> Unit,
    onJoinWithCode: () -> Unit,
    onGenerateQr: () -> Unit,
    onSkipPairing: () -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AbyssBlack)
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Connect With Your Partner",
            style = MaterialTheme.typography.headlineLarge,
            color = EmberOrange,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Both devices need to be nearby.\nChoose a pairing method below.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tab selector: Show Code | Enter Code
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CharcoalDark,
            contentColor = EmberOrange,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = EmberOrange,
                )
            },
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Show Code") },
                selectedContentColor = EmberOrange,
                unselectedContentColor = TextMuted,
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Enter Code") },
                selectedContentColor = EmberOrange,
                unselectedContentColor = TextMuted,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        when (selectedTab) {
            0 -> ShowCodeTab(
                inviteCode = inviteCode,
                pairingStatus = pairingStatus,
                onGenerateCode = onGenerateCode,
            )

            1 -> EnterCodeTab(
                inviteCodeInput = inviteCodeInput,
                pairingStatus = pairingStatus,
                onInviteCodeInputChanged = onInviteCodeInputChanged,
                onJoinWithCode = onJoinWithCode,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Skip option for solo setup
        TextButton(onClick = onSkipPairing) {
            Text(
                text = "Skip — I'll pair with my partner later",
                color = TextMuted,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

/**
 * Tab content for generating and showing an invite code.
 */
@Composable
private fun ShowCodeTab(
    inviteCode: String?,
    pairingStatus: OnboardingViewModel.PairingStatus,
    onGenerateCode: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (inviteCode == null) {
            IgniteCard(glowing = true) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Generate a code and share it with your partner",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    IgniteButton(
                        text = "Generate Invite Code",
                        onClick = onGenerateCode,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        } else {
            IgniteCard(glowing = true) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Share this code with your partner:",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Large monospaced code display
                    Text(
                        text = inviteCode,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 8.sp,
                        ),
                        color = MoltenGold,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = when (pairingStatus) {
                            OnboardingViewModel.PairingStatus.WAITING_FOR_PARTNER ->
                                "Waiting for your partner to enter this code..."
                            OnboardingViewModel.PairingStatus.CONNECTED ->
                                "Connected!"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (pairingStatus == OnboardingViewModel.PairingStatus.CONNECTED)
                            ConsentGreen else TextMuted,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

/**
 * Tab content for entering a code received from partner.
 */
@Composable
private fun EnterCodeTab(
    inviteCodeInput: String,
    pairingStatus: OnboardingViewModel.PairingStatus,
    onInviteCodeInputChanged: (String) -> Unit,
    onJoinWithCode: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IgniteCard {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Enter the code from your partner's device:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = inviteCodeInput,
                    onValueChange = { newValue ->
                        val digitsOnly = newValue.filter { it.isDigit() }.take(6)
                        onInviteCodeInputChanged(digitsOnly)
                    },
                    label = { Text("6-digit code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                    ),
                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                        textAlign = TextAlign.Center,
                        letterSpacing = 4.sp,
                        fontFamily = FontFamily.Monospace,
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmberOrange,
                        unfocusedBorderColor = TextMuted,
                        cursorColor = EmberOrange,
                        focusedLabelColor = EmberOrange,
                    ),
                )

                Spacer(modifier = Modifier.height(20.dp))

                IgniteButton(
                    text = "Join Partner",
                    onClick = onJoinWithCode,
                    enabled = inviteCodeInput.length == 6,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
