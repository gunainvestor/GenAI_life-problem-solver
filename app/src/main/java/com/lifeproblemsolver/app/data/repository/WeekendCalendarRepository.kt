package com.lifeproblemsolver.app.data.repository

import com.lifeproblemsolver.app.data.dao.WeekendCalendarDao
import com.lifeproblemsolver.app.data.model.WeekendCalendar
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeekendCalendarRepository @Inject constructor(
    private val weekendCalendarDao: WeekendCalendarDao
) {
    
    fun getAllWeekends(): Flow<List<WeekendCalendar>> {
        return weekendCalendarDao.getAllWeekends()
    }
    
    fun getWeekendsInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<WeekendCalendar>> {
        return weekendCalendarDao.getWeekendsInRange(startDate, endDate)
    }
    
    fun getSelectedWeekends(): Flow<List<WeekendCalendar>> {
        return weekendCalendarDao.getSelectedWeekends()
    }
    
    suspend fun getWeekendByDate(date: LocalDate): WeekendCalendar? {
        return weekendCalendarDao.getWeekendByDate(date)
    }
    
    suspend fun insertWeekend(weekend: WeekendCalendar): Long {
        return weekendCalendarDao.insertWeekend(weekend)
    }
    
    suspend fun updateWeekendSelection(date: LocalDate, isSelected: Boolean) {
        weekendCalendarDao.updateWeekendSelection(date, isSelected)
    }
    
    suspend fun updateWeekendNote(date: LocalDate, note: String) {
        weekendCalendarDao.updateWeekendNote(date, note)
    }
    
    suspend fun deleteWeekend(weekend: WeekendCalendar) {
        weekendCalendarDao.deleteWeekend(weekend)
    }
    
    suspend fun deleteAllWeekends() {
        weekendCalendarDao.deleteAllWeekends()
    }
    
    /**
     * Generate weekends for the next 5 years and insert them into the database
     */
    suspend fun generateWeekendsForNext5Years() {
        val currentDate = LocalDate.now()
        val endDate = currentDate.plusYears(5)
        
        val weekends = mutableListOf<WeekendCalendar>()
        var currentWeekend = getNextWeekend(currentDate)
        
        while (currentWeekend.isBefore(endDate) || currentWeekend.isEqual(endDate)) {
            weekends.add(WeekendCalendar(date = currentWeekend))
            currentWeekend = currentWeekend.plusWeeks(1)
        }
        
        if (weekends.isNotEmpty()) {
            weekendCalendarDao.insertWeekends(weekends)
        }
    }
    
    /**
     * Get the next weekend date from a given date
     */
    private fun getNextWeekend(fromDate: LocalDate): LocalDate {
        var date = fromDate
        while (date.dayOfWeek != DayOfWeek.SATURDAY && date.dayOfWeek != DayOfWeek.SUNDAY) {
            date = date.plusDays(1)
        }
        return date
    }
    
    /**
     * Format date for display
     */
    fun formatDateForDisplay(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        return date.format(formatter)
    }
    
    /**
     * Get day of week name
     */
    fun getDayOfWeekName(date: LocalDate): String {
        return date.dayOfWeek.name.lowercase().capitalize()
    }
    
    /**
     * Check if a date is a weekend
     */
    fun isWeekend(date: LocalDate): Boolean {
        return date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
    }
} 