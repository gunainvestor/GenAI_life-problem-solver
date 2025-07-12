package com.lifeproblemsolver.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "user_api_keys")
data class UserApiKey(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "API Key",
    val apiKey: String,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastUsed: LocalDateTime? = null
) 