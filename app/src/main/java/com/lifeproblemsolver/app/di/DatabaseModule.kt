package com.lifeproblemsolver.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

    // Define migrations to preserve data during app updates
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create new table with the updated schema
            database.execSQL(
                "CREATE TABLE user_api_keys_new (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "name TEXT NOT NULL DEFAULT 'API Key', " +
                "apiKey TEXT NOT NULL, " +
                "isActive INTEGER NOT NULL DEFAULT 1, " +
                "createdAt TEXT NOT NULL, " +
                "lastUsed TEXT" +
                ")"
            )
            
            // Copy data from old table to new table
            database.execSQL(
                "INSERT INTO user_api_keys_new (id, name, apiKey, isActive, createdAt, lastUsed) " +
                "SELECT 1, 'API Key', apiKey, isActive, createdAt, lastUsed " +
                "FROM user_api_keys"
            )
            
            // Drop old table
            database.execSQL("DROP TABLE user_api_keys")
            
            // Rename new table to original name
            database.execSQL("ALTER TABLE user_api_keys_new RENAME TO user_api_keys")
        }
    }
    
    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create weekend_calendar table
            database.execSQL(
                "CREATE TABLE weekend_calendar (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "date TEXT NOT NULL, " +
                "isSelected INTEGER NOT NULL DEFAULT 0, " +
                "createdAt INTEGER NOT NULL" +
                ")"
            )
        }
    }
    
    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add note column to weekend_calendar table
            database.execSQL(
                "ALTER TABLE weekend_calendar ADD COLUMN note TEXT NOT NULL DEFAULT ''"
            )
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "life_problem_solver_database"
        )
        .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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
    fun provideWeekendCalendarDao(database: AppDatabase): WeekendCalendarDao {
        return database.weekendCalendarDao()
    }

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }
} 