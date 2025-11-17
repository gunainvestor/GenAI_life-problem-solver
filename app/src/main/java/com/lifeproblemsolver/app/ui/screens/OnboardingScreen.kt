package com.lifeproblemsolver.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeproblemsolver.app.ui.viewmodel.DecisionFrequency
import com.lifeproblemsolver.app.ui.viewmodel.OnboardingUiState
import com.lifeproblemsolver.app.ui.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 32.dp)
    ) {
        Text(
            text = "Decision Detox",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Let's measure how much time micro-decisions are stealing.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        ProgressIndicator(currentStep = state.currentStep + 1, totalSteps = state.totalSteps)

        Spacer(modifier = Modifier.height(24.dp))

        OnboardingStepContent(
            state = state,
            onDailyChange = viewModel::onDailyDecisionsChange,
            onTimePerDecisionSelected = viewModel::onTimePerDecisionSelected,
            onRevisitFrequencySelected = viewModel::onRevisitFrequencySelected,
            onDelayFrequencySelected = viewModel::onDelayFrequencySelected,
            onWeeklyHoursChange = viewModel::onWeeklyOverthinkHoursChange,
            onReclaimIntentChange = viewModel::onReclaimFocusIntentChange
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (state.currentStep > 0) {
                TextButton(onClick = viewModel::previousStep) {
                    Text("Back")
                }
            } else {
                Spacer(modifier = Modifier.size(1.dp))
            }

            Button(
                onClick = {
                    if (state.currentStep == state.totalSteps - 1) {
                        viewModel.completeOnboarding(onFinished)
                    } else {
                        viewModel.nextStep()
                    }
                },
                shape = RoundedCornerShape(24.dp)
            ) {
                if (state.currentStep == state.totalSteps - 1) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 8.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(if (state.isSaving) "Finishing..." else "See Lost Time")
                } else {
                    Text("Next")
                }
            }
        }
    }
}

@Composable
private fun ProgressIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalSteps) { index ->
            val isActive = index < currentStep
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .background(
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
            )
        }
    }
}

@Composable
private fun OnboardingStepContent(
    state: OnboardingUiState,
    onDailyChange: (Float) -> Unit,
    onTimePerDecisionSelected: (Int) -> Unit,
    onRevisitFrequencySelected: (DecisionFrequency) -> Unit,
    onDelayFrequencySelected: (DecisionFrequency) -> Unit,
    onWeeklyHoursChange: (Float) -> Unit,
    onReclaimIntentChange: (String) -> Unit
) {
    when (state.currentStep) {
        0 -> QuestionCard(
            title = "How many micro-decisions do you make daily?",
            subtitle = "Chats, errands, micro-tasks, meal choices...",
            content = {
                Slider(
                    value = state.dailyDecisions.toFloat(),
                    onValueChange = onDailyChange,
                    valueRange = 0f..120f,
                    steps = 119,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${state.dailyDecisions} decisions/day",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        )

        1 -> QuestionCard(
            title = "Average time per decision?",
            subtitle = "Being honest helps uncover hidden patterns.",
            content = {
                val options = listOf(1, 2, 3, 5, 10)
                OptionRow(
                    options = options,
                    selected = state.timePerDecisionMinutes,
                    label = { "${it} min" },
                    onSelected = onTimePerDecisionSelected
                )
            }
        )

        2 -> QuestionCard(
            title = "How often do you revisit decisions?",
            subtitle = "Repeating the same thought spiral counts.",
            content = {
                FrequencyOptions(
                    selected = state.revisitFrequency,
                    onSelected = onRevisitFrequencySelected
                )
            }
        )

        3 -> QuestionCard(
            title = "How often does fatigue delay decisions?",
            subtitle = "Decision fatigue slows everything down.",
            content = {
                FrequencyOptions(
                    selected = state.delayFrequency,
                    onSelected = onDelayFrequencySelected
                )
            }
        )

        4 -> QuestionCard(
            title = "How many hours weekly do you overthink tiny stuff?",
            subtitle = "Estimate quickly — we’ll calculate the real cost.",
            content = {
                Slider(
                    value = state.weeklyOverthinkHours.toFloat(),
                    onValueChange = onWeeklyHoursChange,
                    valueRange = 0f..20f,
                    steps = 20,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${state.weeklyOverthinkHours} hrs/week (your guess)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        )

        else -> SummaryCard(state = state, onIntentChange = onReclaimIntentChange)
    }
}

@Composable
private fun QuestionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            content()
        }
    }
}

@Composable
private fun OptionRow(
    options: List<Int>,
    selected: Int,
    label: (Int) -> String,
    onSelected: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        options.chunked(3).forEach { chunk ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                chunk.forEach { option ->
                    val isSelected = option == selected
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clickable { onSelected(option) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = label(option),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FrequencyOptions(
    selected: DecisionFrequency,
    onSelected: (DecisionFrequency) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DecisionFrequency.values().forEach { option ->
            val isSelected = option == selected
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelected(option) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = when (option) {
                            DecisionFrequency.RARELY -> "You rarely double-think."
                            DecisionFrequency.SOMETIMES -> "Occasionally stuck loops."
                            DecisionFrequency.OFTEN -> "Decision spirals slow you down."
                            DecisionFrequency.CONSTANTLY -> "Every choice feels heavy."
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    state: OnboardingUiState,
    onIntentChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "You're losing",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = "${state.calculatedWeeklyHoursDisplay} hrs / week",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = "That's ${state.calculatedDailyMinutes} minutes every single day trapped in micro-decisions. That's time you can reclaim starting now.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            SummaryStat(
                title = "Decision drag (daily)",
                value = "${state.calculatedDailyMinutes} min"
            )
            SummaryStat(
                title = "Weekly brain drain",
                value = "${state.calculatedWeeklyMinutes / 60} hours"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "What would you do with that reclaimed time?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = state.reclaimFocusIntent,
                onValueChange = onIntentChange,
                placeholder = { Text("Start a habit, rest guilt-free, build something...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )
        }
    }
}

@Composable
private fun SummaryStat(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.size(32.dp)
        )
    }
}

