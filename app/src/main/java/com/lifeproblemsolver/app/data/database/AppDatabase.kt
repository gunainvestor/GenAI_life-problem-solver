package com.lifeproblemsolver.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lifeproblemsolver.app.data.dao.InterviewPrepDao
import com.lifeproblemsolver.app.data.dao.ProblemDao
import com.lifeproblemsolver.app.data.dao.UsageStatsDao
import com.lifeproblemsolver.app.data.dao.UserApiKeyDao
import com.lifeproblemsolver.app.data.dao.WeekendCalendarDao
import com.lifeproblemsolver.app.data.model.*

@Database(
    entities = [
        Goal::class,
        WeeklyGoal::class,
        DailyPlan::class,
        Application::class,
        Problem::class,
        UsageStats::class,
        UserApiKey::class,
        WeekendCalendar::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun interviewPrepDao(): InterviewPrepDao
    abstract fun problemDao(): ProblemDao
    abstract fun usageStatsDao(): UsageStatsDao
    abstract fun userApiKeyDao(): UserApiKeyDao
    abstract fun weekendCalendarDao(): WeekendCalendarDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "interview_prep_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 