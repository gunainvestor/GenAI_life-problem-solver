package com.lifeproblemsolver.app.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeproblemsolver.app.data.preferences.OnboardingPreferencesRepository
import com.lifeproblemsolver.app.data.preferences.OnboardingResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val currentStep: Int = 0,
    val morningFrictionId: String = OnboardingPrompts.morningFriction.first().id,
    val workDrainId: String = OnboardingPrompts.workDrain.first().id,
    val eveningLoopId: String = OnboardingPrompts.eveningLoop.first().id,
    val stuckAreaId: String = OnboardingPrompts.stuckAreas.first().id,
    val postponedDecisionId: String = OnboardingPrompts.postponedDecisions.first().id,
    val spiralDurationId: String = OnboardingPrompts.spiralDurations.first().id,
    val dailyDecisions: Int = 15,
    val timePerDecisionMinutes: Int = 2,
    val revisitFrequency: DecisionFrequency = DecisionFrequency.SOMETIMES,
    val delayFrequency: DecisionFrequency = DecisionFrequency.SOMETIMES,
    val weeklyOverthinkHours: Int = 3,
    val reclaimFocusIntent: String = "",
    val isSaving: Boolean = false,
    val didFinish: Boolean = false
) {
    val totalSteps: Int = 12

    private val revisitMultiplier: Double
        get() = revisitFrequency.multiplier

    private val delayMultiplier: Double
        get() = delayFrequency.multiplier

    val calculatedDailyMinutes: Int
        get() = (dailyDecisions * timePerDecisionMinutes * revisitMultiplier * delayMultiplier).roundToInt()

    val calculatedWeeklyMinutes: Int
        get() = (calculatedDailyMinutes * 7) + (weeklyOverthinkHours * 60)

    val calculatedWeeklyHoursDisplay: String
        get() = "%.1f".format(calculatedWeeklyMinutes / 60.0)
}

enum class DecisionFrequency(val label: String, val multiplier: Double) {
    RARELY("Rarely", 1.0),
    SOMETIMES("Sometimes", 1.2),
    OFTEN("Often", 1.4),
    CONSTANTLY("Constantly", 1.6)
}

data class LifestyleQuestionOption(
    val id: String,
    val title: String,
    val subtitle: String
)

object OnboardingPrompts {
    val morningFriction = listOf(
        LifestyleQuestionOption("outfit", "Picking outfits or meals", "Micro choices before coffee"),
        LifestyleQuestionOption("messages", "Catching up on messages", "Pings waiting for answers"),
        LifestyleQuestionOption("planning", "Planning the day", "Calendar, priorities, to-dos"),
        LifestyleQuestionOption("family", "Family / household prep", "Getting everyone out the door")
    )

    val workDrain = listOf(
        LifestyleQuestionOption("priorities", "Prioritizing tasks", "What actually matters first?"),
        LifestyleQuestionOption("requests", "Saying yes / no", "Every new ping needs judgment"),
        LifestyleQuestionOption("delegation", "Delegating / asking help", "Who should own this?"),
        LifestyleQuestionOption("meetings", "Meetings & approvals", "Waiting on other decisions")
    )

    val eveningLoop = listOf(
        LifestyleQuestionOption("meals", "Dinner & plans", "Cooking, ordering, schedules"),
        LifestyleQuestionOption("messages", "Replying to people", "Unread chats at night"),
        LifestyleQuestionOption("entertainment", "Scrolling / streaming", "Netflix roulette again"),
        LifestyleQuestionOption("chores", "Household chores", "Laundry, cleaning, errands")
    )

    val stuckAreas = listOf(
        LifestyleQuestionOption("health", "Health & habits", "Workout, sleep, routines"),
        LifestyleQuestionOption("career", "Career & work", "Pitching, asking, deciding"),
        LifestyleQuestionOption("money", "Money & admin", "Bills, forms, follow-ups"),
        LifestyleQuestionOption("relationships", "Relationships & social", "Talk, text, boundaries")
    )

    val postponedDecisions = listOf(
        LifestyleQuestionOption("reply", "Replying to someone", "Drafted in your head already"),
        LifestyleQuestionOption("appointment", "Booking a call", "Still not scheduled"),
        LifestyleQuestionOption("task", "Finishing a nagging to-do", "Quick but avoided"),
        LifestyleQuestionOption("planning", "Planning something personal", "Trips, hobbies, goals")
    )

    val spiralDurations = listOf(
        LifestyleQuestionOption("5min", "5 minutes+", "Tiny but constant"),
        LifestyleQuestionOption("15min", "15 minutes+", "Half a break gone"),
        LifestyleQuestionOption("30min", "30 minutes+", "Half an hour stalled"),
        LifestyleQuestionOption("60min", "1 hour+", "An entire focus block")
    )
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingPreferencesRepository: OnboardingPreferencesRepository
) : ViewModel() {

    var uiState by mutableStateOf(OnboardingUiState())
        private set

    fun onMorningFrictionSelected(id: String) {
        uiState = uiState.copy(morningFrictionId = id)
    }

    fun onWorkDrainSelected(id: String) {
        uiState = uiState.copy(workDrainId = id)
    }

    fun onEveningLoopSelected(id: String) {
        uiState = uiState.copy(eveningLoopId = id)
    }

    fun onStuckAreaSelected(id: String) {
        uiState = uiState.copy(stuckAreaId = id)
    }

    fun onPostponedDecisionSelected(id: String) {
        uiState = uiState.copy(postponedDecisionId = id)
    }

    fun onSpiralDurationSelected(id: String) {
        uiState = uiState.copy(spiralDurationId = id)
    }

    fun onDailyDecisionsChange(value: Float) {
        uiState = uiState.copy(dailyDecisions = value.toInt())
    }

    fun onTimePerDecisionSelected(minutes: Int) {
        uiState = uiState.copy(timePerDecisionMinutes = minutes)
    }

    fun onRevisitFrequencySelected(frequency: DecisionFrequency) {
        uiState = uiState.copy(revisitFrequency = frequency)
    }

    fun onDelayFrequencySelected(frequency: DecisionFrequency) {
        uiState = uiState.copy(delayFrequency = frequency)
    }

    fun onWeeklyOverthinkHoursChange(value: Float) {
        uiState = uiState.copy(weeklyOverthinkHours = value.toInt())
    }

    fun onReclaimFocusIntentChange(value: String) {
        uiState = uiState.copy(reclaimFocusIntent = value.take(80))
    }

    fun nextStep() {
        uiState = if (uiState.currentStep < uiState.totalSteps - 1) {
            uiState.copy(currentStep = uiState.currentStep + 1)
        } else {
            uiState
        }
    }

    fun previousStep() {
        uiState = if (uiState.currentStep > 0) {
            uiState.copy(currentStep = uiState.currentStep - 1)
        } else {
            uiState
        }
    }

    fun completeOnboarding(onFinished: () -> Unit) {
        if (uiState.isSaving) return
        uiState = uiState.copy(isSaving = true)
        viewModelScope.launch {
            onboardingPreferencesRepository.saveOnboardingResult(
                OnboardingResult(
                    dailyDecisionMinutes = uiState.calculatedDailyMinutes,
                    weeklyDecisionMinutes = uiState.calculatedWeeklyMinutes,
                    reclaimFocusIntent = uiState.reclaimFocusIntent.ifBlank { "Take control" }
                )
            )
            uiState = uiState.copy(isSaving = false, didFinish = true)
            onFinished()
        }
    }
}

