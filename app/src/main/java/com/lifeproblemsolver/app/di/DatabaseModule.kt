package com.lifeproblemsolver.app.di

import android.content.Context
import androidx.room.Room
import com.lifeproblemsolver.app.data.dao.ProblemDao
import com.lifeproblemsolver.app.data.dao.UsageStatsDao
import com.lifeproblemsolver.app.data.dao.UserApiKeyDao
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
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "life_problem_solver_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideProblemDao(database: AppDatabase): ProblemDao {
        return database.problemDao()
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
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }
} 