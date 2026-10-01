package com.yasinonder.aksiyonajandam.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.yasinonder.aksiyonajandam.data.ActionItem

object AlarmScheduler {
    fun schedule(context: Context, item: ActionItem) {
        if (!item.alarmEnabled || item.completed || item.id <= 0) return
        val triggerAt = runCatching { item.scheduledAtMillis() }.getOrNull() ?: return
        if (triggerAt <= System.currentTimeMillis()) return
        scheduleAt(context, item.id, triggerAt)
    }

    fun snooze(context: Context, id: Long, minutes: Int) {
        val triggerAt = System.currentTimeMillis() + minutes * 60_000L
        scheduleAt(context, id, triggerAt)
    }

    fun cancel(context: Context, id: Long) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.cancel(pendingIntent(context, id))
    }

    private fun scheduleAt(context: Context, id: Long, triggerAt: Long) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val operation = pendingIntent(context, id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !manager.canScheduleExactAlarms()) {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation)
        } else {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation)
        }
    }

    private fun pendingIntent(context: Context, id: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE
            putExtra(AlarmReceiver.EXTRA_ID, id)
        }
        return PendingIntent.getBroadcast(
            context,
            id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
