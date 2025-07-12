package com.lifeproblemsolver.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "weekend_calendar")
data class WeekendCalendar(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val isSelected: Boolean = false,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
) 