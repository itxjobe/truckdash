package com.jreid.truckdash.climate

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View

/** Quick seat-warmer pill: seat glyph, label, three level bars. Each tap steps the truck's level. */
class SeatHeatButton(context: Context, private val label: String, onTap: () -> Unit) : View(context) {
    /** 0..3, or null when the truck has not reported it (bars drawn dim). */
    var level: Int? = null
        set(v) { field = v; invalidate() }

    private val on get() = (level ?: 0) > 0
    private val bgOff = fillPaint(0xFF12161B.toInt())
    private val bgOn = fillPaint(0xFF2A1A10.toInt())
    private val edgeOff = strokePaint(0xFF262D36.toInt(), 2f)
    private val edgeOn = strokePaint(0xFFB8621E.toInt(), 2f)
    private val glyphOff = strokePaint(0xFF9AA4B1.toInt(), 6f)
    private val glyphOn = strokePaint(0xFFFF9A4A.toInt(), 6f)
    private val barLit = fillPaint(0xFFFF9A4A.toInt())
    private val barDim = fillPaint(0xFF2E353E.toInt())
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 24f; letterSpacing = 0.12f; color = 0xFFCFD6DE.toInt() }
    private val pressed = fillPaint(0x22FFFFFF)

    private fun fillPaint(c: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = c }
    private fun strokePaint(c: Int, w: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = c; style = Paint.Style.STROKE; strokeWidth = w; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
    }

    init {
        setOnClickListener { onTap() }
        contentDescription = "$label seat heat"
    }

    override fun setPressed(p: Boolean) { super.setPressed(p); invalidate() }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat(); val r = h / 2
        val box = RectF(1f, 1f, w - 1, h - 1)
        c.drawRoundRect(box, r, r, if (on) bgOn else bgOff)
        c.drawRoundRect(box, r, r, if (on) edgeOn else edgeOff)
        if (isPressed) c.drawRoundRect(box, r, r, pressed)

        // seat glyph: backrest, cushion, and three heat waves
        val gx = 70f; val gy = h / 2
        c.drawPath(Path().apply {
            moveTo(gx - 10, gy - 40); lineTo(gx - 4, gy + 14); lineTo(gx + 40, gy + 14); lineTo(gx + 44, gy + 34)
        }, if (on) glyphOn else glyphOff)
        for (i in 0..2) {
            val x = gx + 10 + i * 14f
            c.drawPath(Path().apply { moveTo(x, gy - 4); quadTo(x - 6, gy - 14, x, gy - 24); quadTo(x + 6, gy - 34, x, gy - 44) },
                if (on) glyphOn else glyphOff)
        }

        c.drawText(label, 140f, gy + 9, text)

        val lv = level
        for (i in 0 until ClimateStateParser.SEAT_MAX) {
            val bx = w - 112 + i * 28f
            val bh = 24f + i * 14f
            c.drawRoundRect(RectF(bx, gy + 26 - bh, bx + 18, gy + 26), 6f, 6f,
                if (lv != null && i < lv) barLit else barDim)
        }
    }
}
