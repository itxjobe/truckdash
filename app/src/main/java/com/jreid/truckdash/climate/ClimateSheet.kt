package com.jreid.truckdash.climate

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.TextView

/** Full climate controls over the dash. Tap outside the panel to close. */
class ClimateSheet(context: Context, private val send: (Int) -> Unit) : FrameLayout(context) {
    private val buttons = linkedMapOf(
        "AUTO" to ClimateKeys.AUTO, "SYNC" to ClimateKeys.SYNC, "RECIRC" to ClimateKeys.RECIRC,
        "FACE" to ClimateKeys.MODE_FACE, "FLOOR" to ClimateKeys.MODE_FLOOR, "WINDSHIELD" to ClimateKeys.MODE_WINDSHIELD,
        "MAX DEFROST" to ClimateKeys.FRONT_DEFROST, "REAR DEFROST" to ClimateKeys.REAR_DEFROST, "POWER" to ClimateKeys.POWER,
    )
    private val views = mutableMapOf<Int, TextView>()

    init {
        setBackgroundColor(0xB0000000.toInt())
        setOnClickListener { hide() }
        val grid = GridLayout(context).apply {
            columnCount = 3
            background = GradientDrawable().apply { setColor(0xFF12161B.toInt()); cornerRadius = 48f; setStroke(2, 0xFF262D36.toInt()) }
            setPadding(30, 30, 30, 30)
            isClickable = true // swallow taps inside the panel
        }
        for ((name, key) in buttons) {
            val b = TextView(context).apply {
                text = name; gravity = Gravity.CENTER; textSize = 0f; setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, 30f)
                setTextColor(0xFFCFD6DE.toInt()); paint.isFakeBoldText = true
                setOnClickListener { send(key) }
            }
            views[key] = b
            grid.addView(b, GridLayout.LayoutParams().apply { width = 340; height = 170; setMargins(12, 12, 12, 12) })
        }
        addView(grid, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER))
        render(ClimateState.UNKNOWN)
        visibility = GONE
    }

    fun show() { visibility = VISIBLE }
    fun hide() { visibility = GONE }

    fun render(s: ClimateState) {
        val on = mapOf(ClimateKeys.AUTO to s.auto, ClimateKeys.RECIRC to s.recirc,
            ClimateKeys.FRONT_DEFROST to s.frontDefrost, ClimateKeys.REAR_DEFROST to s.rearDefrost)
        for ((key, v) in views) {
            val lit = on[key] == true
            v.background = GradientDrawable().apply {
                cornerRadius = 28f
                setColor(if (lit) 0xFF0F2A44.toInt() else 0xFF1B2027.toInt())
                setStroke(2, if (lit) 0xFF1F6FB8.toInt() else 0xFF2A313A.toInt())
            }
            v.setTextColor(if (lit) 0xFF7CC0FF.toInt() else 0xFFCFD6DE.toInt())
        }
    }
}
