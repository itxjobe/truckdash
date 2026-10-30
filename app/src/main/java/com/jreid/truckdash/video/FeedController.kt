package com.jreid.truckdash.video

import android.util.Log
import android.view.Surface

enum class FeedState { CLOSED, OPENING, LIVE, RETRY_WAIT, REVERSE }

/**
 * Owns when the single-client video input is held. Releases it for the reverse camera,
 * reopens after reverse ends, recovers from stalled frames and retries failed opens.
 */
class FeedController(
    private val feed: Feed,
    private val scheduler: Scheduler,
    private val settleMs: Long = LVDS_SETTLE_MS,
) {
    var state = FeedState.CLOSED
        private set

    private var surface: Surface? = null
    private var resumed = false
    private var reverse = false
    private var asleep = false
    private var backoffMs = BACKOFF_START_MS
    private var lastFrames = -1
    private var stillMs = 0L
    /** Some firmware reports 0 forever; only trust stalls once the counter has moved. */
    private var counterWorks = false

    private val reopen = Runnable { tryOpen() }
    private val finish = Runnable { finishOpen() }
    private val watchdog = object : Runnable {
        override fun run() {
            if (state != FeedState.LIVE) return
            if (!feed.hasSignal()) {
                Log.w(AisFeed.TAG, "signal lost, closing until it returns")
                feed.close()
                waitForSignal()
                return
            }
            val f = feed.frameCount()
            if (f != lastFrames) {
                if (!counterWorks) Log.i(AisFeed.TAG, "frame counter moving: $lastFrames -> $f")
                counterWorks = true; lastFrames = f; stillMs = 0
            }
            else if (counterWorks) stillMs += WATCH_MS
            if (stillMs >= STALL_MS) {
                Log.w(AisFeed.TAG, "frames stalled at $f, reopening")
                feed.close()
                state = FeedState.CLOSED
                tryOpen()
            } else scheduler.postDelayed(WATCH_MS, this)
        }
    }

    fun onSurface(s: Surface?) { surface = s; sync() }
    fun onResume() { resumed = true; sync() }
    fun onPause() { resumed = false; sync() }

    fun onReverse(active: Boolean) {
        if (active == reverse) return
        reverse = active
        if (active) {
            stop()
            state = FeedState.REVERSE
        } else {
            state = FeedState.CLOSED
            if (wanted()) { state = FeedState.RETRY_WAIT; scheduler.postDelayed(REVERSE_REOPEN_MS, reopen) }
        }
    }

    /** ACC off: the pipe does not survive deep sleep, so let go now. */
    fun onSleep() { asleep = true; sync() }

    /** ACC on: always start a fresh open; the signal gate waits for the radio. */
    fun onWake() {
        asleep = false
        stop()
        state = if (reverse) FeedState.REVERSE else FeedState.CLOSED
        sync()
    }

    private fun wanted() = resumed && surface != null && !reverse && !asleep

    private fun sync() {
        if (wanted()) {
            if (state == FeedState.CLOSED) { backoffMs = BACKOFF_START_MS; tryOpen() }
        } else {
            stop()
            state = if (reverse) FeedState.REVERSE else FeedState.CLOSED
        }
    }

    private fun stop() {
        scheduler.cancel(reopen)
        scheduler.cancel(finish)
        scheduler.cancel(watchdog)
        feed.close()
    }

    private fun tryOpen() {
        val s = surface
        if (!wanted() || s == null) return
        if (!feed.hasSignal()) { waitForSignal(); return }
        state = FeedState.OPENING
        feed.requestVideo()
        if (settleMs > 0) scheduler.postDelayed(settleMs, finish) else finishOpen()
    }

    private fun finishOpen() {
        val s = surface
        if (!wanted() || s == null || state != FeedState.OPENING) return
        if (feed.open(s)) {
            state = FeedState.LIVE
            backoffMs = BACKOFF_START_MS
            lastFrames = feed.frameCount()
            stillMs = 0
            scheduler.postDelayed(WATCH_MS, watchdog)
        } else {
            state = FeedState.RETRY_WAIT
            scheduler.postDelayed(backoffMs, reopen)
            backoffMs = (backoffMs * 2).coerceAtMost(BACKOFF_MAX_MS)
        }
    }

    private fun waitForSignal() {
        state = FeedState.RETRY_WAIT
        scheduler.postDelayed(SIGNAL_POLL_MS, reopen)
    }

    companion object {
        const val SIGNAL_POLL_MS = 1000L
        /** Console waits this long between the video request and openCamera. */
        const val LVDS_SETTLE_MS = 400L
        const val REVERSE_REOPEN_MS = 300L
        const val STALL_MS = 3000L
        const val WATCH_MS = 500L
        const val BACKOFF_START_MS = 1000L
        const val BACKOFF_MAX_MS = 8000L
    }
}
