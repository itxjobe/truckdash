package com.jreid.truckdash.video

import android.util.Log
import android.view.Surface
import java.io.File
import com.szchoiceway.camera.CameraUtils

/** The factory radio video input. Single client: only one app may hold it. */
interface Feed {
    fun open(surface: Surface): Boolean
    fun close()
    fun frameCount(): Int
    /** True when the radio is sending video. Opening without it stays black until reopened. */
    fun hasSignal(): Boolean
    /** Ask the MCU to route the radio's video to the capture chip; it drops this over ACC sleep. */
    fun requestVideo()
}

/** Vendor AIS path, same calls and order as ATSLCarConsole on this unit (GT6, SDK > 29). */
class AisFeed(private val sendMcu: (ByteArray) -> Unit) : Feed {
    private var opened = false

    override fun open(surface: Surface): Boolean {
        if (opened) return true
        if (!CameraUtils.AVAILABLE) return false
        restoreStockTiming()
        CameraUtils.setSurface(surface, 0)
        CameraUtils.setSensorOutputFmt(1)
        val rc = CameraUtils.openCamera(0)
        opened = rc == 0
        Log.i(TAG, "openCamera rc=$rc")
        return opened
    }

    override fun close() {
        if (!opened) return
        CameraUtils.closeCamera()
        CameraUtils.deleteSurface(0)
        opened = false
        Log.i(TAG, "closeCamera")
    }

    override fun frameCount(): Int = if (opened) CameraUtils.getFrameCount(0) else 0

    /** Never a custom value: only the vendor's own string, so the bridge is exactly as the Console leaves it. */
    private fun restoreStockTiming() {
        try { File(BRIDGE_PATH).writeText(STOCK_TIMING) } catch (e: Exception) { Log.w(TAG, "bridge timing write failed", e) }
    }

    /** Same request the Console app sends (RequestCar8836LVDS) before it opens. */
    override fun requestVideo() = sendMcu(REQUEST_LVDS)

    /** Read-only sysfs flag the Console app polls. Unknown counts as present. */
    override fun hasSignal(): Boolean = try {
        File(SIGNAL_PATH).readText().trim() != "0"
    } catch (e: Exception) { true }

    companion object {
        const val TAG = "TruckDash"
        const val SIGNAL_PATH = "/sys/lt8918/lt8918_signal"
        val REQUEST_LVDS = byteArrayOf(0x49, 0x14, 0x01)
        const val BRIDGE_PATH = "/sys/lt8918/lt8918"
        /** Stock LT8918 timing. The Console app writes exactly this before every open (customer 4); sleep wipes it. */
        const val STOCK_TIMING = "hfp=20,hs=4,hbp=3,hact=800,vfp=6,vs=8,vbp=8,vact=480,rd_dly=552"
    }
}
