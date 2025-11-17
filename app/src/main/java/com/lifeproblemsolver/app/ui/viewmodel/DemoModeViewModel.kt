package com.lifeproblemsolver.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeproblemsolver.app.data.preferences.OnboardingPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DemoModeUiState(
    val isProcessing: Boolean = false,
    val demoModeEnabled: Boolean = false,
    val message: String? = null,
    val shouldNavigateToOnboarding: Boolean = false
)

@HiltViewModel
class DemoModeViewModel @Inject constructor(
    private val onboardingPreferencesRepository: OnboardingPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DemoModeUiState())
    val uiState: StateFlow<DemoModeUiState> = _uiState.asStateFlow()

    fun toggleDemoMode(enabled: Boolean) {
        if (_uiState.value.isProcessing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            onboardingPreferencesRepository.setOnboardingCompleted(!enabled)
            _uiState.update {
                it.copy(
                    isProcessing = false,
                    demoModeEnabled = enabled,
                    message = if (enabled) "Onboarding ready to replay" else "Demo mode off",
                    shouldNavigateToOnboarding = enabled
                )
            }
        }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun consumeNavigation() {
        _uiState.update { it.copy(shouldNavigateToOnboarding = false, demoModeEnabled = false) }
    }
}

