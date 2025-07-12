package com.lifeproblemsolver.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(tableName = "problems")
data class Problem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val notes: String = "",
    val category: String = "General",
    val priority: Priority = Priority.MEDIUM,
    val createdAt: Instant = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
    val updatedAt: Instant = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
    val aiSuggestion: String = "",
    val isResolved: Boolean = false
)

enum class Priority {
    LOW, MEDIUM, HIGH, URGENT
} 