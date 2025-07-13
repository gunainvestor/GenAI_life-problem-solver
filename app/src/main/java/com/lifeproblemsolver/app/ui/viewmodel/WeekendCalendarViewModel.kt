package com.lifeproblemsolver.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeproblemsolver.app.data.callback.DatabaseCallback
import com.lifeproblemsolver.app.data.model.WeekendCalendar
import com.lifeproblemsolver.app.data.repository.WeekendCalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class WeekendCalendarViewModel @Inject constructor(
    private val weekendCalendarRepository: WeekendCalendarRepository,
    private val databaseCallback: DatabaseCallback
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WeekendCalendarUiState())
    val uiState: StateFlow<WeekendCalendarUiState> = _uiState.asStateFlow()
    
    init {
        loadWeekends()
    }
    
    private fun loadWeekends() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Generate weekends if database is empty
                val existingWeekends = weekendCalendarRepository.getAllWeekends().first()
                if (existingWeekends.isEmpty()) {
                    weekendCalendarRepository.generateWeekendsForNext5Years()
                }
                
                // Collect weekends from database
                weekendCalendarRepository.getAllWeekends()
                    .collect { weekends ->
                        _uiState.update { 
                            it.copy(
                                weekends = weekends,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }
    
    fun toggleWeekendSelection(context: Context, date: LocalDate) {
        viewModelScope.launch {
            try {
                val currentWeekend = weekendCalendarRepository.getWeekendByDate(date)
                val newSelectionState = !(currentWeekend?.isSelected ?: false)
                
                weekendCalendarRepository.updateWeekendSelection(date, newSelectionState)
                
                // Trigger automatic Excel export
                databaseCallback.triggerAutoExport(context)
                
                // Update local state immediately for better UX
                _uiState.update { currentState ->
                    val updatedWeekends = currentState.weekends.map { weekend ->
                        if (weekend.date == date) {
                            weekend.copy(isSelected = newSelectionState)
                        } else {
                            weekend
                        }
                    }
                    currentState.copy(weekends = updatedWeekends)
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message ?: "Failed to update selection")
                }
            }
        }
    }
    
    fun updateWeekendNote(context: Context, date: LocalDate, note: String) {
        viewModelScope.launch {
            try {
                weekendCalendarRepository.updateWeekendNote(date, note)
                
                // Trigger automatic Excel export
                databaseCallback.triggerAutoExport(context)
                
                // Update local state immediately for better UX
                _uiState.update { currentState ->
                    val updatedWeekends = currentState.weekends.map { weekend ->
                        if (weekend.date == date) {
                            weekend.copy(note = note)
                        } else {
                            weekend
                        }
                    }
                    currentState.copy(weekends = updatedWeekends)
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message ?: "Failed to update note")
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun refreshWeekends() {
        loadWeekends()
    }
}

data class WeekendCalendarUiState(
    val weekends: List<WeekendCalendar> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) 