package com.jreid.truckdash.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.view.View

/** Dark radial backdrop plus the metallic bezel that frames the video (mockup design B). */
class BezelBackground(context: Context) : View(context) {
    private val bg = Paint()
    private val ring = Paint(Paint.ANTI_ALIAS_FLAG)
    private val inner = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#050607") }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        bg.shader = RadialGradient(w * 0.5f, h * 0.42f, maxOf(w, h) * 0.75f,
            Color.parseColor("#1d232b"), Color.parseColor("#07080a"), Shader.TileMode.CLAMP)
        val o = DashLayout.bezelOuter
        ring.shader = LinearGradient(0f, o.top.toFloat(), 0f, o.bottom.toFloat(),
            intArrayOf(Color.parseColor("#4a525c"), Color.parseColor("#1c2026"), Color.parseColor("#3a414a")),
            floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bg)
        canvas.drawRoundRect(DashLayout.bezelOuter.toRectF(), 52f, 52f, ring)
        canvas.drawRoundRect(DashLayout.bezelInner.toRectF(), 44f, 44f, inner)
    }

    private fun Box.toRectF() = RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
}
