package com.lifeproblemsolver.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeproblemsolver.app.data.analytics.AnalyticsService
import com.lifeproblemsolver.app.data.callback.DatabaseCallback
import com.lifeproblemsolver.app.data.exception.RateLimitExceededException
import com.lifeproblemsolver.app.data.model.Priority
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
class AddProblemViewModel @Inject constructor(
    private val repository: ProblemRepository,
    private val analyticsService: AnalyticsService,
    private val usageRepository: UsageRepository,
    private val databaseCallback: DatabaseCallback
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddProblemUiState())
    val uiState: StateFlow<AddProblemUiState> = _uiState.asStateFlow()

    init {
        checkRateLimit()
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

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun appendToDescription(text: String) {
        _uiState.update { currentState ->
            val newDescription = if (currentState.description.isNotBlank()) {
                currentState.description + " " + text
            } else {
                text
            }
            currentState.copy(description = newDescription)
        }
    }

    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun updatePriority(priority: Priority) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun saveProblem(context: Context) {
        val currentState = _uiState.value
        
        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(error = "Title is required") }
            return
        }
        
        if (currentState.description.isBlank()) {
            _uiState.update { it.copy(error = "Description is required") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val problemId = repository.createProblem(
                    title = currentState.title.trim(),
                    description = currentState.description.trim(),
                    category = currentState.category.trim(),
                    priority = currentState.priority
                )
                
                // Save AI solution if it exists
                if (currentState.aiSuggestion.isNotBlank()) {
                    repository.updateProblemWithAiSolution(problemId, currentState.aiSuggestion)
                }
                
                // Log analytics event
                analyticsService.logProblemAdded(
                    category = currentState.category.trim(),
                    priority = currentState.priority.name
                )
                
                // Trigger automatic Excel export
                databaseCallback.triggerAutoExport(context)
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        createdProblemId = problemId
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to save problem"
                    )
                }
            }
        }
    }

    fun generateAiSolution() {
        val currentState = _uiState.value
        
        if (currentState.title.isBlank() && currentState.description.isBlank()) {
            _uiState.update { it.copy(error = "Please provide a problem title or description first") }
            return
        }

        _uiState.update { it.copy(isGeneratingAi = true, error = null) }

        viewModelScope.launch {
            try {
                // Create a temporary problem for AI analysis
                val tempProblem = com.lifeproblemsolver.app.data.model.Problem(
                    title = currentState.title.ifBlank { "Problem" },
                    description = currentState.description.ifBlank { currentState.title },
                    category = currentState.category,
                    priority = currentState.priority
                )
                
                val aiSolution = repository.generateAiSolution(tempProblem)
                
                // Log analytics event
                analyticsService.logAiSolutionRequested(currentState.category.trim())
                
                _uiState.update { 
                    it.copy(
                        isGeneratingAi = false,
                        aiSuggestion = aiSolution
                    )
                }
            } catch (e: RateLimitExceededException) {
                // Log analytics event
                analyticsService.logRateLimitReached()
                
                _uiState.update { 
                    it.copy(
                        isGeneratingAi = false,
                        error = e.message ?: "Rate limit exceeded"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isGeneratingAi = false,
                        error = "Failed to generate AI solution: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetState() {
        _uiState.value = AddProblemUiState()
    }
}

data class AddProblemUiState(
    val title: String = "",
    val description: String = "",
    val notes: String = "",
    val category: String = "General",
    val priority: Priority = Priority.MEDIUM,
    val aiSuggestion: String = "",
    val isLoading: Boolean = false,
    val isGeneratingAi: Boolean = false,
    val isSuccess: Boolean = false,
    val createdProblemId: Long = 0L,
    val error: String? = null,
    val hasReachedRateLimit: Boolean = false,
    val currentRequestCount: Int = 0,
    val hasUserApiKey: Boolean = false
) 