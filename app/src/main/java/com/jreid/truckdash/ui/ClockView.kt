package com.jreid.truckdash.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.text.format.DateFormat
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Date

/** Big thin clock with an uppercase date line; outside temp is appended only when known. */
class ClockView(context: Context) : LinearLayout(context) {
    private val time = TextView(context).apply {
        typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, 150f)
        setTextColor(Color.parseColor("#e9edf2"))
        gravity = Gravity.CENTER
        includeFontPadding = false
    }
    private val date = TextView(context).apply {
        setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, 26f)
        setTextColor(Color.parseColor("#7d8794"))
        letterSpacing = 0.18f
        gravity = Gravity.CENTER
    }

    /** Outside temperature in °F, or null to hide it (never faked). */
    var outsideTempF: Int? = null
        set(value) { field = value; refresh() }

    private val tick = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) = refresh()
    }

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        addView(time)
        addView(date, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply { topMargin = 20 })
        refresh()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        context.registerReceiver(tick, IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK); addAction(Intent.ACTION_TIME_CHANGED); addAction(Intent.ACTION_TIMEZONE_CHANGED)
        })
        refresh()
    }

    override fun onDetachedFromWindow() {
        context.unregisterReceiver(tick)
        super.onDetachedFromWindow()
    }

    private fun refresh() {
        val now = Date()
        time.text = DateFormat.format(if (DateFormat.is24HourFormat(context)) "H:mm" else "h:mm", now)
        val day = DateFormat.format("EEEE, MMMM d", now).toString().uppercase()
        date.text = outsideTempF?.let { "$day  ·  $it°F" } ?: day
    }
}
