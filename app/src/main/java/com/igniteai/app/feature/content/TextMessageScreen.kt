package com.igniteai.app.feature.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.igniteai.app.data.model.ContentItem
import com.igniteai.app.ui.theme.AbyssBlack
import com.igniteai.app.ui.theme.CharcoalMedium
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.TextMuted
import com.igniteai.app.ui.theme.TextPrimary
import com.igniteai.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

/**
 * Text message screen — displays content as chat bubbles.
 *
 * The magic: messages appear one at a time with a typing indicator
 * delay, creating the illusion that someone is texting the couple
 * something intimate in real-time. This builds anticipation with
 * each message reveal.
 *
 * The text body is split on newlines — each line becomes a separate
 * bubble. This turns a single content item into a multi-message
 * conversation experience.
 */
@Composable
fun TextMessageScreen(
    uiState: ContentViewModel.ContentUiState,
    onFavorite: () -> Unit,
    onSkip: () -> Unit,
    onBlock: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AbyssBlack),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Header
            Text(
                text = "Messages",
                style = MaterialTheme.typography.headlineMedium,
                color = EmberOrange,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Message bubbles
            val content = uiState.currentContent
            if (content != null) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    MessageBubbles(content = content)
                }

                // Auto-scroll to bottom as messages appear
                LaunchedEffect(content.id) {
                    delay(500)
                    scrollState.animateScrollTo(scrollState.maxValue)
                }
            } else {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No messages available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                    )
                }
            }

            // Feedback bar
            ContentFeedbackBar(
                onFavorite = onFavorite,
                onSkip = onSkip,
                onBlock = onBlock,
            )
        }
    }
}

/**
 * Renders the content body as individual chat bubbles with typing delay.
 *
 * Each line of the body text becomes a separate message bubble.
 * Messages appear one at a time with a 1-2 second delay simulating typing.
 */
@Composable
private fun MessageBubbles(content: ContentItem) {
    val lines = content.body.split("\n").filter { it.isNotBlank() }
    val visibleMessages = remember(content.id) { mutableStateListOf<String>() }
    var showTyping by remember(content.id) { mutableStateOf(false) }

    // Reveal messages one at a time
    LaunchedEffect(content.id) {
        visibleMessages.clear()
        for (line in lines) {
            showTyping = true
            delay(800 + (line.length * 20L).coerceAtMost(1200)) // Typing time proportional to length
            showTyping = false
            visibleMessages.add(line)
            delay(300) // Brief pause between messages
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (message in visibleMessages) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            ) {
                MessageBubble(text = message)
            }
        }

        // Typing indicator
        if (showTyping) {
            TypingIndicator()
        }
    }
}

@Composable
private fun MessageBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 4.dp,
            ),
            color = EmberOrange.copy(alpha = 0.15f),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = CharcoalMedium,
        ) {
            Text(
                text = "...",
                style = MaterialTheme.typography.bodyLarge,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}
