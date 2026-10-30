package com.jreid.truckdash.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View

enum class NavIcon { HOME, APPS }

/** Flat bar button in the bezel's silver, drawn in code so it scales with no assets. */
class NavButton(context: Context, private val icon: NavIcon, onTap: () -> Unit) : View(context) {
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFB8C0C8.toInt(); style = Paint.Style.STROKE; strokeWidth = 7f
        strokeJoin = Paint.Join.ROUND; strokeCap = Paint.Cap.ROUND
    }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFB8C0C8.toInt() }
    private val pressedBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x22FFFFFF }

    init {
        setOnClickListener { onTap() }
        contentDescription = icon.name.lowercase()
    }

    override fun setPressed(pressed: Boolean) { super.setPressed(pressed); invalidate() }

    override fun onDraw(c: Canvas) {
        val cx = width / 2f; val cy = height / 2f
        if (isPressed) c.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 40f, 40f, pressedBg)
        when (icon) {
            NavIcon.HOME -> c.drawPath(Path().apply {
                moveTo(cx - 52, cy - 4); lineTo(cx, cy - 50); lineTo(cx + 52, cy - 4)
                moveTo(cx - 38, cy - 16); lineTo(cx - 38, cy + 46); lineTo(cx + 38, cy + 46); lineTo(cx + 38, cy - 16)
            }, stroke)
            NavIcon.APPS -> for (r in -1..1) for (col in -1..1) c.drawCircle(cx + col * 34f, cy + r * 34f, 9f, fill)
        }
    }
}
