package com.jreid.truckdash.climate

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View

/** Mockup B slim climate bar. Unknown values are not drawn. */
class ClimateBar(context: Context, private val onAction: (BarAction) -> Unit) : View(context) {
    var state: ClimateState = ClimateState.UNKNOWN
        set(v) { field = v; invalidate() }

    private val light = Typeface.create("sans-serif-light", Typeface.NORMAL)
    private val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF12161B.toInt() }
    private val border = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF262D36.toInt(); style = Paint.Style.STROKE; strokeWidth = 2f }
    private val big = text(84f, 0xFFE9EDF2.toInt(), light)
    private val mid = text(64f, 0xFFE9EDF2.toInt(), light)
    private val label = text(18f, 0xFF7D8794.toInt(), Typeface.DEFAULT).apply { letterSpacing = 0.2f }
    private val chev = text(30f, 0xFF4A5560.toInt(), Typeface.DEFAULT)
    private val pillOff = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF1B2027.toInt() }
    private val pillOffEdge = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF2A313A.toInt(); style = Paint.Style.STROKE; strokeWidth = 2f }
    private val pillOn = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF0F2A44.toInt() }
    private val pillOnEdge = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF1F6FB8.toInt(); style = Paint.Style.STROKE; strokeWidth = 2f }
    private val btnText = text(30f, 0xFFCFD6DE.toInt(), Typeface.DEFAULT_BOLD)
    private val btnTextOn = text(30f, 0xFF7CC0FF.toInt(), Typeface.DEFAULT_BOLD)

    private fun text(size: Float, c: Int, tf: Typeface) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = size; color = c; typeface = tf; textAlign = Paint.Align.CENTER }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        val r = RectF(1f, 1f, w - 1, h - 1)
        c.drawRoundRect(r, h / 2, h / 2, bg); c.drawRoundRect(r, h / 2, h / 2, border)
        val slot = w / ClimateBarHit.SLOTS
        fun cx(i: Int) = slot * i + slot / 2
        val cy = h / 2
        state.driverTemp?.let { tempSlot(c, cx(0), cy, it) }
        state.ac?.let { pill(c, cx(1), cy, "A/C", it) }
        state.fan?.let {
            c.drawText(it, cx(2), cy + 14, mid)
            c.drawText("FAN", cx(2), cy + 54, label)
            if (it != "OFF") { c.drawText("⌃", cx(2) + 80, cy - 10, chev); c.drawText("⌄", cx(2) + 80, cy + 30, chev) }
        }
        pill(c, cx(3), cy, "MODE", false)
        state.passTemp?.let { tempSlot(c, cx(4), cy, it) }
    }

    private fun tempSlot(c: Canvas, x: Float, cy: Float, t: String) {
        c.drawText("⌃", x, cy - 52, chev)
        c.drawText(t, x, cy + 30, big)
        c.drawText("⌄", x, cy + 80, chev)
    }

    private fun pill(c: Canvas, x: Float, cy: Float, t: String, on: Boolean) {
        val r = RectF(x - 70, cy - 55, x + 70, cy + 55)
        c.drawRoundRect(r, 55f, 55f, if (on) pillOn else pillOff)
        c.drawRoundRect(r, 55f, 55f, if (on) pillOnEdge else pillOffEdge)
        c.drawText(t, x, cy + 11, if (on) btnTextOn else btnText)
    }

    private val tap = ClimateTap()

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> tap.down(e.x.toInt(), e.y.toInt(), width, height)
            MotionEvent.ACTION_UP -> {
                tap.up(e.x.toInt(), e.y.toInt(), width, height)?.let(onAction)
                performClick()
            }
            MotionEvent.ACTION_CANCEL -> tap.cancel()
        }
        return true
    }

    override fun performClick(): Boolean = super.performClick()
}
