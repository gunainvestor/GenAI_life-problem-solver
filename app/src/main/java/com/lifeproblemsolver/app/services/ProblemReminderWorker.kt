package com.lifeproblemsolver.app.services

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lifeproblemsolver.app.data.database.AppDatabase
import com.lifeproblemsolver.app.data.model.Priority
import kotlinx.coroutines.flow.first

class ProblemReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val problems = database.problemDao().getAllProblems().first()
            val unresolvedProblems = problems.filter { !it.isResolved }
            val urgentProblems = unresolvedProblems.filter { it.priority == Priority.URGENT }

            if (unresolvedProblems.isNotEmpty()) {
                NotificationService.from(applicationContext).showProblemReminder(
                    unresolvedCount = unresolvedProblems.size,
                    urgentCount = urgentProblems.size
                )
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
} 