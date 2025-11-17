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
        LifestyleQuestionOption("outfit", "Picking outfits or meals", "Every morning starts with tiny negotiations"),
        LifestyleQuestionOption("messages", "Catching up on messages", "WhatsApp, Slack, DMs waiting for answers"),
        LifestyleQuestionOption("planning", "Planning the day", "Calendar, priorities, endless micro planning"),
        LifestyleQuestionOption("family", "Family / household prep", "Coordinating kids, chores, breakfast chaos")
    )

    val workDrain = listOf(
        LifestyleQuestionOption("priorities", "Prioritizing tasks", "Choosing what actually matters drains you"),
        LifestyleQuestionOption("requests", "Saying yes / no to requests", "Context switching every time someone pings you"),
        LifestyleQuestionOption("delegation", "Delegating or asking for help", "Deciding who should own what takes energy"),
        LifestyleQuestionOption("meetings", "Meetings & approvals", "Waiting for decisions keeps work on pause")
    )

    val eveningLoop = listOf(
        LifestyleQuestionOption("meals", "Dinner & plans", "Cooking or ordering, social plans, family coordination"),
        LifestyleQuestionOption("messages", "Replying to people", "Unread chats, DMs, emails nagging you at night"),
        LifestyleQuestionOption("entertainment", "Scrolling / streaming choices", "Netflix roulette, doom scrolling, nothing satisfying"),
        LifestyleQuestionOption("chores", "Household chores", "Laundry, cleaning, errands delayed all day")
    )

    val stuckAreas = listOf(
        LifestyleQuestionOption("health", "Health & habits", "Workout, water, meds, bedtime routines"),
        LifestyleQuestionOption("career", "Career & work decisions", "Pitching ideas, asking for feedback, new opportunities"),
        LifestyleQuestionOption("money", "Money & admin", "Payments, paperwork, follow-ups you keep postponing"),
        LifestyleQuestionOption("relationships", "Relationships & social", "Conversations, boundaries, saying what you mean")
    )

    val postponedDecisions = listOf(
        LifestyleQuestionOption("reply", "Replying to someone", "You typed it mentally 3 times already"),
        LifestyleQuestionOption("appointment", "Booking a call / appointment", "It lives on your list forever"),
        LifestyleQuestionOption("task", "Finishing a nagging to-do", "Quick task, zero motivation"),
        LifestyleQuestionOption("planning", "Planning something personal", "Trips, hobbies, passion projects waiting on you")
    )

    val spiralDurations = listOf(
        LifestyleQuestionOption("5min", "5 minutes+", "Feels tiny but it compounds fast"),
        LifestyleQuestionOption("15min", "15 minutes+", "Half a break gone on one choice"),
        LifestyleQuestionOption("30min", "30 minutes+", "Half an hour lost thinking about it"),
        LifestyleQuestionOption("60min", "1 hour or more", "An entire focus block gone")
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

