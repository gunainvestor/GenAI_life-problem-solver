package com.lifeproblemsolver.app.di

import android.content.Context
import com.lifeproblemsolver.app.BuildConfig
import com.lifeproblemsolver.app.data.analytics.AnalyticsService
import com.lifeproblemsolver.app.data.dao.UsageStatsDao
import com.lifeproblemsolver.app.data.dao.UserApiKeyDao
import com.lifeproblemsolver.app.data.remote.AiService
import com.lifeproblemsolver.app.data.remote.OpenAiService
import com.lifeproblemsolver.app.data.remote.RateLimitedAiService
import com.lifeproblemsolver.app.data.repository.UsageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideUsageRepository(
        usageStatsDao: UsageStatsDao,
        userApiKeyDao: UserApiKeyDao,
        @ApplicationContext context: Context
    ): UsageRepository {
        return UsageRepository(usageStatsDao, userApiKeyDao, context)
    }

    @Provides
    @Singleton
    fun provideAiService(usageRepository: UsageRepository): AiService {
        return RateLimitedAiService(usageRepository)
    }
    
    @Provides
    @Singleton
    fun provideAnalyticsService(): AnalyticsService {
        return AnalyticsService()
    }
} 