package com.jreid.truckdash

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/** ACC wake and sleep from eventcenter (docs/notes/startup-signals.md). */
class AccMonitor(private val context: Context, private val onEvent: (StartupEvent) -> Unit) {
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) {
            onEvent(if (i.getIntExtra(EXTRA, 0) == 1) StartupEvent.ACC_ON else StartupEvent.ACC_OFF)
        }
    }

    fun start() { context.registerReceiver(receiver, IntentFilter(ACTION)) }
    fun stop() = context.unregisterReceiver(receiver)

    companion object {
        const val ACTION = "com.szchoiceway.eventcenter.EventUtils.ACTION_ACC_SLEEP_STATUS_EVT"
        const val EXTRA = "ACC_Status"
    }
}
