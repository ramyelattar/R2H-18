package com.igniteai.app.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.igniteai.app.ui.components.R2H18Button
import com.igniteai.app.ui.components.R2H18Card
import com.igniteai.app.ui.theme.AbyssBlack
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.TextMuted
import com.igniteai.app.ui.theme.TextSecondary

/**
 * Partner name entry screen.
 *
 * Simple and focused — just one input with clear context
 * about why the name is needed and who will see it.
 */
@Composable
fun PartnerSetupScreen(
    partnerName: String,
    error: String?,
    onNameChanged: (String) -> Unit,
    onContinue: () -> Unit,
) {
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
                text = "What should we call you?",
                style = MaterialTheme.typography.headlineLarge,
                color = EmberOrange,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "This name is only visible to your partner.\nChoose whatever feels right.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(40.dp))

            R2H18Card {
                OutlinedTextField(
                    value = partnerName,
                    onValueChange = { if (it.length <= 20) onNameChanged(it) },
                    label = { Text("Your display name") },
                    placeholder = { Text("e.g., Baby, Love, Your Name") },
                    singleLine = true,
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onContinue() },
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmberOrange,
                        unfocusedBorderColor = TextMuted,
                        cursorColor = EmberOrange,
                        focusedLabelColor = EmberOrange,
                    ),
                )
            }
        }

        R2H18Button(
            text = "Continue",
            onClick = onContinue,
            enabled = partnerName.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
