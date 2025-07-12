package com.lifeproblemsolver.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeproblemsolver.app.data.model.UserApiKey
import com.lifeproblemsolver.app.data.repository.UsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApiKeySettingsViewModel @Inject constructor(
    private val usageRepository: UsageRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ApiKeySettingsUiState())
    val uiState: StateFlow<ApiKeySettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadApiKeyStatus()
        loadAllApiKeys()
    }
    
    private fun loadApiKeyStatus() {
        viewModelScope.launch {
            try {
                val hasUserApiKey = usageRepository.hasUserApiKey()
                val remainingRequests = if (hasUserApiKey) {
                    -1 // Unlimited
                } else {
                    val currentCount = usageRepository.getCurrentRequestCount()
                    UsageRepository.MAX_REQUESTS_WITH_PREDEFINED_KEY - currentCount
                }
                
                _uiState.update { currentState ->
                    currentState.copy(
                        hasUserApiKey = hasUserApiKey,
                        remainingRequests = remainingRequests,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = "Failed to load API key status: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun loadAllApiKeys() {
        viewModelScope.launch {
            try {
                usageRepository.getAllApiKeys().collect { apiKeys ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            apiKeys = apiKeys,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = "Failed to load API keys: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun saveApiKey(apiKey: String, name: String = "API Key") {
        if (apiKey.isBlank()) return
        
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                usageRepository.saveUserApiKey(apiKey, name)
                loadApiKeyStatus()
                loadAllApiKeys()
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = "Failed to save API key: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun deleteApiKey(keyId: Long) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                usageRepository.deleteApiKey(keyId)
                loadApiKeyStatus()
                loadAllApiKeys()
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = "Failed to delete API key: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun setActiveApiKey(keyId: Long) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                usageRepository.setActiveApiKey(keyId)
                loadApiKeyStatus()
                loadAllApiKeys()
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = "Failed to set active API key: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun deleteApiKey() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                usageRepository.deleteUserApiKey()
                loadApiKeyStatus()
                loadAllApiKeys()
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = "Failed to delete API key: ${e.message}"
                    )
                }
            }
        }
    }
}

data class ApiKeySettingsUiState(
    val hasUserApiKey: Boolean = false,
    val remainingRequests: Int = 5,
    val apiKeys: List<UserApiKey> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) 