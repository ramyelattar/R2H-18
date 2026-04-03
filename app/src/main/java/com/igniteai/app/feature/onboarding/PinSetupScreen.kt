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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.igniteai.app.ui.components.R2H18Button
import com.igniteai.app.ui.theme.AbyssBlack
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.SafewordRed
import com.igniteai.app.ui.theme.TextMuted
import com.igniteai.app.ui.theme.TextSecondary

/**
 * 6-digit PIN entry with confirmation step.
 *
 * The PIN serves as a backup authentication method when
 * biometric is unavailable (e.g., wet fingers, device lockout).
 *
 * Visual design: PIN dots light up as digits are entered,
 * creating a satisfying input experience.
 */
@Composable
fun PinSetupScreen(
    pin: String,
    pinConfirm: String,
    pinError: String?,
    onPinChanged: (String) -> Unit,
    onPinConfirmChanged: (String) -> Unit,
    onConfirm: () -> Unit,
) {
    var isConfirmPhase by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AbyssBlack)
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = if (!isConfirmPhase) "Create Your PIN" else "Confirm Your PIN",
                style = MaterialTheme.typography.headlineLarge,
                color = EmberOrange,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "This PIN is your backup if biometric is unavailable.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(48.dp))

            // PIN dots visualization
            PinDots(
                filledCount = if (!isConfirmPhase) pin.length else pinConfirm.length,
                total = 6,
                hasError = pinError != null,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Hidden text field for PIN input (keyboard trigger)
            OutlinedTextField(
                value = if (!isConfirmPhase) pin else pinConfirm,
                onValueChange = { newValue ->
                    val digitsOnly = newValue.filter { it.isDigit() }.take(6)
                    if (!isConfirmPhase) {
                        onPinChanged(digitsOnly)
                        if (digitsOnly.length == 6) {
                            isConfirmPhase = true
                        }
                    } else {
                        onPinConfirmChanged(digitsOnly)
                    }
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done,
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(if (!isConfirmPhase) "Enter 6-digit PIN" else "Re-enter PIN") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmberOrange,
                    unfocusedBorderColor = TextMuted,
                    cursorColor = EmberOrange,
                    focusedLabelColor = EmberOrange,
                ),
            )

            if (pinError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = pinError,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SafewordRed,
                    textAlign = TextAlign.Center,
                )
            }
        }

        R2H18Button(
            text = if (!isConfirmPhase) "Next" else "Set PIN",
            onClick = {
                if (!isConfirmPhase && pin.length == 6) {
                    isConfirmPhase = true
                } else if (isConfirmPhase) {
                    onConfirm()
                }
            },
            enabled = if (!isConfirmPhase) pin.length == 6 else pinConfirm.length == 6,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Visual PIN dot indicator — filled dots for entered digits,
 * empty dots for remaining. Turns red on error.
 */
@Composable
private fun PinDots(
    filledCount: Int,
    total: Int,
    hasError: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { index ->
            val isFilled = index < filledCount
            Surface(
                modifier = Modifier.size(16.dp),
                shape = CircleShape,
                color = when {
                    hasError -> SafewordRed
                    isFilled -> EmberOrange
                    else -> TextMuted.copy(alpha = 0.3f)
                },
            ) {}
            if (index < total - 1) {
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
    }
}
