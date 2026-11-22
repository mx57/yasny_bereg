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
import com.example.data.ExpandedRecoveryContent
import java.util.Calendar
import java.util.concurrent.atomic.AtomicInteger

object NotificationHelper {
    const val CHANNEL_DAILY_MOTIVATION = "yasny_bereg_daily_motivation"
    const val CHANNEL_CRITICAL_STATE = "yasny_bereg_critical_alerts"

    const val EXTRA_HOUR = "extra_reminder_hour"
    const val EXTRA_SLOT = "extra_reminder_slot"
    const val EXTRA_TITLE = "extra_notification_title"
    const val EXTRA_BODY = "extra_notification_body"
    const val EXTRA_IS_CRITICAL = "extra_is_critical"

    val reminderTimes: List<String> = listOf("08:00", "12:30", "16:00", "20:00")
    private const val PREFS = "yasny_bereg_prefs"
    private const val KEY_TIMES = "notif_times"
    private const val KEY_ENABLED = "notif_enabled"
    private const val LEGACY_REQUEST_CODE = 777
    private const val FIRST_REQUEST_CODE = 8200

    /** Monotonic ids for on-demand notifications so consecutive posts never overwrite each other. */
    private val nextAdhocNotificationId = AtomicInteger(90_100)

    fun ensureChannelsCreated(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY_MOTIVATION,
                context.getString(R.string.notification_channel_daily),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Поддерживающие сообщения по выбранному расписанию"
            }
            val criticalChannel = NotificationChannel(
                CHANNEL_CRITICAL_STATE,
                context.getString(R.string.notification_channel_critical),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Ручные уведомления приложения; не является медицинским мониторингом"
                enableVibration(true)
            }
            manager.createNotificationChannel(dailyChannel)
            manager.createNotificationChannel(criticalChannel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true

    fun sendNotificationNow(
        context: Context,
        title: String,
        body: String,
        isCritical: Boolean = false,
        notificationId: Int? = null
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
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(if (isCritical) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        return try {
            val id = notificationId
                ?: if (isCritical) 9001
                else 90_100 + (nextAdhocNotificationId.getAndIncrement() % 90_000)
            NotificationManagerCompat.from(context).notify(id, notification)
            true
        } catch (_: SecurityException) {
            false
        }
    }

    /** Schedule only the selected time slots; Android will deliver inexact alarms near each time. */
    fun scheduleDailyMotivations(context: Context, times: Set<String>) {
        ensureChannelsCreated(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        cancelLegacyAlarm(context, alarmManager)
        val selected = times.intersect(reminderTimes.toSet())
        reminderTimes.forEachIndexed { index, time ->
            val pendingIntent = reminderPendingIntent(context, index, time)
            if (time !in selected) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            } else {
                val (hour, minute) = parseTime(time) ?: return@forEachIndexed
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
                }
                runCatching {
                    alarmManager.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                    )
                }
            }
        }
    }

    fun cancelAllDailyMotivations(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        reminderTimes.forEachIndexed { index, time ->
            val pendingIntent = reminderPendingIntent(context, index, time)
            alarmManager?.cancel(pendingIntent)
            pendingIntent.cancel()
        }
        if (alarmManager != null) cancelLegacyAlarm(context, alarmManager)
    }

    /** Recreate alarms after device restart, time change, or time-zone change. */
    fun restoreSavedSchedule(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_ENABLED, false)) {
            cancelAllDailyMotivations(context)
            return
        }
        val stored = prefs.getStringSet(KEY_TIMES, null)?.toSet()
        val legacyDefault = if (prefs.getBoolean(KEY_ENABLED, false)) {
            val target = prefs.getInt("notif_hour", 9) * 60 + prefs.getInt("notif_minute", 0)
            setOf(reminderTimes.minByOrNull { time ->
                val parts = time.split(":")
                kotlin.math.abs((parts[0].toInt() * 60 + parts[1].toInt()) - target)
            } ?: "08:00")
        } else emptySet()
        val selected = (stored ?: legacyDefault).intersect(reminderTimes.toSet())
        scheduleDailyMotivations(context, selected)
    }

    private fun reminderPendingIntent(context: Context, index: Int, time: String): PendingIntent {
        val hour = parseTime(time)?.first ?: 9
        val intent = Intent(context, DailyMotivationReceiver::class.java).apply {
            action = "${context.packageName}.DAILY_REMINDER.$time"
            putExtra(EXTRA_HOUR, hour)
            putExtra(EXTRA_SLOT, time)
        }
        return PendingIntent.getBroadcast(
            context,
            FIRST_REQUEST_CODE + index,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun cancelLegacyAlarm(context: Context, alarmManager: AlarmManager) {
        val oldIntent = Intent(context, DailyMotivationReceiver::class.java)
        val oldPendingIntent = PendingIntent.getBroadcast(
            context,
            LEGACY_REQUEST_CODE,
            oldIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarmManager.cancel(oldPendingIntent)
        oldPendingIntent.cancel()
    }

    private fun parseTime(value: String): Pair<Int, Int>? {
        val parts = value.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return null
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: return null
        return if (hour in 0..23 && minute in 0..59) hour to minute else null
    }
}

class DailyMotivationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val slot = intent?.getStringExtra(NotificationHelper.EXTRA_SLOT)
        val hour = intent?.getIntExtra(NotificationHelper.EXTRA_HOUR, 9) ?: 9
        val copy = if (slot == null) {
            NotificationHelper.sendNotificationNow(
                context,
                intent?.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: "Ясный Берег · напоминание",
                intent?.getStringExtra(NotificationHelper.EXTRA_BODY) ?: "Выберите один безопасный следующий шаг и, если нужно, обратитесь за поддержкой.",
                isCritical = false
            )
        } else {
            val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            val message = ExpandedRecoveryContent.reminderFor(hour, dayOfYear)
            NotificationHelper.sendNotificationNow(
                context = context,
                title = message.title,
                body = message.body,
                isCritical = false,
                notificationId = 8300 + NotificationHelper.reminderTimes.indexOf(slot).coerceAtLeast(0)
            )
        }
        // sendNotificationNow returns false when permission is absent; alarms remain scheduled for later days.
        @Suppress("UNUSED_VARIABLE") val delivered = copy
    }
}

class ReminderScheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> NotificationHelper.restoreSavedSchedule(context)
        }
    }
}
