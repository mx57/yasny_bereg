package com.example.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import java.util.Calendar

object NotificationHelper {
    const val CHANNEL_DAILY_MOTIVATION = "yasny_bereg_daily_motivation"
    const val CHANNEL_CRITICAL_STATE = "yasny_bereg_critical_alerts"

    const val EXTRA_TITLE = "extra_notification_title"
    const val EXTRA_BODY = "extra_notification_body"
    const val EXTRA_IS_CRITICAL = "extra_is_critical"

    fun ensureChannelsCreated(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY_MOTIVATION,
                context.getString(R.string.notification_channel_daily),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Ежедневные ободряющие сообщения, цитаты о трезвости и истории успеха"
            }

            val criticalChannel = NotificationChannel(
                CHANNEL_CRITICAL_STATE,
                context.getString(R.string.notification_channel_critical),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Автоматические уведомления при критическом скачке пульса, стресса или риска срыва"
                enableVibration(true)
            }

            manager.createNotificationChannel(dailyChannel)
            manager.createNotificationChannel(criticalChannel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun sendNotificationNow(
        context: Context,
        title: String,
        body: String,
        isCritical: Boolean = false
    ): Boolean {
        ensureChannelsCreated(context)
        if (!hasNotificationPermission(context)) return false

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            if (isCritical) 901 else 101,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = if (isCritical) CHANNEL_CRITICAL_STATE else CHANNEL_DAILY_MOTIVATION
        val priority = if (isCritical) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(priority)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(
                if (isCritical) 9001 else (System.currentTimeMillis() % 10000).toInt(),
                notification
            )
            true
        } catch (_: SecurityException) {
            false
        }
    }

    fun scheduleDailyMotivation(
        context: Context,
        hourOfDay: Int,
        minute: Int,
        title: String,
        body: String
    ) {
        ensureChannelsCreated(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, DailyMotivationReceiver::class.java).apply {
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_BODY, body)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            777,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (_: Exception) {
        }
    }
}

class DailyMotivationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val title = intent?.getStringExtra(NotificationHelper.EXTRA_TITLE)
            ?: "Ясный Берег: Послание трезвого дня"
        val body = intent?.getStringExtra(NotificationHelper.EXTRA_BODY)
            ?: "Каждый трезвый день возвращает здоровье вашему сердцу и ясность разуму. Откройте приложение для новой цитаты и поддержки."
        NotificationHelper.sendNotificationNow(
            context = context,
            title = title,
            body = body,
            isCritical = false
        )
    }
}
