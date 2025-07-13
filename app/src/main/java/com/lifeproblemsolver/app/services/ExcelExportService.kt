package com.lifeproblemsolver.app.services

import android.content.Context
import android.util.Log
import com.lifeproblemsolver.app.data.model.Problem
import com.lifeproblemsolver.app.data.model.UsageStats
import com.lifeproblemsolver.app.data.model.UserApiKey
import com.lifeproblemsolver.app.data.model.WeekendCalendar
import com.lifeproblemsolver.app.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.ss.usermodel.CellType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ExcelExportService(private val context: Context) {
    
    companion object {
        private const val TAG = "ExcelExportService"
        private const val EXPORT_DIR = "exports"
        private const val CHUNK_SIZE = 50 // Reduced chunk size for better memory management
        private const val MAX_ROWS_PER_SHEET = 5000 // Reduced limit to prevent OOM
    }
    
    suspend fun exportAllData(onProgress: ((Int, String) -> Unit)? = null): Result<String> = withContext(Dispatchers.IO) {
        var workbook: XSSFWorkbook? = null
        var fileOutputStream: FileOutputStream? = null
        
        try {
            Log.d(TAG, "Starting data export...")
            val database = AppDatabase.getDatabase(context)
            
            // Check available memory before starting
            val runtime = Runtime.getRuntime()
            val availableMemory = runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory()
            Log.d(TAG, "Available memory before export: ${availableMemory / 1024 / 1024} MB")
            
            if (availableMemory < 100 * 1024 * 1024) { // Increased to 100MB minimum
                Log.w(TAG, "Low memory detected, export may fail")
                return@withContext Result.failure(Exception("Insufficient memory for export. Please close other apps and try again."))
            }
            
            // Create workbook
            workbook = XSSFWorkbook()
            
            // Export problems in chunks with progress
            Log.d(TAG, "Exporting problems...")
            val problemsResult = exportProblemsInChunks(workbook, database, onProgress)
            if (problemsResult.isFailure) {
                return@withContext Result.failure(problemsResult.exceptionOrNull() ?: Exception("Failed to export problems"))
            }
            
            onProgress?.invoke(40, "Exporting usage stats...")
            try {
                val usageStats = database.usageStatsDao().getUsageStatsByInstallationId("default").first()
                usageStats?.let { stats ->
                    exportUsageStats(workbook, listOf(stats))
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to export usage stats, continuing...", e)
            }
            
            onProgress?.invoke(60, "Exporting API keys...")
            try {
                val userApiKeys = database.userApiKeyDao().getAllApiKeys().first()
                exportUserApiKeys(workbook, userApiKeys)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to export API keys, continuing...", e)
            }
            
            onProgress?.invoke(80, "Exporting weekend calendar...")
            try {
                val weekendCalendar = database.weekendCalendarDao().getAllWeekends().first()
                exportWeekendCalendar(workbook, weekendCalendar)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to export weekend calendar, continuing...", e)
            }
            
            // Create export directory
            val exportDir = File(context.getExternalFilesDir(null), EXPORT_DIR)
            if (!exportDir.exists()) {
                val created = exportDir.mkdirs()
                if (!created) {
                    Log.e(TAG, "Failed to create export directory")
                    return@withContext Result.failure(Exception("Failed to create export directory. Please check storage permissions."))
                }
            }
            
            // Check if directory is writable
            if (!exportDir.canWrite()) {
                Log.e(TAG, "Export directory is not writable")
                return@withContext Result.failure(Exception("Export directory is not writable. Please check storage permissions."))
            }
            
            // Generate filename with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "life_problem_solver_export_$timestamp.xlsx"
            val file = File(exportDir, filename)
            
            // Write to file
            fileOutputStream = FileOutputStream(file)
            workbook.write(fileOutputStream)
            fileOutputStream.flush()
            
            onProgress?.invoke(100, "Export complete!")
            Log.d(TAG, "Data exported successfully to: ${file.absolutePath}")
            Log.d(TAG, "File size: ${file.length() / 1024} KB")
            
            Result.success(file.absolutePath)
            
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Out of memory during export", e)
            Result.failure(Exception("Export failed due to insufficient memory. Please try with fewer problems or close other apps."))
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting data", e)
            Result.failure(e)
        } finally {
            try {
                fileOutputStream?.close()
                workbook?.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing resources", e)
            }
        }
    }
    
    private suspend fun exportProblemsInChunks(
        workbook: XSSFWorkbook,
        database: AppDatabase,
        onProgress: ((Int, String) -> Unit)? = null
    ): Result<Unit> {
        return try {
            val sheet = workbook.createSheet("Problems")
            
            // Create header row
            val headerRow = sheet.createRow(0)
            val headers = listOf("ID", "Title", "Description", "Category", "Priority", "Is Resolved", "AI Solution", "Created At", "Updated At")
            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
            }
            
            // Get total count first
            val totalProblems = try {
                database.problemDao().getAllProblems().first().size
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get total problems count", e)
                return Result.failure(Exception("Failed to access problem data"))
            }
            
            Log.d(TAG, "Total problems to export: $totalProblems")
            
            if (totalProblems > MAX_ROWS_PER_SHEET) {
                Log.w(TAG, "Large dataset detected: $totalProblems problems. This may cause memory issues.")
                return Result.failure(Exception("Too many problems to export. Please delete some problems and try again."))
            }
            
            // Process in chunks to reduce memory usage
            var currentRow = 1
            var offset = 0
            var chunkIndex = 0
            
            while (offset < totalProblems) {
                try {
                    val chunk = database.problemDao().getProblemsPaged(CHUNK_SIZE, offset)
                    Log.d(TAG, "Processing chunk ${chunkIndex + 1}: ${chunk.size} problems")
                    
                    chunk.forEach { problem ->
                        try {
                            val row = sheet.createRow(currentRow++)
                            
                            // Use safe cell creation to prevent crashes
                            row.createCell(0).setCellValue(problem.id.toDouble())
                            row.createCell(1).setCellValue(problem.title ?: "")
                            row.createCell(2).setCellValue(problem.description ?: "")
                            row.createCell(3).setCellValue(problem.category ?: "")
                            row.createCell(4).setCellValue(problem.priority.toString())
                            row.createCell(5).setCellValue(if (problem.isResolved) "Yes" else "No")
                            row.createCell(6).setCellValue(problem.aiSolution ?: "")
                            row.createCell(7).setCellValue(problem.createdAt.toString())
                            row.createCell(8).setCellValue(problem.updatedAt.toString())
                        } catch (e: Exception) {
                            Log.e(TAG, "Error creating row for problem ${problem.id}", e)
                            // Continue with next row instead of failing completely
                        }
                    }
                    
                    offset += CHUNK_SIZE
                    chunkIndex++
                    
                    val progress = if (totalProblems == 0) 0 else (offset * 40 / totalProblems).coerceAtMost(40)
                    onProgress?.invoke(progress, "Exported $offset of $totalProblems problems...")
                    
                    // Force garbage collection between chunks to free memory
                    System.gc()
                    
                    // Small delay to prevent UI blocking
                    kotlinx.coroutines.delay(10)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing chunk $chunkIndex", e)
                    // Continue with next chunk instead of failing completely
                    offset += CHUNK_SIZE
                    chunkIndex++
                }
            }
            
            // Auto-size columns (but limit to prevent memory issues)
            try {
                (0..8).forEach { 
                    if (it < 6) { // Only auto-size first 6 columns to save memory
                        sheet.autoSizeColumn(it)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not auto-size columns", e)
            }
            
            Log.d(TAG, "Problems exported successfully: $totalProblems rows")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting problems", e)
            Result.failure(e)
        }
    }
    
    private fun exportUsageStats(workbook: XSSFWorkbook, usageStats: List<UsageStats>) {
        try {
            val sheet = workbook.createSheet("Usage Statistics")
            
            // Create header row
            val headerRow = sheet.createRow(0)
            val headers = listOf("ID", "Installation ID", "Request Count", "Last Request Time", "Created At")
            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
            }
            
            // Add data rows
            usageStats.forEachIndexed { index, stats ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(stats.id.toDouble())
                row.createCell(1).setCellValue(stats.installationId ?: "")
                row.createCell(2).setCellValue(stats.requestCount.toDouble())
                row.createCell(3).setCellValue(stats.lastRequestTime?.toString() ?: "")
                row.createCell(4).setCellValue(stats.createdAt.toString())
            }
            
            // Auto-size columns
            (0..4).forEach { sheet.autoSizeColumn(it) }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting usage stats", e)
        }
    }
    
    private fun exportUserApiKeys(workbook: XSSFWorkbook, userApiKeys: List<UserApiKey>) {
        try {
            val sheet = workbook.createSheet("API Keys")
            
            // Create header row
            val headerRow = sheet.createRow(0)
            val headers = listOf("ID", "Name", "API Key", "Is Active", "Created At", "Last Used")
            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
            }
            
            // Add data rows
            userApiKeys.forEachIndexed { index, apiKey ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(apiKey.id.toDouble())
                row.createCell(1).setCellValue(apiKey.name ?: "")
                row.createCell(2).setCellValue(apiKey.apiKey ?: "")
                row.createCell(3).setCellValue(if (apiKey.isActive) "Yes" else "No")
                row.createCell(4).setCellValue(apiKey.createdAt.toString())
                row.createCell(5).setCellValue(apiKey.lastUsed?.toString() ?: "")
            }
            
            // Auto-size columns
            (0..5).forEach { sheet.autoSizeColumn(it) }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting API keys", e)
        }
    }
    
    private fun exportWeekendCalendar(workbook: XSSFWorkbook, weekendCalendar: List<WeekendCalendar>) {
        try {
            val sheet = workbook.createSheet("Weekend Calendar")
            
            // Create header row
            val headerRow = sheet.createRow(0)
            val headers = listOf("ID", "Date", "Is Selected", "Note", "Created At")
            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
            }
            
            // Add data rows
            weekendCalendar.forEachIndexed { index, calendar ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(calendar.id.toDouble())
                row.createCell(1).setCellValue(calendar.date.toString())
                row.createCell(2).setCellValue(if (calendar.isSelected) "Yes" else "No")
                row.createCell(3).setCellValue(calendar.note ?: "")
                row.createCell(4).setCellValue(calendar.createdAt.toString())
            }
            
            // Auto-size columns
            (0..4).forEach { sheet.autoSizeColumn(it) }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting weekend calendar", e)
        }
    }
    
    fun getExportDirectory(): String {
        val exportDir = File(context.getExternalFilesDir(null), EXPORT_DIR)
        return exportDir.absolutePath
    }
    
    fun getLatestExportFile(): String? {
        val exportDir = File(context.getExternalFilesDir(null), EXPORT_DIR)
        if (!exportDir.exists()) return null
        
        val files = exportDir.listFiles { file -> 
            file.isFile && file.name.endsWith(".xlsx") 
        }
        
        return files?.maxByOrNull { it.lastModified() }?.absolutePath
    }
} 