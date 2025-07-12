package com.lifeproblemsolver.app.data.model

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AppTypeConverters {
    
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, formatter) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        return date?.format(formatter)
    }
    
    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.name
    }
    
    @TypeConverter
    fun toPriority(value: String): Priority {
        return Priority.valueOf(value)
    }
} 