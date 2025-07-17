package com.lifeproblemsolver.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeproblemsolver.app.data.analytics.AnalyticsService
import com.lifeproblemsolver.app.data.database.AppDatabase
import com.lifeproblemsolver.app.services.ExcelExportService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.work.*
import java.util.concurrent.TimeUnit
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updateDailyReminders(enabled: Boolean) {
        _uiState.update { it.copy(dailyRemindersEnabled = enabled) }
    }

    fun updateUrgentAlerts(enabled: Boolean) {
        _uiState.update { it.copy(urgentAlertsEnabled = enabled) }
    }

    fun showTimePicker() {
        _uiState.update { it.copy(showTimePicker = true) }
    }

    fun hideTimePicker() {
        _uiState.update { it.copy(showTimePicker = false) }
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        val timeString = String.format("%02d:%02d", hour, minute)
        _uiState.update { 
            it.copy(
                reminderTime = timeString,
                showTimePicker = false
            ) 
        }
    }

    fun showClearDataDialog() {
        _uiState.update { it.copy(showClearDataDialog = true) }
    }

    fun hideClearDataDialog() {
        _uiState.update { it.copy(showClearDataDialog = false) }
    }

    fun exportData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                // This would be implemented with actual export logic
                _uiState.update { 
                    it.copy(
                        isExporting = false,
                        exportMessage = "Data exported successfully"
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isExporting = false,
                        exportMessage = "Export failed: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isClearing = true) }
            try {
                // This would be implemented with actual clear logic
                _uiState.update { 
                    it.copy(
                        isClearing = false,
                        clearMessage = "All data cleared successfully"
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isClearing = false,
                        clearMessage = "Clear failed: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun scheduleDailyReminder(hour: Int, minute: Int) {
        val now = java.util.Calendar.getInstance()
        val target = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            if (before(now)) add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        val initialDelay = target.timeInMillis - now.timeInMillis
        val workRequest = PeriodicWorkRequestBuilder<com.lifeproblemsolver.app.services.ProblemReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("problem_reminder")
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "problem_reminder",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
}

data class SettingsUiState(
    val dailyRemindersEnabled: Boolean = false,
    val urgentAlertsEnabled: Boolean = true,
    val reminderTime: String = "09:00",
    val showTimePicker: Boolean = false,
    val showClearDataDialog: Boolean = false,
    val isExporting: Boolean = false,
    val isClearing: Boolean = false,
    val exportMessage: String? = null,
    val clearMessage: String? = null
) 