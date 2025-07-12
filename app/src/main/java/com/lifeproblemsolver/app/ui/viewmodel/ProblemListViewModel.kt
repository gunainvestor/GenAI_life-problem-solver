package com.lifeproblemsolver.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeproblemsolver.app.data.model.Problem
import com.lifeproblemsolver.app.data.model.Priority
import com.lifeproblemsolver.app.data.repository.ProblemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProblemListViewModel @Inject constructor(
    private val repository: ProblemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProblemListUiState())
    val uiState: StateFlow<ProblemListUiState> = _uiState.asStateFlow()

    private val _filterType = MutableStateFlow(FilterType.ALL)
    val filterType: StateFlow<FilterType> = _filterType.asStateFlow()

    init {
        loadProblems()
        loadCategories()
    }

    fun loadProblems() {
        viewModelScope.launch {
            repository.getAllProblems()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .onCompletion { _uiState.update { it.copy(isLoading = false) } }
                .catch { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Unknown error occurred"
                        )
                    }
                }
                .collect { problems ->
                    _uiState.update { 
                        it.copy(
                            problems = problems,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun loadProblemsByFilter(filterType: FilterType) {
        _filterType.value = filterType
        viewModelScope.launch {
            val problemsFlow = when (filterType) {
                FilterType.ALL -> repository.getAllProblems()
                FilterType.RESOLVED -> repository.getProblemsByStatus(true)
                FilterType.UNRESOLVED -> repository.getProblemsByStatus(false)
                FilterType.HIGH_PRIORITY -> repository.getProblemsByPriority(Priority.HIGH.name)
                FilterType.URGENT -> repository.getProblemsByPriority(Priority.URGENT.name)
            }
            
            problemsFlow
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .onCompletion { _uiState.update { it.copy(isLoading = false) } }
                .catch { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Unknown error occurred"
                        )
                    }
                }
                .collect { problems ->
                    _uiState.update { 
                        it.copy(
                            problems = problems,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun loadProblemsByCategory(category: String) {
        viewModelScope.launch {
            repository.getProblemsByCategory(category)
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .onCompletion { _uiState.update { it.copy(isLoading = false) } }
                .catch { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Unknown error occurred"
                        )
                    }
                }
                .collect { problems ->
                    _uiState.update { 
                        it.copy(
                            problems = problems,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getAllCategories()
                .catch { error ->
                    _uiState.update { 
                        it.copy(error = error.message ?: "Failed to load categories")
                    }
                }
                .collect { categories ->
                    _uiState.update { it.copy(categories = categories) }
                }
        }
    }

    fun deleteProblem(problem: Problem) {
        viewModelScope.launch {
            try {
                repository.deleteProblem(problem)
                // Problems will be automatically updated through Flow
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

    fun createSampleProblems() {
        viewModelScope.launch {
            repository.createSampleProblems()
            loadProblems()
        }
    }
}

data class ProblemListUiState(
    val problems: List<Problem> = emptyList(),
    val categories: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class FilterType {
    ALL, RESOLVED, UNRESOLVED, HIGH_PRIORITY, URGENT
} 