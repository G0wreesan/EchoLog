package com.echolog.app.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.echolog.app.data.LogEntity
import com.echolog.app.receiver.ReminderReceiver

object ReminderScheduler {
    fun scheduleReminder(context: Context, log: LogEntity) {
        val scheduledTime = log.scheduledAt?.toLongOrNull() ?: return
        if (scheduledTime <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", log.title)
            putExtra("caption", log.caption)
            putExtra("logId", log.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            log.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                scheduledTime,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Fallback for when exact alarms are not permitted
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                scheduledTime,
                pendingIntent
            )
        }
    }

    fun cancelReminder(context: Context, logId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            logId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
