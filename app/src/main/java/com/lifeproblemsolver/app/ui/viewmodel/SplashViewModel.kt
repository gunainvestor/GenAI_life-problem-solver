package com.lifeproblemsolver.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeproblemsolver.app.data.preferences.OnboardingPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface SplashDestination {
    object Loading : SplashDestination
    object ShowOnboarding : SplashDestination
    object ShowHome : SplashDestination
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val onboardingPreferencesRepository: OnboardingPreferencesRepository
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination

    init {
        viewModelScope.launch {
            onboardingPreferencesRepository.onboardingState.collect { state ->
                _destination.value = if (state.hasCompleted) {
                    SplashDestination.ShowHome
                } else {
                    SplashDestination.ShowOnboarding
                }
            }
        }
    }
}

