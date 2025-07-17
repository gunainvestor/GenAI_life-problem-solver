package com.lifeproblemsolver.app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.lifeproblemsolver.app.MainActivity
import com.lifeproblemsolver.app.R
import javax.inject.Inject

class NotificationService @Inject constructor(
    private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "life_problem_solver_channel"
        const val CHANNEL_NAME = "Life Problem Solver"
        const val CHANNEL_DESCRIPTION = "Notifications for problem reminders and updates"
        const val NOTIFICATION_ID_REMINDER = 1001
        const val NOTIFICATION_ID_URGENT = 1002
        const val NOTIFICATION_ID_DAILY = 1003

        fun from(context: Context): NotificationService = NotificationService(context.applicationContext)
    }
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    init {
        createNotificationChannel()
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    fun showProblemReminder(unresolvedCount: Int, urgentCount: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val title = when {
            urgentCount > 0 -> "🚨 $urgentCount Urgent Problems Need Attention"
            unresolvedCount > 0 -> "📋 $unresolvedCount Problems Awaiting Solution"
            else -> "Life Problem Solver"
        }
        val message = when {
            urgentCount > 0 -> "You have $urgentCount urgent problems that need immediate attention."
            unresolvedCount > 0 -> "You have $unresolvedCount unresolved problems. Time to tackle them!"
            else -> "Keep track of your life problems with AI assistance."
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(NOTIFICATION_ID_REMINDER, notification)
    }
    fun showUrgentProblemReminder(problemTitle: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🚨 Urgent Problem Reminder")
            .setContentText("Don't forget: $problemTitle")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(NOTIFICATION_ID_URGENT, notification)
    }
} 