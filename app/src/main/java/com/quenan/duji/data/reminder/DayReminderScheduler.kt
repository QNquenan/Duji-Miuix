package com.quenan.duji.data.reminder

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.quenan.duji.MainActivity
import com.quenan.duji.data.day.DayData
import com.quenan.duji.data.day.DayRepository
import com.quenan.duji.data.day.nextReminderEventDate
import com.quenan.duji.data.day.nextReminderTriggerDate
import com.quenan.duji.data.day.reminderDaysUntil
import com.quenan.duji.data.day.reminderNotificationText
import com.quenan.duji.widget.WidgetIntentFactory
import com.quenan.duji.widget.WidgetTargetType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object DayReminderScheduler {
    const val ACTION_DAY_REMINDER = "com.quenan.duji.action.DAY_REMINDER"
    const val EXTRA_DAY_ID = "day_id"
    const val EXTRA_REMIND_ON_DAY = "remind_on_day"

    private const val CHANNEL_ID = "those_days_reminders"
    private const val CHANNEL_NAME = "\u90a3\u4e9b\u65e5\u5b50\u63d0\u9192"

    fun createNotificationChannel(context: Context) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "\u751f\u65e5\u3001\u7eaa\u5ff5\u65e5\u548c\u5012\u6570\u65e5\u63d0\u9192"
            },
        )
    }

    fun schedule(context: Context, day: DayData) {
        cancel(context, day.id)
        if (!day.reminderEnabled) return

        if (day.reminderDaysBefore > 0) {
            scheduleReminder(context, day, remindOnDay = false)
        }
        if (day.remindOnDay) {
            scheduleReminder(context, day, remindOnDay = true)
        }
    }

    fun cancel(context: Context, dayId: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        listOf(false, true).forEach { remindOnDay ->
            val pendingIntent = reminderPendingIntent(context, dayId, remindOnDay)
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    suspend fun rescheduleAll(context: Context, days: List<DayData>? = null) {
        createNotificationChannel(context)
        val daysToSchedule = days ?: DayRepository(context).getAllDays()
        daysToSchedule.forEach { day -> schedule(context, day) }
    }

    fun showReminderIfDue(context: Context, day: DayData, remindOnDay: Boolean) {
        if (!day.reminderEnabled) return
        if (remindOnDay && !day.remindOnDay) return
        if (!remindOnDay && day.reminderDaysBefore <= 0) return
        if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val daysBefore = if (remindOnDay) 0 else day.reminderDaysBefore
        if (day.reminderDaysUntil() != daysBefore) return

        createNotificationChannel(context)
        val contentIntent = PendingIntent.getActivity(
            context,
            notificationRequestCode(day.id),
            Intent(context, MainActivity::class.java)
                .putExtra(WidgetIntentFactory.EXTRA_START_PAGE, 1)
                .putExtra(WidgetIntentFactory.EXTRA_TARGET_TYPE, WidgetTargetType.DAY.name)
                .putExtra(WidgetIntentFactory.EXTRA_TARGET_ID, day.id)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("\u90a3\u4e9b\u65e5\u5b50\u63d0\u9192")
            .setContentText(day.reminderNotificationText(daysBefore))
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationRequestCode(day.id, remindOnDay), notification)
    }

    private fun scheduleReminder(context: Context, day: DayData, remindOnDay: Boolean) {
        val triggerAtMillis = nextTriggerAtMillis(day, remindOnDay) ?: return
        context.getSystemService(AlarmManager::class.java)
            .setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                reminderPendingIntent(context, day.id, remindOnDay),
            )
    }

    private fun reminderPendingIntent(context: Context, dayId: Long, remindOnDay: Boolean): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            notificationRequestCode(dayId, remindOnDay),
            Intent(context, DayReminderAlarmReceiver::class.java)
                .setAction(ACTION_DAY_REMINDER)
                .putExtra(EXTRA_DAY_ID, dayId)
                .putExtra(EXTRA_REMIND_ON_DAY, remindOnDay),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun nextTriggerAtMillis(day: DayData, remindOnDay: Boolean): Long? {
        val now = LocalDateTime.now()
        val reminderTime = LocalTime.of(
            day.reminderHour.coerceIn(0, 23),
            day.reminderMinute.coerceIn(0, 59),
        )
        fun triggerDate(onOrAfter: LocalDate): LocalDate? = if (remindOnDay) {
            day.nextReminderEventDate(onOrAfter)
        } else {
            day.nextReminderTriggerDate(onOrAfter)
        }

        var date = triggerDate(now.toLocalDate()) ?: return null
        if (date == now.toLocalDate() && !reminderTime.isAfter(now.toLocalTime())) {
            date = triggerDate(now.toLocalDate().plusDays(1)) ?: return null
        }
        return LocalDateTime.of(date, reminderTime)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    private fun notificationRequestCode(dayId: Long, remindOnDay: Boolean = false): Int {
        val baseCode = (dayId xor (dayId ushr 32)).toInt()
        return if (remindOnDay) baseCode xor 0x40000000 else baseCode
    }
}

class DayReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DayReminderScheduler.ACTION_DAY_REMINDER) return
        val dayId = intent.getLongExtra(DayReminderScheduler.EXTRA_DAY_ID, -1L)
        if (dayId < 0L) return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val day = DayRepository(context.applicationContext).getAllDays()
                    .firstOrNull { it.id == dayId }
                if (day != null) {
                    val remindOnDay = intent.getBooleanExtra(
                        DayReminderScheduler.EXTRA_REMIND_ON_DAY,
                        day.remindOnDay && day.reminderDaysBefore == 0,
                    )
                    DayReminderScheduler.showReminderIfDue(context.applicationContext, day, remindOnDay)
                    DayReminderScheduler.schedule(context.applicationContext, day)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

class DayReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                DayReminderScheduler.rescheduleAll(context.applicationContext)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
