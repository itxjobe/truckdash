package com.jreid.truckdash.video

import android.view.MotionEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeScheduler : Scheduler {
    var now = 0L
    private val pending = mutableListOf<Pair<Long, Runnable>>()
    override fun postDelayed(ms: Long, r: Runnable) { pending += (now + ms) to r }
    override fun cancel(r: Runnable) { pending.removeAll { it.second === r } }
    fun advance(ms: Long) {
        val end = now + ms
        while (true) {
            val next = pending.filter { it.first <= end }.minByOrNull { it.first } ?: break
            pending.remove(next); now = next.first; next.second.run()
        }
        now = end
    }
}

class TouchMapperTest {
    private val sent = mutableListOf<ByteArray>()
    private val clock = FakeScheduler()
    private val mapper = TouchMapper({ sent += it }, clock)

    private fun ByteArray.down() = this[3].toInt() == 1
    private fun ByteArray.x() = (this[4].toInt() and 0xff) or ((this[5].toInt() and 0xff) shl 8)
    private fun ByteArray.y() = (this[6].toInt() and 0xff) or ((this[7].toInt() and 0xff) shl 8)

    @Test fun maps_view_to_800x480() {
        mapper.onEvent(MotionEvent.ACTION_DOWN, 1200f, 720f, 1200, 720)
        mapper.onEvent(MotionEvent.ACTION_DOWN, 600f, 360f, 1200, 720)
        assertEquals(listOf(799 to 479, 399 to 239), sent.map { it.x() to it.y() })
    }

    @Test fun down_then_hold_resends_every_500ms() {
        mapper.onEvent(MotionEvent.ACTION_DOWN, 100f, 100f, 1200, 720)
        clock.advance(1100)
        assertEquals(listOf(true, true, true), sent.map { it.down() })
    }

    @Test fun cancel_and_exit_send_up() {
        mapper.onEvent(MotionEvent.ACTION_DOWN, 100f, 100f, 1200, 720)
        mapper.onEvent(MotionEvent.ACTION_CANCEL, 100f, 100f, 1200, 720)
        clock.advance(2000)
        assertEquals(listOf(true, false), sent.map { it.down() })

        sent.clear()
        mapper.onEvent(MotionEvent.ACTION_DOWN, 100f, 100f, 1200, 720)
        mapper.onEvent(MotionEvent.ACTION_MOVE, 1300f, 100f, 1200, 720) // finger slid off the video
        mapper.onEvent(MotionEvent.ACTION_MOVE, 1350f, 100f, 1200, 720)
        mapper.onEvent(MotionEvent.ACTION_UP, 1350f, 100f, 1200, 720)
        clock.advance(2000)
        assertEquals(listOf(true, false), sent.map { it.down() })
    }

    @Test fun up_releases_at_last_position() {
        mapper.onEvent(MotionEvent.ACTION_DOWN, 600f, 360f, 1200, 720)
        mapper.onEvent(MotionEvent.ACTION_UP, 600f, 360f, 1200, 720)
        assertEquals(false, sent.last().down())
        assertEquals(399 to 239, sent.last().x() to sent.last().y())
    }

    @Test fun reset_while_held_sends_up_and_stops_repeat() {
        mapper.onEvent(MotionEvent.ACTION_DOWN, 100f, 100f, 1200, 720)
        mapper.reset() // window lost with no CANCEL (reverse, wake)
        clock.advance(3000)
        assertEquals(listOf(true, false), sent.map { it.down() })
        mapper.reset()
        assertEquals(2, sent.size)
    }
}
