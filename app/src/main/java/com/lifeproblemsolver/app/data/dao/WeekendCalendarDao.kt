package com.lifeproblemsolver.app.data.dao

import androidx.room.*
import com.lifeproblemsolver.app.data.model.WeekendCalendar
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface WeekendCalendarDao {
    
    @Query("SELECT * FROM weekend_calendar ORDER BY date ASC")
    fun getAllWeekends(): Flow<List<WeekendCalendar>>
    
    @Query("SELECT * FROM weekend_calendar WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getWeekendsInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<WeekendCalendar>>
    
    @Query("SELECT * FROM weekend_calendar WHERE isSelected = 1 ORDER BY date ASC")
    fun getSelectedWeekends(): Flow<List<WeekendCalendar>>
    
    @Query("SELECT * FROM weekend_calendar WHERE date = :date LIMIT 1")
    suspend fun getWeekendByDate(date: LocalDate): WeekendCalendar?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeekend(weekend: WeekendCalendar): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeekends(weekends: List<WeekendCalendar>)
    
    @Update
    suspend fun updateWeekend(weekend: WeekendCalendar)
    
    @Query("UPDATE weekend_calendar SET isSelected = :isSelected WHERE date = :date")
    suspend fun updateWeekendSelection(date: LocalDate, isSelected: Boolean)
    
    @Query("UPDATE weekend_calendar SET note = :note WHERE date = :date")
    suspend fun updateWeekendNote(date: LocalDate, note: String)
    
    @Delete
    suspend fun deleteWeekend(weekend: WeekendCalendar)
    
    @Query("DELETE FROM weekend_calendar WHERE date < :date")
    suspend fun deleteWeekendsBefore(date: LocalDate)
    
    @Query("DELETE FROM weekend_calendar")
    suspend fun deleteAllWeekends()
} 