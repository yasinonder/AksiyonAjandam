package com.yasinonder.aksiyonajandam.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.yasinonder.aksiyonajandam.data.ActionDatabase

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_ID, -1L)
        if (id <= 0) return

        when (intent.action) {
            ACTION_DONE -> {
                ActionDatabase.get(context).setCompleted(id, true)
                AlarmScheduler.cancel(context, id)
                NotificationManagerCompat.from(context).cancel(id.toInt())
            }

            ACTION_SNOOZE_10 -> {
                AlarmScheduler.snooze(context, id, 10)
                NotificationManagerCompat.from(context).cancel(id.toInt())
            }

            ACTION_SNOOZE_30 -> {
                AlarmScheduler.snooze(context, id, 30)
                NotificationManagerCompat.from(context).cancel(id.toInt())
            }

            else -> showAlarm(context, id)
        }
    }

    private fun showAlarm(context: Context, id: Long) {
        val item = ActionDatabase.get(context).byId(id) ?: return
        if (item.completed) return

        ensureChannel(context)

        val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra(EXTRA_ID, id)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val fullScreenPending = PendingIntent.getActivity(
            context,
            id.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val done = actionPendingIntent(context, id, ACTION_DONE, 100_000)
        val snooze10 = actionPendingIntent(context, id, ACTION_SNOOZE_10, 200_000)
        val snooze30 = actionPendingIntent(context, id, ACTION_SNOOZE_30, 300_000)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(item.title)
            .setContentText(item.subject.ifBlank { "Aksiyon zamanı geldi." })
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPending, true)
            .setContentIntent(fullScreenPending)
            .addAction(0, "Tamamlandı", done)
            .addAction(0, "10 dk ertele", snooze10)
            .addAction(0, "30 dk ertele", snooze30)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(id.toInt(), notification)
        }
    }

    private fun actionPendingIntent(
        context: Context,
        id: Long,
        actionName: String,
        requestOffset: Int
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = actionName
            putExtra(EXTRA_ID, id)
        }
        return PendingIntent.getBroadcast(
            context,
            id.toInt() + requestOffset,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        val sound = Settings.System.DEFAULT_ALARM_ALERT_URI
        val audio = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Aksiyon Alarmları",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Aksiyon Ajandam hatırlatmaları"
            enableVibration(true)
            setSound(sound, audio)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }

        manager.createNotificationChannel(channel)
    }

    companion object {
        const val EXTRA_ID = "action_id"
        const val ACTION_FIRE = "com.yasinonder.aksiyonajandam.FIRE"
        const val ACTION_DONE = "com.yasinonder.aksiyonajandam.DONE"
        const val ACTION_SNOOZE_10 = "com.yasinonder.aksiyonajandam.SNOOZE_10"
        const val ACTION_SNOOZE_30 = "com.yasinonder.aksiyonajandam.SNOOZE_30"
        private const val CHANNEL_ID = "action_alarm_channel"
    }
}
