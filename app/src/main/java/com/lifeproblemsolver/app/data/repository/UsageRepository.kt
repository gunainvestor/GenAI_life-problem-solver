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
        saveUserApiKey(apiKey, "API Key")
    }
    
    suspend fun saveUserApiKey(apiKey: String, name: String) {
        val userApiKey = UserApiKey(
            name = name,
            apiKey = apiKey,
            isActive = true
        )
        
        // Insert new key first to get the ID
        userApiKeyDao.insertApiKey(userApiKey)
        
        // Get the inserted key to get its ID
        val insertedKey = userApiKeyDao.getAllApiKeys().first().firstOrNull { it.apiKey == apiKey }
        
        if (insertedKey != null) {
            // Deactivate other keys
            userApiKeyDao.deactivateOtherKeys(insertedKey.id)
            Log.d(TAG, "User API key saved successfully with name: $name")
        } else {
            Log.e(TAG, "Failed to retrieve inserted API key")
        }
    }
    
    suspend fun getAllApiKeys(): Flow<List<UserApiKey>> {
        return userApiKeyDao.getAllApiKeys()
    }
    
    suspend fun deleteApiKey(keyId: Long) {
        userApiKeyDao.deleteApiKey(keyId)
        Log.d(TAG, "API key with ID $keyId deleted")
    }
    
    suspend fun setActiveApiKey(keyId: Long) {
        // Deactivate all keys first
        userApiKeyDao.deactivateOtherKeys(keyId)
        
        // Get the key and activate it
        val keys = userApiKeyDao.getAllApiKeys().first()
        val keyToActivate = keys.find { it.id == keyId }
        keyToActivate?.let {
            val updatedKey = it.copy(isActive = true)
            userApiKeyDao.updateApiKey(updatedKey)
            Log.d(TAG, "API key with ID $keyId set as active")
        }
    }
    
    suspend fun getUserApiKey(): Flow<UserApiKey?> {
        return userApiKeyDao.getActiveApiKey()
    }
    
    suspend fun updateLastUsed() {
        val currentTime = LocalDateTime.now()
        val activeKey = userApiKeyDao.getActiveApiKey().first()
        activeKey?.let {
            userApiKeyDao.updateLastUsed(it.id, currentTime)
            Log.d(TAG, "Updated last used time for user API key")
        }
    }
    
    suspend fun deleteUserApiKey() {
        val activeKey = userApiKeyDao.getActiveApiKey().first()
        activeKey?.let {
            userApiKeyDao.deleteApiKey(it.id)
            Log.d(TAG, "Active user API key deleted")
        }
    }
    
    companion object {
        const val MAX_REQUESTS_WITH_PREDEFINED_KEY = 5
    }
} 