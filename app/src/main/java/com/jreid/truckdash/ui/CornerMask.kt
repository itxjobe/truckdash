package com.jreid.truckdash.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View

/** Paints black outside a rounded rect so the video underneath gets rounded corners and trimmed edges. */
class CornerMask(context: Context, private val radius: Float, private val inset: Float) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK }
    private val path = Path()

    override fun onDraw(canvas: Canvas) {
        path.reset()
        path.fillType = Path.FillType.EVEN_ODD
        path.addRect(0f, 0f, width.toFloat(), height.toFloat(), Path.Direction.CW)
        path.addRoundRect(RectF(inset, inset, width - inset, height - inset), radius, radius, Path.Direction.CW)
        canvas.drawPath(path, paint)
    }
}
