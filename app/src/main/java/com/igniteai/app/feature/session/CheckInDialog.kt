package com.igniteai.app.feature.session

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.igniteai.app.ui.theme.ConsentGreen
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.SafewordRed
import com.igniteai.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

/**
 * Mid-session check-in dialog.
 *
 * Appears at 50% of session time to ensure ongoing consent.
 * Both partners must confirm to continue. Features:
 * - 30-second visible countdown timer
 * - "Continue" and "End Session" buttons
 * - Auto-ends if no response within 2 minutes (handled by ViewModel)
 */
@Composable
fun CheckInDialog(
    onContinue: () -> Unit,
    onEndSession: () -> Unit,
) {
    var timeRemainingSeconds by remember { mutableLongStateOf(30L) }

    // Countdown timer
    LaunchedEffect(Unit) {
        while (timeRemainingSeconds > 0) {
            delay(1000)
            timeRemainingSeconds--
        }
    }

    val progress = timeRemainingSeconds / 30f

    AlertDialog(
        onDismissRequest = { /* Cannot dismiss — must choose */ },
        title = {
            Text(
                text = "Check-In",
                style = MaterialTheme.typography.headlineMedium,
                color = EmberOrange,
            )
        },
        text = {
            Column {
                Text(
                    text = "Still enjoying yourselves?\nBoth partners confirm to continue.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                )

                Spacer(modifier = Modifier.height(16.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = if (timeRemainingSeconds > 10) EmberOrange else SafewordRed,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${timeRemainingSeconds}s remaining",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (timeRemainingSeconds > 10) TextSecondary else SafewordRed,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onContinue) {
                Text("Continue", color = ConsentGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onEndSession) {
                Text("End Session", color = SafewordRed)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}
