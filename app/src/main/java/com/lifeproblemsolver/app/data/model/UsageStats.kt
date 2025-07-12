package com.lifeproblemsolver.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "usage_stats")
data class UsageStats(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val installationId: String,
    val requestCount: Int = 0,
    val lastRequestTime: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
) 