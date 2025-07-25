package com.lifeproblemsolver.app.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeproblemsolver.app.data.callback.DatabaseCallback
import com.lifeproblemsolver.app.data.exception.RateLimitExceededException
import com.lifeproblemsolver.app.data.repository.ProblemRepository
import com.lifeproblemsolver.app.data.repository.UsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProblemDetailViewModel @Inject constructor(
    private val problemRepository: ProblemRepository,
    private val usageRepository: UsageRepository,
    private val databaseCallback: DatabaseCallback,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val problemId: Long = savedStateHandle.get<Long>("problemId") ?: 0L
    
    private val _uiState = MutableStateFlow(ProblemDetailUiState())
    val uiState: StateFlow<ProblemDetailUiState> = _uiState.asStateFlow()
    
    init {
        if (problemId > 0) {
            loadProblem()
            checkRateLimit()
        } else {
            _uiState.update { 
                it.copy(
                    error = "Invalid problem ID. Please try again."
                )
            }
        }
    }

    private fun checkRateLimit() {
        viewModelScope.launch {
            try {
                val hasUserKey = usageRepository.hasUserApiKey()
                val hasReachedLimit = if (!hasUserKey) {
                    usageRepository.hasReachedLimit()
                } else {
                    false
                }
                val currentCount = if (!hasUserKey) {
                    usageRepository.getCurrentRequestCount()
                } else {
                    0
                }
                
                _uiState.update { 
                    it.copy(
                        hasReachedRateLimit = hasReachedLimit,
                        currentRequestCount = currentCount,
                        hasUserApiKey = hasUserKey
                    )
                }
            } catch (e: Exception) {
                // If we can't check rate limit, assume it's okay
                _uiState.update { 
                    it.copy(
                        hasReachedRateLimit = false,
                        currentRequestCount = 0,
                        hasUserApiKey = false
                    )
                }
            }
        }
    }
    
    private fun loadProblem() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val problem = problemRepository.getProblemById(problemId)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        problem = problem
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load problem: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun generateAiSolution(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingAi = true, error = null) }
            
            try {
                val problem = uiState.value.problem ?: return@launch
                val solution = problemRepository.generateAiSolution(problem)
                val updatedProblem = problem.copy(aiSolution = solution, updatedAt = java.time.LocalDateTime.now())
                problemRepository.updateProblem(updatedProblem)
                
                // Trigger automatic Excel export
                databaseCallback.triggerAutoExport(context)
                
                _uiState.update { 
                    it.copy(
                        isGeneratingAi = false,
                        problem = updatedProblem
                    )
                }
                Log.d(TAG, "AI solution generation completed successfully")
            } catch (e: RateLimitExceededException) {
                _uiState.update { 
                    it.copy(
                        isGeneratingAi = false,
                        error = e.message ?: "Rate limit exceeded"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating AI solution", e)
                _uiState.update { 
                    it.copy(
                        isGeneratingAi = false,
                        error = "Failed to generate AI solution: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun deleteProblem(context: Context) {
        viewModelScope.launch {
            try {
                val problem = uiState.value.problem ?: return@launch
                problemRepository.deleteProblem(problem)
                
                // Trigger automatic Excel export
                databaseCallback.triggerAutoExport(context)
                
                _uiState.update { it.copy(shouldNavigateBack = true) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to delete problem: ${e.message}")
                }
            }
        }
    }
    
    fun markAsResolved(context: Context) {
        viewModelScope.launch {
            try {
                problemRepository.markProblemAsResolved(problemId)
                val updatedProblem = uiState.value.problem?.copy(
                    isResolved = true,
                    updatedAt = java.time.LocalDateTime.now()
                )
                
                // Trigger automatic Excel export
                databaseCallback.triggerAutoExport(context)
                
                _uiState.update { 
                    it.copy(problem = updatedProblem)
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to mark problem as resolved: ${e.message}")
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun onNavigateBackHandled() {
        _uiState.update { it.copy(shouldNavigateBack = false) }
    }
    
    companion object {
        private const val TAG = "ProblemDetailViewModel"
    }
}

data class ProblemDetailUiState(
    val problem: com.lifeproblemsolver.app.data.model.Problem? = null,
    val isLoading: Boolean = false,
    val isGeneratingAi: Boolean = false,
    val error: String? = null,
    val shouldNavigateBack: Boolean = false,
    val hasReachedRateLimit: Boolean = false,
    val currentRequestCount: Int = 0,
    val hasUserApiKey: Boolean = false
) 