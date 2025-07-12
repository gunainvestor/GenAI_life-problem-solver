package com.lifeproblemsolver.app.data.repository

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.lifeproblemsolver.app.data.dao.UsageStatsDao
import com.lifeproblemsolver.app.data.dao.UserApiKeyDao
import com.lifeproblemsolver.app.data.model.UsageStats
import com.lifeproblemsolver.app.data.model.UserApiKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
    private val TAG = "UsageRepository"
    
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
        
        Log.d(TAG, "Incrementing request count for installation: $installationId")
        
        // Check if usage stats exist
        val existingStats = usageStatsDao.getUsageStatsByInstallationId(installationId).first()
        
        // If no stats exist, create new one
        if (existingStats == null) {
            Log.d(TAG, "Creating new usage stats for installation: $installationId")
            val newStats = UsageStats(
                installationId = installationId,
                requestCount = 1,
                lastRequestTime = currentTime
            )
            usageStatsDao.insertUsageStats(newStats)
        } else {
            Log.d(TAG, "Incrementing existing usage stats. Current count: ${existingStats.requestCount}")
            // Increment existing count
            usageStatsDao.incrementRequestCount(installationId, currentTime)
        }
    }
    
    suspend fun getCurrentRequestCount(): Int {
        val installationId = getInstallationId()
        val count = usageStatsDao.getRequestCount(installationId)
        Log.d(TAG, "Current request count for installation $installationId: $count")
        return count
    }
    
    suspend fun hasReachedLimit(): Boolean {
        val currentCount = getCurrentRequestCount()
        val hasReached = currentCount >= MAX_REQUESTS_WITH_PREDEFINED_KEY
        Log.d(TAG, "Has reached limit check: $currentCount >= $MAX_REQUESTS_WITH_PREDEFINED_KEY = $hasReached")
        return hasReached
    }
    
    suspend fun hasUserApiKey(): Boolean {
        val hasKey = userApiKeyDao.hasActiveApiKey() > 0
        Log.d(TAG, "Has user API key: $hasKey")
        return hasKey
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
        Log.d(TAG, "User API key saved successfully")
    }
    
    suspend fun getUserApiKey(): Flow<UserApiKey?> {
        return userApiKeyDao.getActiveApiKey()
    }
    
    suspend fun updateLastUsed() {
        val currentTime = LocalDateTime.now()
        userApiKeyDao.updateLastUsed("default", currentTime)
        Log.d(TAG, "Updated last used time for user API key")
    }
    
    suspend fun deleteUserApiKey() {
        userApiKeyDao.deleteApiKey("default")
        Log.d(TAG, "User API key deleted")
    }
    
    companion object {
        const val MAX_REQUESTS_WITH_PREDEFINED_KEY = 5
    }
} 