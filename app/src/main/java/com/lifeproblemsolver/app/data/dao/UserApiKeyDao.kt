package com.lifeproblemsolver.app.data.dao

import androidx.room.*
import com.lifeproblemsolver.app.data.model.UserApiKey
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface UserApiKeyDao {
    
    @Query("SELECT * FROM user_api_keys WHERE isActive = 1 LIMIT 1")
    fun getActiveApiKey(): Flow<UserApiKey?>
    
    @Query("SELECT * FROM user_api_keys ORDER BY createdAt DESC")
    fun getAllApiKeys(): Flow<List<UserApiKey>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiKey(userApiKey: UserApiKey)
    
    @Update
    suspend fun updateApiKey(userApiKey: UserApiKey)
    
    @Query("UPDATE user_api_keys SET isActive = 0 WHERE id != :excludeId")
    suspend fun deactivateOtherKeys(excludeId: Long)
    
    @Query("UPDATE user_api_keys SET lastUsed = :currentTime WHERE id = :keyId")
    suspend fun updateLastUsed(keyId: Long, currentTime: LocalDateTime)
    
    @Query("DELETE FROM user_api_keys WHERE id = :keyId")
    suspend fun deleteApiKey(keyId: Long)
    
    @Query("SELECT COUNT(*) FROM user_api_keys WHERE isActive = 1")
    suspend fun hasActiveApiKey(): Int
    
    @Query("SELECT COUNT(*) FROM user_api_keys")
    suspend fun getApiKeyCount(): Int
} 