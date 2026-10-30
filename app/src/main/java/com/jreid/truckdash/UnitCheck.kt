package com.jreid.truckdash

import android.content.Context
import android.net.Uri
import android.util.Log

/**
 * Car commands (climate, seat heat, touch) were worked out on a CanType 16 unit only. Other CanTypes
 * use other code paths in the vendor Console app, with different key codes, so the dash stays
 * display-only there. CanType is the vendor setting `Sys_CarType`, read the way the Console reads it.
 */
object UnitCheck {
    const val TESTED_CAN_TYPE = 16
    private val URI = Uri.parse("content://com.szchoiceway.eventcenter.SysVarProvider/SysVar")

    /** null = could not be read; the check cannot judge then, so it does not block. */
    fun allowsCarCommands(canType: Int?): Boolean = canType == null || canType == TESTED_CAN_TYPE

    fun readCanType(context: Context): Int? = try {
        context.contentResolver.query(URI, null, "keyname=?", arrayOf("Sys_CarType"), null)?.use { c ->
            if (c.moveToFirst()) c.getString(c.getColumnIndexOrThrow("keyvalue"))?.trim()?.toIntOrNull() else null
        }
    } catch (e: Exception) {
        Log.w("TruckDash", "could not read CanType", e)
        null
    }
}
