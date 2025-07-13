package com.lifeproblemsolver.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeproblemsolver.app.services.ExcelExportService
import com.lifeproblemsolver.app.services.CsvExportService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExcelExportViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(ExcelExportUiState())
    val uiState: StateFlow<ExcelExportUiState> = _uiState.asStateFlow()
    
    fun exportData(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                error = null,
                progress = 0,
                progressMessage = "Starting export..."
            )
            val exportService = ExcelExportService(context)
            try {
                exportService.exportAllData { progress, message ->
                    _uiState.value = _uiState.value.copy(
                        progress = progress,
                        progressMessage = message
                    )
                }
                .onSuccess { filePath ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        exportPath = filePath,
                        exportDirectory = exportService.getExportDirectory(),
                        latestExportFile = exportService.getLatestExportFile(),
                        progress = 100,
                        progressMessage = "Export completed successfully!"
                    )
                }
                .onFailure { exception ->
                    val errorMessage = when {
                        exception.message?.contains("memory", ignoreCase = true) == true -> 
                            "Export failed due to insufficient memory. Please close other apps and try again."
                        exception.message?.contains("permission", ignoreCase = true) == true -> 
                            "Export failed due to storage permission issues. Please check app permissions."
                        else -> exception.message ?: "Export failed with an unknown error"
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMessage,
                        progress = 0,
                        progressMessage = "Export failed"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Export failed: ${e.message}",
                    progress = 0,
                    progressMessage = "Export failed"
                )
            }
        }
    }

    fun exportCsvData(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCsvLoading = true,
                csvError = null,
                csvProgress = 0,
                csvProgressMessage = "Starting CSV export..."
            )
            val exportService = CsvExportService(context)
            try {
                exportService.exportAllData { progress, message ->
                    _uiState.value = _uiState.value.copy(
                        csvProgress = progress,
                        csvProgressMessage = message
                    )
                }
                .onSuccess { filePaths ->
                    _uiState.value = _uiState.value.copy(
                        isCsvLoading = false,
                        csvExportPaths = filePaths,
                        csvExportDirectory = exportService.getExportDirectory(),
                        csvLatestExportFiles = exportService.getLatestExportFiles(),
                        csvProgress = 100,
                        csvProgressMessage = "CSV export completed successfully!"
                    )
                }
                .onFailure { exception ->
                    val errorMessage = exception.message ?: "CSV export failed with an unknown error"
                    _uiState.value = _uiState.value.copy(
                        isCsvLoading = false,
                        csvError = errorMessage,
                        csvProgress = 0,
                        csvProgressMessage = "CSV export failed"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCsvLoading = false,
                    csvError = "CSV export failed: ${e.message}",
                    csvProgress = 0,
                    csvProgressMessage = "CSV export failed"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null, csvError = null)
    }
    
    fun updateExportDirectory(context: Context) {
        val exportService = ExcelExportService(context)
        _uiState.value = _uiState.value.copy(
            exportDirectory = exportService.getExportDirectory(),
            latestExportFile = exportService.getLatestExportFile()
        )
        val csvExportService = CsvExportService(context)
        _uiState.value = _uiState.value.copy(
            csvExportDirectory = csvExportService.getExportDirectory(),
            csvLatestExportFiles = csvExportService.getLatestExportFiles()
        )
    }
    
    fun resetProgress() {
        _uiState.value = _uiState.value.copy(
            progress = 0,
            progressMessage = null,
            csvProgress = 0,
            csvProgressMessage = null
        )
    }
}

data class ExcelExportUiState(
    val isLoading: Boolean = false,
    val exportPath: String? = null,
    val exportDirectory: String? = null,
    val latestExportFile: String? = null,
    val error: String? = null,
    val progress: Int = 0,
    val progressMessage: String? = null,
    // CSV export state
    val isCsvLoading: Boolean = false,
    val csvExportPaths: List<String>? = null,
    val csvExportDirectory: String? = null,
    val csvLatestExportFiles: List<String>? = null,
    val csvError: String? = null,
    val csvProgress: Int = 0,
    val csvProgressMessage: String? = null
) 