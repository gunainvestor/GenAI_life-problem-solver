
package com.lifeproblemsolver.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val targetDate: Date,
    val isCompleted: Boolean = false
)

@Entity(tableName = "weekly_goals")
data class WeeklyGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val goalId: Long,
    val week: Int,
    val title: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "daily_plans")
data class DailyPlan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weeklyGoalId: Long,
    val date: Date,
    val topic: String,
    val kpi: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "applications")
data class Application(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dailyPlanId: Long,
    val companyName: String,
    val position: String,
    val status: ApplicationStatus,
    val appliedDate: Date
)

enum class ApplicationStatus {
    APPLIED,
    CALLBACK_RECEIVED,
    FIRST_ROUND,
    SECOND_ROUND,
    OFFER_RECEIVED,
    REJECTED
} 