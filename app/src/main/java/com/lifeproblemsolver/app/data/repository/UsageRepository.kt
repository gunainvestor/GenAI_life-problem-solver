package com.lifeproblemsolver.app.data.repository

import android.content.Context
import android.provider.Settings
import com.lifeproblemsolver.app.data.dao.UsageStatsDao
import com.lifeproblemsolver.app.data.dao.UserApiKeyDao
import com.lifeproblemsolver.app.data.model.UsageStats
import com.lifeproblemsolver.app.data.model.UserApiKey
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageRepository @Inject constructor(
    private val usageStatsDao: UsageStatsDao,
    private val userApiKeyDao: UserApiKeyDao,
    private val context: Context
) {
    
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    fun getInstallationId(): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown"
    }
    
    suspend fun getUsageStats(): Flow<UsageStats?> {
        val installationId = getInstallationId()
        return usageStatsDao.getUsageStatsByInstallationId(installationId)
    }
    
    suspend fun incrementRequestCount() {
        val installationId = getInstallationId()
        val currentTime = LocalDateTime.now()
        
        // Check if usage stats exist
        val existingStats = usageStatsDao.getUsageStatsByInstallationId(installationId)
        
        // If no stats exist, create new one
        if (existingStats == null) {
            val newStats = UsageStats(
                installationId = installationId,
                requestCount = 1,
                lastRequestTime = currentTime
            )
            usageStatsDao.insertUsageStats(newStats)
        } else {
            // Increment existing count
            usageStatsDao.incrementRequestCount(installationId, currentTime)
        }
    }
    
    suspend fun getCurrentRequestCount(): Int {
        val installationId = getInstallationId()
        return usageStatsDao.getRequestCount(installationId)
    }
    
    suspend fun hasReachedLimit(): Boolean {
        val currentCount = getCurrentRequestCount()
        return currentCount >= MAX_REQUESTS_WITH_PREDEFINED_KEY
    }
    
    suspend fun hasUserApiKey(): Boolean {
        return userApiKeyDao.hasActiveApiKey() > 0
    }
    
    suspend fun saveUserApiKey(apiKey: String) {
        val userApiKey = UserApiKey(
            apiKey = apiKey,
            isActive = true
        )
        
        // Deactivate other keys
        userApiKeyDao.deactivateOtherKeys()
        
        // Insert new key
        userApiKeyDao.insertApiKey(userApiKey)
    }
    
    suspend fun getUserApiKey(): Flow<UserApiKey?> {
        return userApiKeyDao.getActiveApiKey()
    }
    
    suspend fun updateLastUsed() {
        val currentTime = LocalDateTime.now()
        userApiKeyDao.updateLastUsed("default", currentTime)
    }
    
    suspend fun deleteUserApiKey() {
        userApiKeyDao.deleteApiKey("default")
    }
    
    companion object {
        const val MAX_REQUESTS_WITH_PREDEFINED_KEY = 5
    }
} 