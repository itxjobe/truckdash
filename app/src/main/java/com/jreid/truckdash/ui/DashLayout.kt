package com.jreid.truckdash.ui

/** Plain rect in window px (android.graphics.Rect is a stub in JVM tests). */
data class Box(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val width get() = right - left
    val height get() = bottom - top
    fun grow(by: Int) = Box(left - by, top - by, right + by, bottom + by)
}

/** Design B geometry for the 1440x1640 window above the nav bar (spec: video 1200x720 at x=120, y=350). */
object DashLayout {
    const val WINDOW_W = 1440
    const val WINDOW_H = 1640
    const val VIDEO_RADIUS = 32

    val video = Box(120, 350, 1320, 1070)
    val bezelOuter = video.grow(24)
    val bezelInner = video.grow(14)
    val clockBlock = Box(0, 60, WINDOW_W, 300)
    val climateBar = Box(150, 1150, 1290, 1350)
    val seatLeft = Box(150, 1390, 620, 1560)
    val seatRight = Box(820, 1390, 1290, 1560)

    /** The dash's own bottom bar, drawn where the hidden system bar was (screen is 1440x1920). */
    val navBar = Box(0, WINDOW_H, WINDOW_W, 1920)
    val homeButton = Box(440, 1680, 680, 1880)
    val appsButton = Box(760, 1680, 1000, 1880)
}
