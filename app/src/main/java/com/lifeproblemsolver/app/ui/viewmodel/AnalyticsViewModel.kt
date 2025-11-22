package com.lifeproblemsolver.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeproblemsolver.app.data.repository.ProblemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val problemRepository: ProblemRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()
    
    init {
        loadAnalytics()
    }
    
    fun loadAnalytics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val averageRating = problemRepository.getAverageSolutionRating()
                val ratedCount = problemRepository.getRatedProblemsCount()
                val problemsWithSolution = problemRepository.getProblemsWithSolutionCount()
                
                // Get first value from Flow
                val allProblems = problemRepository.getAllProblems().first()
                
                val totalProblems = allProblems.size
                val resolvedProblems = allProblems.count { it.isResolved }
                val highRatingCount = allProblems.count { it.solutionRating != null && it.solutionRating!! >= 4.0f }
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        averageRating = averageRating ?: 0f,
                        ratedProblemsCount = ratedCount,
                        problemsWithSolutionCount = problemsWithSolution,
                        totalProblemsCount = totalProblems,
                        resolvedProblemsCount = resolvedProblems,
                        highRatingCount = highRatingCount
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load analytics: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun refresh() {
        loadAnalytics()
    }
}

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val averageRating: Float = 0f,
    val ratedProblemsCount: Int = 0,
    val problemsWithSolutionCount: Int = 0,
    val totalProblemsCount: Int = 0,
    val resolvedProblemsCount: Int = 0,
    val highRatingCount: Int = 0,
    val error: String? = null
)

