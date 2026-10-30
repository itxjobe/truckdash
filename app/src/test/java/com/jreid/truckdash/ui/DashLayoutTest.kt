package com.jreid.truckdash.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DashLayoutTest {
    private val v = DashLayout.video

    @Test fun video_is_exact_1_5x_true_shape() {
        assertEquals(1200, v.width)
        assertEquals(720, v.height)
        assertEquals(v.width * 480, v.height * 800)
    }

    @Test fun video_centered() = assertEquals(v.left, DashLayout.WINDOW_W - v.right)

    @Test fun video_matches_approved_mockup_position() = assertEquals(350, v.top)

    @Test fun bezel_rings_wrap_video() {
        assertEquals(Box(96, 326, 1344, 1094), DashLayout.bezelOuter)
        assertEquals(Box(106, 336, 1334, 1084), DashLayout.bezelInner)
    }

    @Test fun nothing_overlaps() {
        assertTrue(DashLayout.clockBlock.bottom < DashLayout.bezelOuter.top)
        assertTrue(DashLayout.bezelOuter.bottom + 40 <= DashLayout.climateBar.top)
        assertTrue(DashLayout.climateBar.bottom <= DashLayout.WINDOW_H)
    }

    @Test fun own_bar_fills_the_strip_the_system_bar_used() {
        assertEquals(Box(0, 1640, 1440, 1920), DashLayout.navBar)
    }

    @Test fun home_and_apps_sit_inside_the_bar_without_overlap_and_are_easy_to_hit() {
        val h = DashLayout.homeButton; val a = DashLayout.appsButton; val bar = DashLayout.navBar
        for (b in listOf(h, a)) {
            assertTrue(b.left >= bar.left && b.right <= bar.right && b.top >= bar.top && b.bottom <= bar.bottom)
            assertTrue(b.width >= 200 && b.height >= 160)
        }
        assertTrue(h.right <= a.left)
    }

    @Test fun seat_buttons_sit_between_climate_and_nav_bar() {
        for (b in listOf(DashLayout.seatLeft, DashLayout.seatRight)) {
            assertTrue(b.top >= DashLayout.climateBar.bottom + 20 && b.bottom <= DashLayout.navBar.top - 20)
            assertTrue(b.height >= 140 && b.width >= 300)
        }
        assertTrue(DashLayout.seatLeft.right < DashLayout.seatRight.left)
        assertEquals(DashLayout.climateBar.left, DashLayout.seatLeft.left)
        assertEquals(DashLayout.climateBar.right, DashLayout.seatRight.right)
    }
}
