package com.lifeproblemsolver.app.data.dao

import androidx.room.*
import com.lifeproblemsolver.app.data.model.UsageStats
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface UsageStatsDao {
    
    @Query("SELECT * FROM usage_stats WHERE installationId = :installationId LIMIT 1")
    fun getUsageStatsByInstallationId(installationId: String): Flow<UsageStats?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageStats(usageStats: UsageStats)
    
    @Update
    suspend fun updateUsageStats(usageStats: UsageStats)
    
    @Query("UPDATE usage_stats SET requestCount = requestCount + 1, lastRequestTime = :currentTime WHERE installationId = :installationId")
    suspend fun incrementRequestCount(installationId: String, currentTime: LocalDateTime)
    
    @Query("SELECT COALESCE(requestCount, 0) FROM usage_stats WHERE installationId = :installationId")
    suspend fun getRequestCount(installationId: String): Int
} 