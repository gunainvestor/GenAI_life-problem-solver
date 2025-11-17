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
    val dailyDecisions: Int = 15,
    val timePerDecisionMinutes: Int = 2,
    val revisitFrequency: DecisionFrequency = DecisionFrequency.SOMETIMES,
    val delayFrequency: DecisionFrequency = DecisionFrequency.SOMETIMES,
    val weeklyOverthinkHours: Int = 3,
    val reclaimFocusIntent: String = "",
    val isSaving: Boolean = false,
    val didFinish: Boolean = false
) {
    val totalSteps: Int = 6

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

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingPreferencesRepository: OnboardingPreferencesRepository
) : ViewModel() {

    var uiState by mutableStateOf(OnboardingUiState())
        private set

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

