package com.lifeproblemsolver.app.data.backup

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lifeproblemsolver.app.data.dao.ProblemDao
import com.lifeproblemsolver.app.data.dao.UserApiKeyDao
import com.lifeproblemsolver.app.data.dao.WeekendCalendarDao
import com.lifeproblemsolver.app.data.model.Problem
import com.lifeproblemsolver.app.data.model.UserApiKey
import com.lifeproblemsolver.app.data.model.WeekendCalendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataBackupService @Inject constructor(
    private val problemDao: ProblemDao,
    private val userApiKeyDao: UserApiKeyDao,
    private val weekendCalendarDao: WeekendCalendarDao,
    private val context: Context
) {
    
    data class BackupData(
        val problems: List<Problem>,
        val apiKeys: List<UserApiKey>,
        val weekendCalendar: List<WeekendCalendar>,
        val backupDate: String = LocalDateTime.now().toString(),
        val appVersion: String = "1.2"
    )
    
    suspend fun createBackup(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val problems = problemDao.getAllProblems().first()
            val apiKeys = userApiKeyDao.getAllApiKeys().first()
            val weekendCalendar = weekendCalendarDao.getAllWeekends().first()
            
            val backupData = BackupData(
                problems = problems,
                apiKeys = apiKeys,
                weekendCalendar = weekendCalendar
            )
            
            val gson = Gson()
            val jsonData = gson.toJson(backupData)
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(jsonData)
                }
            }
            
            Result.success("Backup created successfully with ${problems.size} problems, ${apiKeys.size} API keys, and ${weekendCalendar.size} weekend entries")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun restoreBackup(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val jsonData = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                ?: throw Exception("Could not read backup file")
            
            val gson = Gson()
            val backupData = gson.fromJson<BackupData>(jsonData, object : TypeToken<BackupData>() {}.type)
            
            // Clear existing data
            problemDao.deleteAllProblems()
            userApiKeyDao.deleteAllApiKeys()
            weekendCalendarDao.deleteAllWeekends()
            
            // Restore data
            backupData.problems.forEach { problem ->
                problemDao.insertProblem(problem)
            }
            
            backupData.apiKeys.forEach { apiKey ->
                userApiKeyDao.insertApiKey(apiKey)
            }
            
            backupData.weekendCalendar.forEach { weekend ->
                weekendCalendarDao.insertWeekend(weekend)
            }
            
            Result.success("Backup restored successfully: ${backupData.problems.size} problems, ${backupData.apiKeys.size} API keys, and ${backupData.weekendCalendar.size} weekend entries restored")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getBackupInfo(): String {
        val problems = problemDao.getAllProblems().first()
        val apiKeys = userApiKeyDao.getAllApiKeys().first()
        val weekendCalendar = weekendCalendarDao.getAllWeekends().first()
        
        return """
            📊 Data Summary:
            • Problems: ${problems.size}
            • API Keys: ${apiKeys.size}
            • Weekend Calendar Entries: ${weekendCalendar.size}
            • Last Backup: ${LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}
        """.trimIndent()
    }
} 