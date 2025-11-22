package com.lifeproblemsolver.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeproblemsolver.app.ui.theme.*
import com.lifeproblemsolver.app.ui.viewmodel.DecisionFrequency
import com.lifeproblemsolver.app.ui.viewmodel.LifestyleQuestionOption
import com.lifeproblemsolver.app.ui.viewmodel.OnboardingPrompts
import com.lifeproblemsolver.app.ui.viewmodel.OnboardingUiState
import com.lifeproblemsolver.app.ui.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state = viewModel.uiState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        // Full-screen question content
        OnboardingStepContent(
            state = state,
            onMorningSelection = viewModel::onMorningFrictionSelected,
            onWorkDrainSelection = viewModel::onWorkDrainSelected,
            onEveningLoopSelection = viewModel::onEveningLoopSelected,
            onStuckAreaSelection = viewModel::onStuckAreaSelected,
            onPostponedDecisionSelection = viewModel::onPostponedDecisionSelected,
            onSpiralDurationSelection = viewModel::onSpiralDurationSelected,
            onDailyChange = viewModel::onDailyDecisionsChange,
            onTimePerDecisionSelected = viewModel::onTimePerDecisionSelected,
            onRevisitFrequencySelected = viewModel::onRevisitFrequencySelected,
            onDelayFrequencySelected = viewModel::onDelayFrequencySelected,
            onWeeklyHoursChange = viewModel::onWeeklyOverthinkHoursChange,
            onReclaimIntentChange = viewModel::onReclaimFocusIntentChange
        )

        // Progress indicator at top
        ProgressIndicator(
            currentStep = state.currentStep + 1,
            totalSteps = state.totalSteps,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )

        // Navigation buttons at bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (state.currentStep > 0) {
                TextButton(onClick = viewModel::previousStep) {
                    Text("Back", color = Color.White)
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
                shape = RoundedCornerShape(24.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = PrimaryBlue
                )
            ) {
                if (state.currentStep == state.totalSteps - 1) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 8.dp),
                            strokeWidth = 2.dp,
                            color = PrimaryBlue
                        )
                    }
                    Text(if (state.isSaving) "Finishing..." else "See Lost Time", fontWeight = FontWeight.Bold)
                } else {
                    Text("Next", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ProgressIndicator(currentStep: Int, totalSteps: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(totalSteps) { index ->
            val isActive = index < currentStep
            val animatedWeight by animateFloatAsState(
                targetValue = if (isActive) 1f else 0.3f,
                animationSpec = tween(300),
                label = "progress"
            )
            Box(
                modifier = Modifier
                    .weight(animatedWeight)
                    .height(4.dp)
                    .background(
                        color = if (isActive) Color.White else Color.White.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
private fun OnboardingStepContent(
    state: OnboardingUiState,
    onMorningSelection: (String) -> Unit,
    onWorkDrainSelection: (String) -> Unit,
    onEveningLoopSelection: (String) -> Unit,
    onStuckAreaSelection: (String) -> Unit,
    onPostponedDecisionSelection: (String) -> Unit,
    onSpiralDurationSelection: (String) -> Unit,
    onDailyChange: (Float) -> Unit,
    onTimePerDecisionSelected: (Int) -> Unit,
    onRevisitFrequencySelected: (DecisionFrequency) -> Unit,
    onDelayFrequencySelected: (DecisionFrequency) -> Unit,
    onWeeklyHoursChange: (Float) -> Unit,
    onReclaimIntentChange: (String) -> Unit
) {
    when (state.currentStep) {
        0 -> FullScreenQuestionCard(
            question = "What drains you before 9am?",
            icon = Icons.Default.WbSunny,
            gradient = PrimaryGradient,
            content = {
                LifestyleQuestionSelector(
                    options = OnboardingPrompts.morningFriction,
                    selectedId = state.morningFrictionId,
                    onSelected = onMorningSelection
                )
            }
        )

        1 -> FullScreenQuestionCard(
            question = "Where does work stall you most?",
            icon = Icons.Default.Work,
            gradient = AccentGradient,
            content = {
                LifestyleQuestionSelector(
                    options = OnboardingPrompts.workDrain,
                    selectedId = state.workDrainId,
                    onSelected = onWorkDrainSelection
                )
            }
        )

        2 -> FullScreenQuestionCard(
            question = "What loops in your tired brain?",
            icon = Icons.Default.Nightlight,
            gradient = SecondaryGradient,
            content = {
                LifestyleQuestionSelector(
                    options = OnboardingPrompts.eveningLoop,
                    selectedId = state.eveningLoopId,
                    onSelected = onEveningLoopSelection
                )
            }
        )

        3 -> FullScreenQuestionCard(
            question = "Where do you spiral most?",
            icon = Icons.Default.Psychology,
            gradient = AccentGradient,
            content = {
                LifestyleQuestionSelector(
                    options = OnboardingPrompts.stuckAreas,
                    selectedId = state.stuckAreaId,
                    onSelected = onStuckAreaSelection
                )
            }
        )

        4 -> FullScreenQuestionCard(
            question = "What have you postponed 3+ times?",
            icon = Icons.Default.Schedule,
            gradient = PrimaryGradient,
            content = {
                LifestyleQuestionSelector(
                    options = OnboardingPrompts.postponedDecisions,
                    selectedId = state.postponedDecisionId,
                    onSelected = onPostponedDecisionSelection
                )
            }
        )

        5 -> FullScreenQuestionCard(
            question = "Longest time stuck on something tiny?",
            icon = Icons.Default.Timer,
            gradient = SecondaryGradient,
            content = {
                LifestyleQuestionSelector(
                    options = OnboardingPrompts.spiralDurations,
                    selectedId = state.spiralDurationId,
                    onSelected = onSpiralDurationSelection
                )
            }
        )

        6 -> QuestionCard(
            title = "How many micro-decisions do you make daily?",
            subtitle = "Chats, errands, micro tasks, meals…",
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

        7 -> QuestionCard(
            title = "Average time per decision?",
            subtitle = "Quick pick keeps the math honest.",
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

        8 -> QuestionCard(
            title = "How often do you revisit decisions?",
            subtitle = "Include loops and second guessing.",
            content = {
                FrequencyOptions(
                    selected = state.revisitFrequency,
                    onSelected = onRevisitFrequencySelected
                )
            }
        )

        9 -> QuestionCard(
            title = "How often does fatigue delay decisions?",
            subtitle = "How often do you stall from exhaustion?",
            content = {
                FrequencyOptions(
                    selected = state.delayFrequency,
                    onSelected = onDelayFrequencySelected
                )
            }
        )

        10 -> QuestionCard(
            title = "How many hours weekly do you overthink tiny stuff?",
            subtitle = "Give a fast estimate; we’ll finish the math.",
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
private fun FullScreenQuestionCard(
    question: String,
    icon: ImageVector,
    gradient: Brush,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Large icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = Color.White.copy(alpha = 0.9f)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // One-liner question
            Text(
                text = question,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Options content
            content()
        }
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

            LifestyleSummaryRow(
                title = "Morning friction",
                value = OnboardingPrompts.morningFriction.labelFor(state.morningFrictionId)
            )
            LifestyleSummaryRow(
                title = "Work bottleneck",
                value = OnboardingPrompts.workDrain.labelFor(state.workDrainId)
            )
            LifestyleSummaryRow(
                title = "Most postponed",
                value = OnboardingPrompts.postponedDecisions.labelFor(state.postponedDecisionId)
            )
            LifestyleSummaryRow(
                title = "Longest spiral",
                value = OnboardingPrompts.spiralDurations.labelFor(state.spiralDurationId)
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
private fun LifestyleQuestionSelector(
    options: List<LifestyleQuestionOption>,
    selectedId: String,
    onSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        options.chunked(2).forEach { chunk ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                chunk.forEach { option ->
                    val isSelected = option.id == selectedId
                    val animatedElevation by animateFloatAsState(
                        targetValue = if (isSelected) 12f else 4f,
                        animationSpec = tween(300),
                        label = "elevation"
                    )
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                            .shadow(
                                elevation = animatedElevation.dp,
                                shape = RoundedCornerShape(20.dp),
                                spotColor = if (isSelected) Color.White.copy(alpha = 0.5f) else Color.Transparent
                            )
                            .clickable { onSelected(option.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.9f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = option.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = option.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) PrimaryBlue.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (chunk.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun LifestyleSummaryRow(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

private fun List<LifestyleQuestionOption>.labelFor(id: String): String =
    firstOrNull { it.id == id }?.title ?: firstOrNull()?.title ?: ""

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

