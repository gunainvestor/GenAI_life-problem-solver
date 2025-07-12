package com.lifeproblemsolver.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lifeproblemsolver.app.data.dao.ProblemDao
import com.lifeproblemsolver.app.data.dao.UsageStatsDao
import com.lifeproblemsolver.app.data.dao.UserApiKeyDao
import com.lifeproblemsolver.app.data.dao.WeekendCalendarDao
import com.lifeproblemsolver.app.data.model.Problem
import com.lifeproblemsolver.app.data.model.UsageStats
import com.lifeproblemsolver.app.data.model.UserApiKey
import com.lifeproblemsolver.app.data.model.WeekendCalendar
import com.lifeproblemsolver.app.data.model.AppTypeConverters

@Database(
    entities = [
        Problem::class,
        UsageStats::class,
        UserApiKey::class,
        WeekendCalendar::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun problemDao(): ProblemDao
    abstract fun usageStatsDao(): UsageStatsDao
    abstract fun userApiKeyDao(): UserApiKeyDao
    abstract fun weekendCalendarDao(): WeekendCalendarDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
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
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "life_problem_solver_database"
                )
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 