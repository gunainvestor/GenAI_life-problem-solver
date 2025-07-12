package com.lifeproblemsolver.app.ui.viewmodel

import com.lifeproblemsolver.app.data.repository.UsageRepository
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ApiKeySettingsViewModelTest {
    
    private lateinit var viewModel: ApiKeySettingsViewModel
    private lateinit var usageRepository: UsageRepository
    
    @Before
    fun setup() {
        usageRepository = mockk(relaxed = true)
        viewModel = ApiKeySettingsViewModel(usageRepository)
    }
    
    @Test
    fun `initial state has correct default values`() = runTest {
        // Given
        coEvery { usageRepository.hasUserApiKey() } returns false
        coEvery { usageRepository.getCurrentRequestCount() } returns 0
        
        // When
        val uiState = viewModel.uiState.value
        
        // Then
        assertEquals(false, uiState.hasUserApiKey)
        assertEquals(10, uiState.remainingRequests)
        assertEquals(false, uiState.isLoading)
        assertNull(uiState.error)
    }
    
    @Test
    fun `loadApiKeyStatus updates state when user has API key`() = runTest {
        // Given
        coEvery { usageRepository.hasUserApiKey() } returns true
        
        // When
        val uiState = viewModel.uiState.value
        
        // Then
        assertEquals(true, uiState.hasUserApiKey)
        assertEquals(-1, uiState.remainingRequests) // Unlimited
    }
    
    @Test
    fun `loadApiKeyStatus updates state when user has no API key`() = runTest {
        // Given
        coEvery { usageRepository.hasUserApiKey() } returns false
        coEvery { usageRepository.getCurrentRequestCount() } returns 3
        
        // When
        val uiState = viewModel.uiState.value
        
        // Then
        assertEquals(false, uiState.hasUserApiKey)
        assertEquals(7, uiState.remainingRequests) // 10 - 3 = 7
    }
    
    @Test
    fun `saveApiKey calls repository and updates state`() = runTest {
        // Given
        val apiKey = "test_api_key"
        coEvery { usageRepository.saveUserApiKey(apiKey) } returns Unit
        coEvery { usageRepository.hasUserApiKey() } returns true
        
        // When
        viewModel.saveApiKey(apiKey)
        
        // Then
        coVerify { usageRepository.saveUserApiKey(apiKey) }
        coVerify { usageRepository.hasUserApiKey() }
        coVerify { usageRepository.getCurrentRequestCount() }
    }
    
    @Test
    fun `saveApiKey handles errors correctly`() = runTest {
        // Given
        val apiKey = "test_api_key"
        val errorMessage = "Failed to save API key"
        coEvery { usageRepository.saveUserApiKey(apiKey) } throws Exception(errorMessage)
        
        // When
        viewModel.saveApiKey(apiKey)
        
        // Then
        val uiState = viewModel.uiState.value
        assertEquals(false, uiState.isLoading)
        assertTrue(uiState.error?.contains(errorMessage) == true)
    }
    
    @Test
    fun `saveApiKey does nothing when apiKey is blank`() = runTest {
        // Given
        val apiKey = ""
        
        // When
        viewModel.saveApiKey(apiKey)
        
        // Then
        coVerify(exactly = 0) { usageRepository.saveUserApiKey(any()) }
    }
    
    @Test
    fun `deleteApiKey calls repository and updates state`() = runTest {
        // Given
        coEvery { usageRepository.deleteUserApiKey() } returns Unit
        coEvery { usageRepository.hasUserApiKey() } returns false
        coEvery { usageRepository.getCurrentRequestCount() } returns 0
        
        // When
        viewModel.deleteApiKey()
        
        // Then
        coVerify { usageRepository.deleteUserApiKey() }
        coVerify { usageRepository.hasUserApiKey() }
        coVerify { usageRepository.getCurrentRequestCount() }
    }
    
    @Test
    fun `deleteApiKey handles errors correctly`() = runTest {
        // Given
        val errorMessage = "Failed to delete API key"
        coEvery { usageRepository.deleteUserApiKey() } throws Exception(errorMessage)
        
        // When
        viewModel.deleteApiKey()
        
        // Then
        val uiState = viewModel.uiState.value
        assertEquals(false, uiState.isLoading)
        assertTrue(uiState.error?.contains(errorMessage) == true)
    }
    
    @Test
    fun `loadApiKeyStatus handles errors correctly`() = runTest {
        // Given
        val errorMessage = "Failed to load API key status"
        coEvery { usageRepository.hasUserApiKey() } throws Exception(errorMessage)
        
        // When
        // The error will be handled in the init block
        
        // Then
        val uiState = viewModel.uiState.value
        assertEquals(false, uiState.isLoading)
        assertTrue(uiState.error?.contains(errorMessage) == true)
    }
} 