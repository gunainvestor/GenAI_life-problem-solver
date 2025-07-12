package com.lifeproblemsolver.app.di

import com.lifeproblemsolver.app.data.remote.AiService
import com.lifeproblemsolver.app.data.remote.OpenAiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAiService(): AiService {
        // Load API key from BuildConfig or environment variable
        val apiKey = BuildConfig.OPENAI_API_KEY.ifEmpty { 
            System.getenv("OPENAI_API_KEY") ?: ""
        }
        
        if (apiKey.isEmpty()) {
            throw IllegalStateException("OpenAI API key not found. Please set OPENAI_API_KEY in gradle.properties or environment variables.")
        }
        
        return OpenAiService(apiKey)
    }
} 