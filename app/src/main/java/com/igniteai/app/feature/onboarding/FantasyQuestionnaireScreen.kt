package com.igniteai.app.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.igniteai.app.data.repository.FantasyQuestion
import com.igniteai.app.ui.components.IgniteButton
import com.igniteai.app.ui.components.IgniteCard
import com.igniteai.app.ui.theme.AbyssBlack
import com.igniteai.app.ui.theme.CharcoalMedium
import com.igniteai.app.ui.theme.EmberOrange
import com.igniteai.app.ui.theme.MoltenGold
import com.igniteai.app.ui.theme.TextMuted
import com.igniteai.app.ui.theme.TextSecondary

/**
 * One-question-at-a-time fantasy questionnaire.
 *
 * Design principles:
 * - Private: "Your answers are never shown to your partner"
 * - Non-judgmental: warm, accepting tone
 * - Animated transitions between questions
 * - Progress bar shows completion percentage
 * - Skip option on every question (no pressure)
 */
@Composable
fun FantasyQuestionnaireScreen(
    questions: List<FantasyQuestion>,
    onComplete: (Map<String, Any>) -> Unit,
    onSkipAll: () -> Unit,
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    val answers = remember { mutableMapOf<String, Any>() }

    val progress = if (questions.isNotEmpty()) {
        (currentIndex.toFloat()) / questions.size
    } else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AbyssBlack)
            .padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        // Privacy reminder
        Text(
            text = "🔒 Your answers are private and never shown to your partner",
            style = MaterialTheme.typography.bodyLarge,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = EmberOrange,
            trackColor = CharcoalMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${currentIndex + 1} of ${questions.size}",
            style = MaterialTheme.typography.bodyLarge,
            color = TextMuted,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Question card with animated transitions
        if (questions.isNotEmpty() && currentIndex < questions.size) {
            val question = questions[currentIndex]

            AnimatedContent(
                targetState = currentIndex,
                transitionSpec = {
                    (slideInHorizontally { it } + fadeIn())
                        .togetherWith(slideOutHorizontally { -it } + fadeOut())
                },
                label = "question_transition",
                modifier = Modifier.weight(1f),
            ) { index ->
                val q = questions[index]
                QuestionCard(
                    question = q,
                    currentAnswer = answers[q.id],
                    onAnswerChanged = { answer -> answers[q.id] = answer },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (currentIndex > 0) {
                IgniteButton(
                    text = "Back",
                    onClick = { currentIndex-- },
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            if (currentIndex < questions.size - 1) {
                IgniteButton(
                    text = if (answers.containsKey(questions[currentIndex].id)) "Next" else "Skip",
                    onClick = { currentIndex++ },
                    modifier = Modifier.weight(1f),
                )
            } else {
                IgniteButton(
                    text = "Complete",
                    onClick = { onComplete(answers) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * Renders a single question based on its type (scale, choice, checkbox).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuestionCard(
    question: FantasyQuestion,
    currentAnswer: Any?,
    onAnswerChanged: (Any) -> Unit,
) {
    IgniteCard(glowing = true) {
        Column(
            modifier = Modifier.padding(8.dp),
        ) {
            Text(
                text = question.category.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelLarge,
                color = MoltenGold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = question.text,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (question.type) {
                "scale" -> {
                    ScaleInput(
                        options = question.options,
                        labels = question.labels ?: emptyMap(),
                        currentValue = (currentAnswer as? String)?.toIntOrNull()
                            ?: question.options.first().toIntOrNull() ?: 1,
                        onValueChanged = { onAnswerChanged(it.toString()) },
                    )
                }

                "choice" -> {
                    ChoiceInput(
                        options = question.options,
                        currentChoice = currentAnswer as? String,
                        onChoiceSelected = { onAnswerChanged(it) },
                    )
                }

                "checkbox" -> {
                    CheckboxInput(
                        options = question.options,
                        selectedItems = (currentAnswer as? List<*>)
                            ?.filterIsInstance<String>() ?: emptyList(),
                        onSelectionChanged = { onAnswerChanged(it) },
                    )
                }
            }
        }
    }
}

/**
 * Slider-based scale input (1-5 or 1-10).
 */
@Composable
private fun ScaleInput(
    options: List<String>,
    labels: Map<String, String>,
    currentValue: Int,
    onValueChanged: (Int) -> Unit,
) {
    val min = options.first().toFloatOrNull() ?: 1f
    val max = options.last().toFloatOrNull() ?: 5f
    var sliderValue by remember(currentValue) { mutableFloatStateOf(currentValue.toFloat()) }

    Column {
        Slider(
            value = sliderValue,
            onValueChange = {
                sliderValue = it
                onValueChanged(it.toInt())
            },
            valueRange = min..max,
            steps = (max - min - 1).toInt(),
            colors = SliderDefaults.colors(
                thumbColor = EmberOrange,
                activeTrackColor = EmberOrange,
                inactiveTrackColor = CharcoalMedium,
            ),
        )

        // Labels below slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            labels.forEach { (key, label) ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextMuted,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your answer: ${sliderValue.toInt()}",
            style = MaterialTheme.typography.bodyLarge,
            color = EmberOrange,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Radio-button-style single choice input.
 */
@Composable
private fun ChoiceInput(
    options: List<String>,
    currentChoice: String?,
    onChoiceSelected: (String) -> Unit,
) {
    Column {
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onChoiceSelected(option) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = option == currentChoice,
                    onClick = { onChoiceSelected(option) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = EmberOrange,
                        unselectedColor = TextMuted,
                    ),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (option == currentChoice) EmberOrange else TextSecondary,
                )
            }
        }
    }
}

/**
 * Multi-select checkbox input (for interests and boundaries).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CheckboxInput(
    options: List<String>,
    selectedItems: List<String>,
    onSelectionChanged: (List<String>) -> Unit,
) {
    val selected = remember(selectedItems) { mutableStateListOf(*selectedItems.toTypedArray()) }

    Column {
        options.forEach { option ->
            val isSelected = option in selected

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isSelected) selected.remove(option) else selected.add(option)
                        onSelectionChanged(selected.toList())
                    }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = {
                        if (isSelected) selected.remove(option) else selected.add(option)
                        onSelectionChanged(selected.toList())
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = EmberOrange,
                        uncheckedColor = TextMuted,
                        checkmarkColor = AbyssBlack,
                    ),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) EmberOrange else TextSecondary,
                )
            }
        }
    }
}
