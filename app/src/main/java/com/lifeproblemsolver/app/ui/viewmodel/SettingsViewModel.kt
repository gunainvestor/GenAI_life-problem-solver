package com.lifeproblemsolver.app.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeproblemsolver.app.data.analytics.AnalyticsService
import com.lifeproblemsolver.app.data.database.AppDatabase
import com.lifeproblemsolver.app.services.CsvExportService
import com.lifeproblemsolver.app.services.ExcelExportService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.work.*
import java.io.File
import java.util.ArrayList
import java.util.concurrent.TimeUnit
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyticsService: AnalyticsService,
    private val csvExportService: CsvExportService
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
                csvExportService.exportAllData { progress, message ->
                    _uiState.update { 
                        it.copy(
                            exportMessage = message
                        ) 
                    }
                }.fold(
                    onSuccess = {
                        _uiState.update { 
                            it.copy(
                                isExporting = false,
                                exportMessage = "Data exported successfully to ${csvExportService.getExportDirectory()}"
                            ) 
                        }
                    },
                    onFailure = { exception ->
                        _uiState.update { 
                            it.copy(
                                isExporting = false,
                                exportMessage = "Export failed: ${exception.message}"
                            ) 
                        }
                    }
                )
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
                val database = AppDatabase.getDatabase(context)
                database.problemDao().deleteAllProblems()
                database.userApiKeyDao().deleteAllApiKeys()
                database.weekendCalendarDao().deleteAllWeekends()
                
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

    fun shareDatabase() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSharing = true, shareMessage = null) }
            try {
                // Export all data to CSV
                val result = csvExportService.exportAllData { progress, message ->
                    _uiState.update { 
                        it.copy(
                            shareProgress = progress,
                            shareProgressMessage = message
                        ) 
                    }
                }

                result.fold(
                    onSuccess = { exportedFiles ->
                        if (exportedFiles.isEmpty()) {
                            _uiState.update { 
                                it.copy(
                                    isSharing = false,
                                    shareMessage = "No data to export"
                                ) 
                            }
                            return@launch
                        }
                        
                        // Validate all files exist
                        val validFiles = exportedFiles.mapNotNull { filePath ->
                            val file = File(filePath)
                            if (file.exists()) file else null
                        }
                        
                        if (validFiles.isEmpty()) {
                            _uiState.update { 
                                it.copy(
                                    isSharing = false,
                                    shareMessage = "Export files not found"
                                ) 
                            }
                            return@launch
                        }
                        
                        // Create URIs for all files
                        val uris = validFiles.map { file ->
                            FileProvider.getUriForFile(
                                context,
                                context.packageName + ".fileprovider",
                                file
                            )
                        }
                        
                        // Share all CSV files using ACTION_SEND_MULTIPLE
                        val shareIntent = if (uris.size == 1) {
                            // Single file - use ACTION_SEND for better compatibility
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_SUBJECT, "Life Problem Solver Database Export")
                                putExtra(Intent.EXTRA_TEXT, "Please find attached the exported database in CSV format.")
                                putExtra(Intent.EXTRA_STREAM, uris.first())
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                        } else {
                            // Multiple files - use ACTION_SEND_MULTIPLE
                            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_SUBJECT, "Life Problem Solver Database Export")
                                putExtra(Intent.EXTRA_TEXT, "Please find attached the exported database files in CSV format (${uris.size} files).")
                                putParcelableArrayListExtra(
                                    Intent.EXTRA_STREAM,
                                    ArrayList<android.net.Uri>(uris)
                                )
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                        }
                        
                        context.startActivity(Intent.createChooser(shareIntent, "Share Database"))
                        
                        _uiState.update { 
                            it.copy(
                                isSharing = false,
                                shareMessage = "Database exported (${validFiles.size} files) and ready to share",
                                shareProgress = 100
                            ) 
                        }
                    },
                    onFailure = { exception ->
                        _uiState.update { 
                            it.copy(
                                isSharing = false,
                                shareMessage = "Failed to export: ${exception.message}"
                            ) 
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSharing = false,
                        shareMessage = "Share failed: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun clearShareMessage() {
        _uiState.update { it.copy(shareMessage = null, shareProgress = 0, shareProgressMessage = null) }
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
    val isSharing: Boolean = false,
    val exportMessage: String? = null,
    val clearMessage: String? = null,
    val shareMessage: String? = null,
    val shareProgress: Int = 0,
    val shareProgressMessage: String? = null
) 