package com.yasinonder.aksiyonajandam.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yasinonder.aksiyonajandam.data.ActionDatabase

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        ActionDatabase.get(context)
            .pendingAlarmItems()
            .forEach { AlarmScheduler.schedule(context, it) }
    }
}
