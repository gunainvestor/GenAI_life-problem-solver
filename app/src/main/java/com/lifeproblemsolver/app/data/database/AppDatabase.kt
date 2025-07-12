package com.lifeproblemsolver.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lifeproblemsolver.app.data.dao.ProblemDao
import com.lifeproblemsolver.app.data.dao.UsageStatsDao
import com.lifeproblemsolver.app.data.dao.UserApiKeyDao
import com.lifeproblemsolver.app.data.model.Problem
import com.lifeproblemsolver.app.data.model.UsageStats
import com.lifeproblemsolver.app.data.model.UserApiKey
import com.lifeproblemsolver.app.data.model.AppTypeConverters

@Database(
    entities = [
        Problem::class,
        UsageStats::class,
        UserApiKey::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun problemDao(): ProblemDao
    abstract fun usageStatsDao(): UsageStatsDao
    abstract fun userApiKeyDao(): UserApiKeyDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "life_problem_solver_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 