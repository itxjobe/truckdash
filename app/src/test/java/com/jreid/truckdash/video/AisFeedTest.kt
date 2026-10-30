package com.jreid.truckdash.video

import android.graphics.SurfaceTexture
import android.view.Surface
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/** The JVM has no vendor library, which is exactly the "built without libcamera_utils.so" case. */
class AisFeedTest {
    @Test fun missing_vendor_library_does_not_crash_open() {
        val feed = AisFeed { }
        assertFalse(feed.open(Surface(null as SurfaceTexture?)))
    }

    @Test fun missing_vendor_library_reports_no_frames_and_closes_quietly() {
        val feed = AisFeed { }
        assertEquals(0, feed.frameCount())
        feed.close()
    }
}
