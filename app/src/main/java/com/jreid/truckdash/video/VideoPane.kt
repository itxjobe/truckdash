package com.jreid.truckdash.video

import android.annotation.SuppressLint
import android.content.Context
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.Surface
import android.widget.FrameLayout
import com.jreid.truckdash.ui.CornerMask

/** Factory video surface with rounded, trimmed edges. Touches go to [touchMapper]. */
@SuppressLint("ViewConstructor", "ClickableViewAccessibility")
class VideoPane(context: Context, touchMapper: TouchMapper) : FrameLayout(context) {
    var feedListener: (Surface?) -> Unit = {}

    init {
        val surface = SurfaceView(context)
        surface.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(h: SurfaceHolder) = feedListener(h.surface)
            override fun surfaceChanged(h: SurfaceHolder, f: Int, w: Int, hh: Int) {}
            override fun surfaceDestroyed(h: SurfaceHolder) = feedListener(null)
        })
        surface.setOnTouchListener { v, e ->
            touchMapper.onEvent(e.actionMasked, e.x, e.y, v.width, v.height)
            true
        }
        addView(surface, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(CornerMask(context, radius = 32f, inset = 2f), LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }
}
