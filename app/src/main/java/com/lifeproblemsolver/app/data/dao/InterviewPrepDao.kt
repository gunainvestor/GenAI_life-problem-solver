
package com.lifeproblemsolver.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.lifeproblemsolver.app.data.model.*
import java.util.Date

@Dao
interface InterviewPrepDao {

    @Insert
    suspend fun insertGoal(goal: Goal): Long

    @Update
    suspend fun updateGoal(goal: Goal)

    @Query("SELECT * FROM goals")
    suspend fun getAllGoals(): List<Goal>

    @Insert
    suspend fun insertWeeklyGoal(weeklyGoal: WeeklyGoal): Long

    @Update
    suspend fun updateWeeklyGoal(weeklyGoal: WeeklyGoal)

    @Query("SELECT * FROM weekly_goals WHERE goalId = :goalId")
    suspend fun getWeeklyGoalsForGoal(goalId: Long): List<WeeklyGoal>

    @Insert
    suspend fun insertDailyPlan(dailyPlan: DailyPlan): Long

    @Update
    suspend fun updateDailyPlan(dailyPlan: DailyPlan)

    @Query("SELECT * FROM daily_plans WHERE weeklyGoalId = :weeklyGoalId")
    suspend fun getDailyPlansForWeeklyGoal(weeklyGoalId: Long): List<DailyPlan>

    @Query("SELECT * FROM daily_plans WHERE date = :date")
    suspend fun getDailyPlansForDate(date: Date): List<DailyPlan>

    @Insert
    suspend fun insertApplication(application: Application): Long

    @Update
    suspend fun updateApplication(application: Application)

    @Query("SELECT * FROM applications WHERE dailyPlanId = :dailyPlanId")
    suspend fun getApplicationsForDailyPlan(dailyPlanId: Long): List<Application>
    
    @Query("SELECT * FROM applications WHERE appliedDate = :date")
    suspend fun getApplicationsForDate(date: Date): List<Application>
} 