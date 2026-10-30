package com.jreid.truckdash.climate

import org.junit.Assert.assertEquals
import org.junit.Test

class ClimateBarHitTest {
    // bar is 1140 x 200, five equal slots of 228
    private fun at(x: Int, y: Int) = ClimateBarHit.actionAt(x, y, 1140, 200)

    @Test fun top_half_of_a_temp_raises_it_bottom_half_lowers_it() {
        assertEquals(BarAction.Key(ClimateKeys.DRIVER_UP), at(100, 40))
        assertEquals(BarAction.Key(ClimateKeys.DRIVER_DOWN), at(100, 160))
        assertEquals(BarAction.Key(ClimateKeys.PASS_UP), at(1050, 40))
        assertEquals(BarAction.Key(ClimateKeys.PASS_DOWN), at(1050, 160))
    }

    @Test fun fan_steps_by_half() {
        assertEquals(BarAction.Key(ClimateKeys.FAN_UP), at(570, 50))
        assertEquals(BarAction.Key(ClimateKeys.FAN_DOWN), at(570, 150))
    }

    @Test fun ac_toggles_and_mode_opens_sheet() {
        assertEquals(BarAction.Key(ClimateKeys.AC), at(340, 100))
        assertEquals(BarAction.OpenSheet, at(800, 100))
    }

    @Test fun outside_the_bar_does_nothing() {
        assertEquals(null, at(-5, 100))
        assertEquals(null, at(1200, 100))
    }
}
