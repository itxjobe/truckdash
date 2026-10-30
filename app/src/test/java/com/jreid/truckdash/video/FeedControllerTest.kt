package com.jreid.truckdash.video

import android.view.Surface
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeFeed : Feed {
    var opens = 0
    var closes = 0
    var openResult = true
    var frames = 0
    var isOpen = false
    var signal = true
    var requests = 0
    val events = mutableListOf<String>()
    override fun hasSignal(): Boolean = signal
    override fun requestVideo() { requests++; events += "request" }
    override fun open(surface: Surface): Boolean { opens++; events += "open"; isOpen = openResult; return openResult }
    override fun close() { if (isOpen) closes++; isOpen = false }
    override fun frameCount(): Int = frames
}

class FeedControllerTest {
    private val feed = FakeFeed()
    private val clock = FakeScheduler()
    private val ctl = FeedController(feed, clock, settleMs = 0)
    private val surface: Surface = Surface(null as android.graphics.SurfaceTexture?)

    private fun live() { ctl.onSurface(surface); ctl.onResume() }

    @Test fun opens_when_surface_and_resumed() {
        ctl.onSurface(surface)
        assertEquals(0, feed.opens)
        ctl.onResume()
        assertEquals(1, feed.opens)
        assertEquals(FeedState.LIVE, ctl.state)
    }

    @Test fun reverse_releases_then_reopens() {
        live()
        ctl.onReverse(true)
        assertEquals(1, feed.closes)
        assertEquals(FeedState.REVERSE, ctl.state)
        ctl.onReverse(false)
        clock.advance(299)
        assertEquals(1, feed.opens)
        clock.advance(1)
        assertEquals(2, feed.opens)
        assertEquals(FeedState.LIVE, ctl.state)
    }

    @Test fun reverse_at_start_never_opens() {
        ctl.onReverse(true)
        live()
        clock.advance(5000)
        assertEquals(0, feed.opens)
    }

    @Test fun stalled_frames_trigger_reopen() {
        feed.frames = 10
        live()
        clock.advance(1000); feed.frames = 20
        clock.advance(1000) // advancing: still live
        assertEquals(1, feed.opens)
        clock.advance(3000) // frames stuck at 20 for 3 s
        assertEquals(1, feed.closes)
        assertEquals(2, feed.opens)
    }

    @Test fun dead_frame_counter_never_triggers_reopen() {
        feed.frames = 0 // this unit's counter reports 0 forever while video plays
        live()
        clock.advance(30000)
        assertEquals(0, feed.closes)
        assertEquals(1, feed.opens)
    }

    @Test fun waits_for_signal_before_opening() {
        feed.signal = false // radio still booting after ACC on
        live()
        clock.advance(5000)
        assertEquals(0, feed.opens)
        feed.signal = true
        clock.advance(1000)
        assertEquals(1, feed.opens)
        assertEquals(FeedState.LIVE, ctl.state)
    }

    @Test fun signal_loss_while_live_closes_then_reopens_when_back() {
        live()
        feed.signal = false
        clock.advance(1000)
        assertEquals(1, feed.closes)
        clock.advance(5000)
        assertEquals(1, feed.opens)
        feed.signal = true
        clock.advance(1000)
        assertEquals(2, feed.opens)
    }

    @Test fun open_failure_retries_with_backoff() {
        feed.openResult = false
        live()
        assertEquals(FeedState.RETRY_WAIT, ctl.state)
        clock.advance(1000); assertEquals(2, feed.opens)
        clock.advance(2000); assertEquals(3, feed.opens)
        clock.advance(4000); assertEquals(4, feed.opens)
        clock.advance(8000); assertEquals(5, feed.opens)
        clock.advance(8000); assertEquals(6, feed.opens) // capped at 8 s
        feed.openResult = true
        clock.advance(8000)
        assertEquals(FeedState.LIVE, ctl.state)
    }

    @Test fun pause_closes() {
        live()
        ctl.onPause()
        assertEquals(1, feed.closes)
        assertEquals(FeedState.CLOSED, ctl.state)
        clock.advance(20000)
        assertEquals(1, feed.opens)
    }

    @Test fun acc_sleep_closes_and_wake_reopens_while_resumed() {
        live()
        ctl.onSleep()
        assertEquals(1, feed.closes)
        assertEquals(FeedState.CLOSED, ctl.state)
        clock.advance(10000)
        assertEquals(1, feed.opens)
        ctl.onWake()
        assertEquals(2, feed.opens)
        assertEquals(FeedState.LIVE, ctl.state)
    }

    // Real timing: ask the MCU for the radio's video, wait 400 ms like the Console app, then open.
    private val slow = FeedController(feed, clock)
    private fun liveSlow() { slow.onSurface(surface); slow.onResume() }

    @Test fun asks_radio_for_video_and_waits_before_opening() {
        liveSlow()
        assertEquals(1, feed.requests)
        assertEquals(0, feed.opens)
        clock.advance(FeedController.LVDS_SETTLE_MS)
        assertEquals(listOf("request", "open"), feed.events)
        assertEquals(FeedState.LIVE, slow.state)
    }

    @Test fun wake_asks_for_video_again() {
        liveSlow(); clock.advance(400)
        slow.onSleep(); slow.onWake()
        assertEquals(2, feed.requests)
        clock.advance(400)
        assertEquals(2, feed.opens)
    }

    @Test fun pause_during_settle_cancels_the_open() {
        liveSlow()
        slow.onPause()
        clock.advance(5000)
        assertEquals(0, feed.opens)
    }
}
