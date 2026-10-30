package com.jreid.truckdash.climate

/** Acts only when a touch lifts on the same control (and half) it went down on, so a slide never sends a command. */
class ClimateTap {
    private var downAction: BarAction? = null

    fun down(x: Int, y: Int, w: Int, h: Int) { downAction = ClimateBarHit.actionAt(x, y, w, h) }

    fun up(x: Int, y: Int, w: Int, h: Int): BarAction? {
        val start = downAction ?: return null
        downAction = null
        return if (ClimateBarHit.actionAt(x, y, w, h) == start) start else null
    }

    fun cancel() { downAction = null }
}
