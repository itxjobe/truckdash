package com.jreid.truckdash

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log

/**
 * Claims the firmware's first auto-start slot so the dash launches at every ACC wake and boot.
 * The firmware force-stops all user apps at sleep, so the firmware has to launch the dash; it cannot wake itself.
 * Writes with "keyname=?" + args: only then does SysVarProvider notify "update/[]KEY=VALUE", which is
 * what refreshes eventcenter's cached copy. A write without args leaves the cache stale and eventcenter
 * clears the slot at the next wake (docs/notes/startup-signals.md).
 */
object AutoStartSlot {
    private val URI = Uri.parse("content://com.szchoiceway.eventcenter.SysVarProvider/SysVar")
    const val PACKAGE_KEY = "SET_AutoStartPackageName_KEY"
    const val CLASS_KEY = "SET_AutoStartClassName_KEY"

    fun claim(context: Context) {
        try {
            put(context, PACKAGE_KEY, context.packageName)
            put(context, CLASS_KEY, DashActivity::class.java.name)
        } catch (e: Exception) {
            Log.w("TruckDash", "could not claim auto-start slot", e)
        }
    }

    private fun put(context: Context, key: String, value: String) {
        val n = context.contentResolver.update(URI, ContentValues().apply { put("keyvalue", value) }, "keyname=?", arrayOf(key))
        Log.i("TruckDash", "auto-start $key=$value rows=$n")
    }
}
