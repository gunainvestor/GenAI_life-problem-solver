package com.lifeproblemsolver.app.services

import android.content.Context
import android.util.Log
import com.lifeproblemsolver.app.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import android.os.Environment

class CsvExportService(private val context: Context) {
    companion object {
        private const val TAG = "CsvExportService"
        private const val EXPORT_DIR = "exports"
    }

    suspend fun exportAllData(onProgress: ((Int, String) -> Unit)? = null): Result<List<String>> = withContext(Dispatchers.IO) {
        val exportDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), EXPORT_DIR)
        if (!exportDir.exists()) {
            val created = exportDir.mkdirs()
            if (!created) {
                Log.e(TAG, "Failed to create export directory")
                return@withContext Result.failure(Exception("Failed to create export directory. Please check storage permissions."))
            }
        }
        if (!exportDir.canWrite()) {
            Log.e(TAG, "Export directory is not writable")
            return@withContext Result.failure(Exception("Export directory is not writable. Please check storage permissions."))
        }

        val database = AppDatabase.getDatabase(context)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val exportedFiles = mutableListOf<String>()
        try {
            // Export Problems
            onProgress?.invoke(10, "Exporting problems to CSV...")
            val problems = database.problemDao().getAllProblems().first()
            val problemsFile = File(exportDir, "problems_$timestamp.csv")
            FileWriter(problemsFile).use { writer ->
                writer.appendLine("ID,Title,Description,Category,Priority,Is Resolved,AI Solution,Created At,Updated At")
                problems.forEach { p ->
                    writer.appendLine(
                        "${p.id}," +
                        "\"${p.title?.replace("\"", "\"\"") ?: ""}\"," +
                        "\"${p.description?.replace("\"", "\"\"") ?: ""}\"," +
                        "\"${p.category?.replace("\"", "\"\"") ?: ""}\"," +
                        "${p.priority}," +
                        "${if (p.isResolved) "Yes" else "No"}," +
                        "\"${p.aiSolution?.replace("\"", "\"\"") ?: ""}\"," +
                        "${p.createdAt},${p.updatedAt}"
                    )
                }
            }
            exportedFiles.add(problemsFile.absolutePath)

            // Export Usage Stats
            onProgress?.invoke(30, "Exporting usage stats to CSV...")
            val usageStats = database.usageStatsDao().getUsageStatsByInstallationId("default").first()
            val usageStatsFile = File(exportDir, "usage_stats_$timestamp.csv")
            FileWriter(usageStatsFile).use { writer ->
                writer.appendLine("ID,Installation ID,Request Count,Last Request Time,Created At")
                usageStats?.let { s ->
                    writer.appendLine(
                        "${s.id},${s.installationId},${s.requestCount},${s.lastRequestTime},${s.createdAt}"
                    )
                }
            }
            exportedFiles.add(usageStatsFile.absolutePath)

            // Export API Keys
            onProgress?.invoke(50, "Exporting API keys to CSV...")
            val apiKeys = database.userApiKeyDao().getAllApiKeys().first()
            val apiKeysFile = File(exportDir, "api_keys_$timestamp.csv")
            FileWriter(apiKeysFile).use { writer ->
                writer.appendLine("ID,Name,API Key,Is Active,Created At,Last Used")
                apiKeys.forEach { k ->
                    writer.appendLine(
                        "${k.id}," +
                        "\"${k.name?.replace("\"", "\"\"") ?: ""}\"," +
                        "\"${k.apiKey?.replace("\"", "\"\"") ?: ""}\"," +
                        "${if (k.isActive) "Yes" else "No"}," +
                        "${k.createdAt},${k.lastUsed ?: ""}"
                    )
                }
            }
            exportedFiles.add(apiKeysFile.absolutePath)

            // Export Weekend Calendar
            onProgress?.invoke(70, "Exporting weekend calendar to CSV...")
            val weekends = database.weekendCalendarDao().getAllWeekends().first()
            val weekendsFile = File(exportDir, "weekend_calendar_$timestamp.csv")
            FileWriter(weekendsFile).use { writer ->
                writer.appendLine("ID,Date,Is Selected,Note,Created At")
                weekends.forEach { w ->
                    writer.appendLine(
                        "${w.id},${w.date},${if (w.isSelected) "Yes" else "No"},\"${w.note?.replace("\"", "\"\"") ?: ""}\",${w.createdAt}"
                    )
                }
            }
            exportedFiles.add(weekendsFile.absolutePath)

            onProgress?.invoke(100, "CSV export complete!")
            Log.d(TAG, "CSV data exported to: $exportedFiles")
            Result.success(exportedFiles)
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting CSV data", e)
            Result.failure(e)
        }
    }

    fun getExportDirectory(): String {
        val exportDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), EXPORT_DIR)
        return exportDir.absolutePath
    }

    fun getLatestExportFiles(): List<String> {
        val exportDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), EXPORT_DIR)
        if (!exportDir.exists()) return emptyList()
        return exportDir.listFiles { file -> file.isFile && file.name.endsWith(".csv") }
            ?.sortedByDescending { it.lastModified() }
            ?.map { it.absolutePath } ?: emptyList()
    }
} 