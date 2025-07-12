package com.lifeproblemsolver.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeproblemsolver.app.data.exception.RateLimitExceededException
import com.lifeproblemsolver.app.data.repository.ProblemRepository
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
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val problemId: Long = checkNotNull(savedStateHandle["problemId"]).toString().toLong()
    
    private val _uiState = MutableStateFlow(ProblemDetailUiState())
    val uiState: StateFlow<ProblemDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadProblem()
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
    
    fun generateAiSolution() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingAi = true, error = null) }
            
            try {
                val problem = uiState.value.problem ?: return@launch
                val solution = problemRepository.generateAiSolution(problem)
                val updatedProblem = problem.copy(aiSolution = solution, updatedAt = java.time.LocalDateTime.now())
                problemRepository.updateProblem(updatedProblem)
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
    
    fun deleteProblem() {
        viewModelScope.launch {
            try {
                val problem = uiState.value.problem ?: return@launch
                problemRepository.deleteProblem(problem)
                _uiState.update { it.copy(shouldNavigateBack = true) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to delete problem: ${e.message}")
                }
            }
        }
    }
    
    fun markAsResolved() {
        viewModelScope.launch {
            try {
                problemRepository.markProblemAsResolved(problemId)
                val updatedProblem = uiState.value.problem?.copy(
                    isResolved = true,
                    updatedAt = java.time.LocalDateTime.now()
                )
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
    val shouldNavigateBack: Boolean = false
) 