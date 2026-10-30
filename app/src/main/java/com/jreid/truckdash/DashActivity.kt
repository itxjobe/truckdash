package com.jreid.truckdash

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import com.jreid.truckdash.climate.BarAction
import com.jreid.truckdash.climate.ClimateBar
import com.jreid.truckdash.climate.ClimateKeys
import com.jreid.truckdash.climate.SeatHeatButton
import com.jreid.truckdash.climate.ClimateMonitor
import com.jreid.truckdash.climate.ClimateSheet
import com.jreid.truckdash.mcu.McuPackets
import com.jreid.truckdash.mcu.McuSender
import com.jreid.truckdash.ui.BezelBackground
import com.jreid.truckdash.ui.ClockView
import com.jreid.truckdash.ui.DashLayout
import com.jreid.truckdash.ui.NavButton
import com.jreid.truckdash.ui.NavIcon
import com.jreid.truckdash.video.AisFeed
import com.jreid.truckdash.video.FeedController
import com.jreid.truckdash.video.MainScheduler
import com.jreid.truckdash.video.ReverseMonitor
import com.jreid.truckdash.video.TouchMapper
import com.jreid.truckdash.video.VideoPane

class DashActivity : Activity() {
    private val feed by lazy {
        val mcu = McuSender(this)
        FeedController(AisFeed { mcu.sendRaw(it) }, MainScheduler())
    }
    private val scheduler = MainScheduler()
    private lateinit var reverse: ReverseMonitor
    private lateinit var acc: AccMonitor
    private lateinit var climate: ClimateMonitor
    private lateinit var touch: TouchMapper
    private var visible = false
    private var inReverse = false
    private val reclaim = Runnable {
        if (StartupPolicy.shouldBringDashForward(StartupEvent.ACC_ON, visible, inReverse)) {
            Log.i(AisFeed.TAG, "taking the screen back after wake")
            startActivity(Intent(this, DashActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()
        val root = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        root.addView(BezelBackground(this), FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        val c = DashLayout.clockBlock
        val clock = ClockView(this)
        root.addView(clock, FrameLayout.LayoutParams(c.width, c.height).apply { leftMargin = c.left; topMargin = c.top })
        val sender = McuSender(this)
        val pressKey = { key: Int ->
            sender.send(McuPackets.climateKey(key, true))
            sender.send(McuPackets.climateKey(key, false))
        }
        val sheet = ClimateSheet(this, pressKey)
        val bar = ClimateBar(this) { a ->
            when (a) {
                is BarAction.Key -> pressKey(a.code)
                BarAction.OpenSheet -> sheet.show()
            }
        }
        val cb = DashLayout.climateBar
        root.addView(bar, FrameLayout.LayoutParams(cb.width, cb.height).apply { leftMargin = cb.left; topMargin = cb.top })
        val seatL = SeatHeatButton(this, "DRIVER") { pressKey(ClimateKeys.LEFT_SEAT_HEAT) }
        val seatR = SeatHeatButton(this, "PASSENGER") { pressKey(ClimateKeys.RIGHT_SEAT_HEAT) }
        for ((v, b) in listOf(seatL to DashLayout.seatLeft, seatR to DashLayout.seatRight)) {
            root.addView(v, FrameLayout.LayoutParams(b.width, b.height).apply { leftMargin = b.left; topMargin = b.top })
        }
        climate = ClimateMonitor(this) { st ->
            bar.state = st; sheet.render(st); clock.outsideTempF = st.outsideTempF
            seatL.level = st.leftSeatHeat; seatR.level = st.rightSeatHeat
        }
        touch = TouchMapper(sender::send, MainScheduler())
        val pane = VideoPane(this, touch)
        pane.feedListener = feed::onSurface
        val v = DashLayout.video
        root.addView(pane, FrameLayout.LayoutParams(v.width, v.height).apply { leftMargin = v.left; topMargin = v.top })
        for ((box, icon) in listOf(DashLayout.homeButton to NavIcon.HOME, DashLayout.appsButton to NavIcon.APPS)) {
            root.addView(NavButton(this, icon) { onNav(icon) },
                FrameLayout.LayoutParams(box.width, box.height).apply { leftMargin = box.left; topMargin = box.top })
        }
        root.addView(sheet, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        setContentView(root)
        climate.start()
        AutoStartSlot.claim(this)
        reverse = ReverseMonitor(this) { r -> inReverse = r; feed.onReverse(r) }.also { it.start() }
        acc = AccMonitor(this) { e ->
            scheduler.cancel(reclaim)
            if (e == StartupEvent.ACC_ON) {
                feed.onWake()
                scheduler.postDelayed(RECLAIM_AFTER_WAKE_MS, reclaim)
            } else {
                touch.reset()
                feed.onSleep()
                climate.reset()
            }
        }.also { it.start() }
    }

    override fun onResume() { super.onResume(); visible = true; hideSystemBars(); feed.onResume() }

    override fun onWindowFocusChanged(hasFocus: Boolean) { super.onWindowFocusChanged(hasFocus); if (hasFocus) hideSystemBars() }

    /** Hide the vendor bar on the dash only; it comes back in every other app. Swipe up reveals it. */
    @Suppress("DEPRECATION")
    private fun hideSystemBars() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }

    private fun onNav(icon: NavIcon) = when (icon) {
        NavIcon.HOME -> Unit // already home
        NavIcon.APPS -> sendBroadcast(Intent(ACTION_ALL_APPS).putExtra("zxw_Launcher", "AppList"))
    }
    override fun onPause() { visible = false; touch.reset(); feed.onPause(); super.onPause() }
    override fun onDestroy() { touch.reset(); scheduler.cancel(reclaim); acc.stop(); climate.stop(); reverse.stop(); feed.onPause(); super.onDestroy() }

    companion object {
        /** Same broadcast the vendor bar's grid button ends in (eventcenter SRC_APPLIST). */
        const val ACTION_ALL_APPS = "ZXW_ACTION_LAUNCHER_ALLAPPS_START_EVT"
        /** Firmware restores the last app 5.5 s into wake, ~3 s after the ACC broadcast. */
        const val RECLAIM_AFTER_WAKE_MS = 5000L
    }
}
