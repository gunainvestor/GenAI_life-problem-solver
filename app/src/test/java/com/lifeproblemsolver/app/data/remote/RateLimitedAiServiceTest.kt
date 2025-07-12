package com.lifeproblemsolver.app.data.remote

import com.lifeproblemsolver.app.data.repository.UsageRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RateLimitedAiServiceTest {
    
    private lateinit var rateLimitedAiService: RateLimitedAiService
    private lateinit var usageRepository: UsageRepository
    
    @Before
    fun setup() {
        usageRepository = mockk(relaxed = true)
        rateLimitedAiService = RateLimitedAiService(usageRepository)
    }
    
    @Test
    fun `generateSolution uses user API key when available`() = runTest {
        // Given
        val userApiKey = "user_api_key"
        val problem = "test problem"
        val expectedSolution = "AI solution"
        
        coEvery { usageRepository.getUserApiKey() } returns flowOf(
            mockk { every { apiKey } returns userApiKey }
        )
        coEvery { usageRepository.updateLastUsed() } returns Unit
        
        // When
        val result = rateLimitedAiService.generateSolution(problem)
        
        // Then
        coVerify { usageRepository.updateLastUsed() }
        coVerify(exactly = 0) { usageRepository.incrementRequestCount() }
    }
    
    @Test
    fun `generateSolution uses predefined key when no user key available`() = runTest {
        // Given
        val predefinedKey = "predefined_key"
        val problem = "test problem"
        
        coEvery { usageRepository.getUserApiKey() } returns flowOf(null)
        coEvery { usageRepository.hasReachedLimit() } returns false
        coEvery { usageRepository.incrementRequestCount() } returns Unit
        
        // Mock BuildConfig
        mockkStatic("com.lifeproblemsolver.app.BuildConfig")
        every { any<Class<*>>().getField("OPENAI_API_KEY").get(null) } returns predefinedKey
        
        // When
        try {
            rateLimitedAiService.generateSolution(problem)
        } catch (e: Exception) {
            // Expected since we can't mock the actual API call
        }
        
        // Then
        coVerify { usageRepository.incrementRequestCount() }
        coVerify(exactly = 0) { usageRepository.updateLastUsed() }
    }
    
    @Test
    fun `generateSolution throws RateLimitExceededException when limit reached`() = runTest {
        // Given
        val problem = "test problem"
        
        coEvery { usageRepository.getUserApiKey() } returns flowOf(null)
        coEvery { usageRepository.hasReachedLimit() } returns true
        
        // Mock BuildConfig
        mockkStatic("com.lifeproblemsolver.app.BuildConfig")
        every { any<Class<*>>().getField("OPENAI_API_KEY").get(null) } returns "predefined_key"
        
        // When & Then
        val exception = assertThrows(RateLimitExceededException::class.java) {
            runTest {
                rateLimitedAiService.generateSolution(problem)
            }
        }
        
        assertTrue(exception.message?.contains("5") == true)
    }
    
    @Test
    fun `generateSolution throws exception when no API key available`() = runTest {
        // Given
        val problem = "test problem"
        
        coEvery { usageRepository.getUserApiKey() } returns flowOf(null)
        
        // Mock BuildConfig to return empty string
        mockkStatic("com.lifeproblemsolver.app.BuildConfig")
        every { any<Class<*>>().getField("OPENAI_API_KEY").get(null) } returns ""
        
        // When & Then
        val exception = assertThrows(IllegalStateException::class.java) {
            runTest {
                rateLimitedAiService.generateSolution(problem)
            }
        }
        
        assertTrue(exception.message?.contains("No API key available") == true)
    }
} 