package com.lifeproblemsolver.app.data.repository

import android.content.Context
import android.provider.Settings
import com.lifeproblemsolver.app.data.dao.UsageStatsDao
import com.lifeproblemsolver.app.data.dao.UserApiKeyDao
import com.lifeproblemsolver.app.data.model.UsageStats
import com.lifeproblemsolver.app.data.model.UserApiKey
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class UsageRepositoryTest {
    
    private lateinit var usageRepository: UsageRepository
    private lateinit var usageStatsDao: UsageStatsDao
    private lateinit var userApiKeyDao: UserApiKeyDao
    private lateinit var context: Context
    private lateinit var contentResolver: android.content.ContentResolver
    
    @Before
    fun setup() {
        usageStatsDao = mockk(relaxed = true)
        userApiKeyDao = mockk(relaxed = true)
        context = mockk(relaxed = true)
        contentResolver = mockk(relaxed = true)
        
        every { context.contentResolver } returns contentResolver
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns mockk(relaxed = true)
        
        usageRepository = UsageRepository(usageStatsDao, userApiKeyDao, context)
    }
    
    @Test
    fun `getInstallationId returns android id`() {
        // Given
        val expectedId = "test_android_id"
        every { Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) } returns expectedId
        
        // When
        val result = usageRepository.getInstallationId()
        
        // Then
        assertEquals(expectedId, result)
    }
    
    @Test
    fun `getInstallationId returns unknown when android id is null`() {
        // Given
        every { Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) } returns null
        
        // When
        val result = usageRepository.getInstallationId()
        
        // Then
        assertEquals("unknown", result)
    }
    
    @Test
    fun `hasReachedLimit returns true when request count exceeds limit`() = runTest {
        // Given
        val installationId = "test_id"
        every { Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) } returns installationId
        coEvery { usageStatsDao.getRequestCount(installationId) } returns UsageRepository.MAX_REQUESTS_WITH_PREDEFINED_KEY
        
        // When
        val result = usageRepository.hasReachedLimit()
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `hasReachedLimit returns false when request count is below limit`() = runTest {
        // Given
        val installationId = "test_id"
        every { Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) } returns installationId
        coEvery { usageStatsDao.getRequestCount(installationId) } returns 5
        
        // When
        val result = usageRepository.hasReachedLimit()
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `hasUserApiKey returns true when active key exists`() = runTest {
        // Given
        coEvery { userApiKeyDao.hasActiveApiKey() } returns 1
        
        // When
        val result = usageRepository.hasUserApiKey()
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `hasUserApiKey returns false when no active key exists`() = runTest {
        // Given
        coEvery { userApiKeyDao.hasActiveApiKey() } returns 0
        
        // When
        val result = usageRepository.hasUserApiKey()
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `saveUserApiKey deactivates other keys and saves new key`() = runTest {
        // Given
        val apiKey = "test_api_key"
        
        // When
        usageRepository.saveUserApiKey(apiKey)
        
        // Then
        coVerify { userApiKeyDao.deactivateOtherKeys() }
        coVerify { 
            userApiKeyDao.insertApiKey(match { 
                it.apiKey == apiKey && it.isActive 
            }) 
        }
    }
    
    @Test
    fun `getUserApiKey returns active key`() = runTest {
        // Given
        val expectedKey = UserApiKey(apiKey = "test_key", isActive = true)
        coEvery { userApiKeyDao.getActiveApiKey() } returns flowOf(expectedKey)
        
        // When
        val result = usageRepository.getUserApiKey().first()
        
        // Then
        assertEquals(expectedKey, result)
    }
    
    @Test
    fun `incrementRequestCount creates new stats when none exist`() = runTest {
        // Given
        val installationId = "test_id"
        every { Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) } returns installationId
        coEvery { usageStatsDao.getUsageStatsByInstallationId(installationId) } returns flowOf(null)
        
        // When
        usageRepository.incrementRequestCount()
        
        // Then
        coVerify { 
            usageStatsDao.insertUsageStats(match { 
                it.installationId == installationId && it.requestCount == 1 
            }) 
        }
    }
    
    @Test
    fun `incrementRequestCount updates existing stats when they exist`() = runTest {
        // Given
        val installationId = "test_id"
        val currentTime = LocalDateTime.now().toString()
        every { Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) } returns installationId
        coEvery { usageStatsDao.getUsageStatsByInstallationId(installationId) } returns flowOf(
            UsageStats(installationId = installationId, requestCount = 5)
        )
        
        // When
        usageRepository.incrementRequestCount()
        
        // Then
        coVerify { usageStatsDao.incrementRequestCount(installationId, any()) }
    }
} 