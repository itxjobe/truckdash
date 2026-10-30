package com.jreid.truckdash.climate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.szchoiceway.canbus.CarAirState

/** Live climate from the canbus app and outside temp from eventcenter (docs/notes/climate-keys.md). */
class ClimateMonitor(private val context: Context, private val onState: (ClimateState) -> Unit) {
    private val store = ClimateStore()

    private val receiver = object : BroadcastReceiver() {
        @Suppress("DEPRECATION")
        override fun onReceive(c: Context, i: Intent) {
            when (i.action) {
                AIR -> store.onAir(try {
                    i.setExtrasClassLoader(CarAirState::class.java.classLoader)
                    i.getParcelableExtra(AIR_EXTRA)
                } catch (e: Exception) { Log.w("TruckDash", "bad climate parcel", e); null })
                OUTSIDE -> store.onOutside(i.getStringExtra(OUTSIDE_EXTRA))
            }
            onState(store.current())
        }
    }

    fun start() { context.registerReceiver(receiver, IntentFilter().apply { addAction(AIR); addAction(OUTSIDE) }) }
    /** Safe to call even if start() never ran or failed. */
    fun stop() {
        try { context.unregisterReceiver(receiver) } catch (_: IllegalArgumentException) {}
    }

    /** Forget everything (ACC off); values come back with the next broadcasts. */
    fun reset() { store.reset(); onState(store.current()) }

    companion object {
        const val AIR = "com.choiceway.canbus.carairstruct"
        const val AIR_EXTRA = "com.choiceway.canbus.carairstruct.airstate"
        const val OUTSIDE = "com.choiceway.eventcenter.CanUtils.CAN_CAR_OUT_SIDE_TEMP_EVT"
        const val OUTSIDE_EXTRA = "com.choiceway.eventcenter.CanUtils.CAN_CAR_OUT_SIDE_TEMP_EVT_EXTRA_STR"
    }
}
