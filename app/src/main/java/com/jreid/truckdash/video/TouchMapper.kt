package com.jreid.truckdash.video

import android.view.MotionEvent
import com.jreid.truckdash.mcu.McuPackets

/**
 * Turns touches on the video view into factory-radio touch frames.
 * Matches the Console app: scale to 0..799 / 0..479, resend "down" every 500 ms while held,
 * and always end a press with "up" (including cancel and sliding off the view).
 */
class TouchMapper(private val send: (ByteArray) -> Unit, private val scheduler: Scheduler) {
    private var pressed = false
    private var lastX = 0
    private var lastY = 0
    private val repeat = object : Runnable {
        override fun run() {
            send(McuPackets.touch(lastX, lastY, true))
            scheduler.postDelayed(HOLD_REPEAT_MS, this)
        }
    }

    fun onEvent(action: Int, x: Float, y: Float, viewW: Int, viewH: Int) {
        val inside = x in 0f..viewW.toFloat() && y in 0f..viewH.toFloat()
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                if (!inside) return
                press(x, y, viewW, viewH)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!pressed) return
                if (inside) press(x, y, viewW, viewH) else release()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> if (pressed) release()
        }
    }

    /** End any held press. For when the window is lost without a CANCEL (pause, reverse, wake). */
    fun reset() { if (pressed) release() }

    private fun press(x: Float, y: Float, viewW: Int, viewH: Int) {
        lastX = (x * (McuPackets.SRC_W - 1) / viewW).toInt()
        lastY = (y * (McuPackets.SRC_H - 1) / viewH).toInt()
        pressed = true
        scheduler.cancel(repeat)
        send(McuPackets.touch(lastX, lastY, true))
        scheduler.postDelayed(HOLD_REPEAT_MS, repeat)
    }

    private fun release() {
        pressed = false
        scheduler.cancel(repeat)
        send(McuPackets.touch(lastX, lastY, false))
    }

    companion object { const val HOLD_REPEAT_MS = 500L }
}
