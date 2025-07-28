package com.lifeproblemsolver.app.di

import android.content.Context
import androidx.room.Room
import com.lifeproblemsolver.app.data.callback.DatabaseCallback
import com.lifeproblemsolver.app.data.dao.InterviewPrepDao
import com.lifeproblemsolver.app.data.dao.ProblemDao
import com.lifeproblemsolver.app.data.dao.UsageStatsDao
import com.lifeproblemsolver.app.data.dao.UserApiKeyDao
import com.lifeproblemsolver.app.data.dao.WeekendCalendarDao
import com.lifeproblemsolver.app.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideInterviewPrepDao(database: AppDatabase): InterviewPrepDao {
        return database.interviewPrepDao()
    }

    @Provides
    fun provideUsageStatsDao(database: AppDatabase): UsageStatsDao {
        return database.usageStatsDao()
    }

    @Provides
    fun provideUserApiKeyDao(database: AppDatabase): UserApiKeyDao {
        return database.userApiKeyDao()
    }

    @Provides
    fun provideProblemDao(database: AppDatabase): ProblemDao {
        return database.problemDao()
    }

    @Provides
    fun provideWeekendCalendarDao(database: AppDatabase): WeekendCalendarDao {
        return database.weekendCalendarDao()
    }

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }
    
    @Provides
    @Singleton
    fun provideDatabaseCallback(): DatabaseCallback {
        return DatabaseCallback()
    }
} 