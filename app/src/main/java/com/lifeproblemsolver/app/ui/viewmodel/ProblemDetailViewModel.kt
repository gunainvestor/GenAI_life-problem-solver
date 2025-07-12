package com.lifeproblemsolver.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeproblemsolver.app.data.model.Problem
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
    private val repository: ProblemRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val TAG = "ProblemDetailViewModel"
    private val problemId: Long = savedStateHandle.get<String>("problemId")?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow(ProblemDetailUiState())
    val uiState: StateFlow<ProblemDetailUiState> = _uiState.asStateFlow()

    init {
        if (problemId > 0) {
            loadProblem()
        }
    }

    private fun loadProblem() {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            try {
                val problem = repository.getProblemById(problemId)
                if (problem != null) {
                    _uiState.update { 
                        it.copy(
                            problem = problem,
                            isLoading = false,
                            error = null
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Problem not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load problem"
                    )
                }
            }
        }
    }

    fun generateAiSolution() {
        Log.d(TAG, "generateAiSolution called")
        val currentProblem = _uiState.value.problem ?: return
        
        if (currentProblem.aiSuggestion.isNotBlank()) {
            Log.d(TAG, "AI solution already exists, skipping generation")
            return
        }

        Log.d(TAG, "Starting AI solution generation for problem: ${currentProblem.title}")
        _uiState.update { it.copy(isGeneratingAi = true, error = null) }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Calling repository.generateAiSolution...")
                val aiSolution = repository.generateAiSolution(currentProblem)
                Log.d(TAG, "Received AI solution from repository, length: ${aiSolution.length}")
                
                // Update the problem with AI solution
                Log.d(TAG, "Updating problem with AI solution...")
                repository.updateProblemWithAiSolution(problemId, aiSolution)
                
                // Reload the problem to get updated data
                Log.d(TAG, "Reloading problem...")
                loadProblem()
                
                _uiState.update { it.copy(isGeneratingAi = false) }
                Log.d(TAG, "AI solution generation completed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error generating AI solution", e)
                Log.e(TAG, "Error message: ${e.message}")
                Log.e(TAG, "Error type: ${e.javaClass.simpleName}")
                _uiState.update { 
                    it.copy(
                        isGeneratingAi = false,
                        error = e.message ?: "Failed to generate AI solution"
                    )
                }
            }
        }
    }

    fun markAsResolved() {
        viewModelScope.launch {
            try {
                repository.markProblemAsResolved(problemId)
                loadProblem() // Reload to get updated status
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message ?: "Failed to mark problem as resolved")
                }
            }
        }
    }

    fun deleteProblem() {
        val currentProblem = _uiState.value.problem ?: return
        
        viewModelScope.launch {
            try {
                repository.deleteProblem(currentProblem)
                _uiState.update { it.copy(isDeleted = true) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message ?: "Failed to delete problem")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class ProblemDetailUiState(
    val problem: Problem? = null,
    val isLoading: Boolean = false,
    val isGeneratingAi: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null
) 