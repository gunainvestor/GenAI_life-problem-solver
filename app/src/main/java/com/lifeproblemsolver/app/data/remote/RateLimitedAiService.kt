package com.lifeproblemsolver.app.data.remote

import com.lifeproblemsolver.app.BuildConfig
import com.lifeproblemsolver.app.data.exception.RateLimitExceededException
import com.lifeproblemsolver.app.data.repository.UsageRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RateLimitedAiService @Inject constructor(
    private val usageRepository: UsageRepository
) : AiService {
    
    private var userApiKeyService: OpenAiService? = null
    private var predefinedApiKeyService: OpenAiService? = null
    
    private suspend fun getPredefinedApiKey(): String {
        return try {
            BuildConfig.OPENAI_API_KEY.ifEmpty { 
                System.getenv("OPENAI_API_KEY") ?: ""
            }
        } catch (e: Exception) {
            System.getenv("OPENAI_API_KEY") ?: ""
        }
    }
    
    private suspend fun getUserApiKey(): String? {
        return usageRepository.getUserApiKey().first()?.apiKey
    }
    
    private suspend fun getApiKeyService(): OpenAiService {
        // First, try to use user's API key
        val userKey = getUserApiKey()
        if (!userKey.isNullOrEmpty()) {
            if (userApiKeyService == null) {
                userApiKeyService = OpenAiService(userKey)
            }
            return userApiKeyService!!
        }
        
        // Fall back to predefined API key with rate limiting
        val predefinedKey = getPredefinedApiKey()
        if (predefinedKey.isEmpty()) {
            throw IllegalStateException("No API key available. Please add your OpenAI API key in settings.")
        }
        
        if (predefinedApiKeyService == null) {
            predefinedApiKeyService = OpenAiService(predefinedKey)
        }
        
        // Check rate limit for predefined key
        if (usageRepository.hasReachedLimit()) {
            throw RateLimitExceededException(
                "You've reached the limit of ${UsageRepository.MAX_REQUESTS_WITH_PREDEFINED_KEY} requests with the predefined API key. " +
                "Please add your own OpenAI API key in settings for unlimited usage."
            )
        }
        
        return predefinedApiKeyService!!
    }
    
    override suspend fun generateSolution(request: SolutionRequest): SolutionResponse {
        val service = getApiKeyService()
        
        // Check if using predefined key and increment usage
        val userKey = getUserApiKey()
        if (userKey.isNullOrEmpty()) {
            usageRepository.incrementRequestCount()
        } else {
            usageRepository.updateLastUsed()
        }
        
        return service.generateSolution(request)
    }
} 