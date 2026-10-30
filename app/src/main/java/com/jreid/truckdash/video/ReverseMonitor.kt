package com.jreid.truckdash.video

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/** Reverse gear from eventcenter (see docs/notes/reverse-signal.md). */
class ReverseMonitor(private val context: Context, private val onChange: (Boolean) -> Unit) {
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) { onChange(i.action == START) }
    }

    fun start() {
        context.registerReceiver(receiver, IntentFilter().apply { addAction(START); addAction(END) })
        onChange(readProperty() == "1")
    }

    /** Safe to call even if start() never ran or failed. */
    fun stop() {
        try { context.unregisterReceiver(receiver) } catch (_: IllegalArgumentException) {}
    }

    private fun readProperty(): String = try {
        Class.forName("android.os.SystemProperties")
            .getMethod("get", String::class.java, String::class.java)
            .invoke(null, PROP, "0") as String
    } catch (e: Exception) { "0" }

    companion object {
        const val START = "com.choiceway.eventcenter.EventUtils.MCU_MSG_BACKCAR_START"
        const val END = "com.choiceway.eventcenter.EventUtils.MCU_MSG_BACKCAR_END"
        const val PROP = "sys.backcar.state"
    }
}
