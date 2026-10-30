package com.jreid.truckdash.video

import android.os.Handler
import android.os.Looper

/** Delayed-callback seam so timing logic is testable on the JVM. */
interface Scheduler {
    fun postDelayed(ms: Long, r: Runnable)
    fun cancel(r: Runnable)
}

class MainScheduler : Scheduler {
    private val handler = Handler(Looper.getMainLooper())
    override fun postDelayed(ms: Long, r: Runnable) { handler.postDelayed(r, ms) }
    override fun cancel(r: Runnable) { handler.removeCallbacks(r) }
}
