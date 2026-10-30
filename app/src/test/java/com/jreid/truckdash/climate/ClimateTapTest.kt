package com.jreid.truckdash.climate

import org.junit.Assert.assertEquals
import org.junit.Test

class ClimateTapTest {
    private val tap = ClimateTap()
    private fun gesture(x0: Int, y0: Int, x1: Int, y1: Int): BarAction? { tap.down(x0, y0, 1140, 200); return tap.up(x1, y1, 1140, 200) }

    @Test fun tap_in_place_acts() {
        assertEquals(BarAction.Key(ClimateKeys.FAN_UP), gesture(570, 50, 575, 60))
    }

    @Test fun slide_from_fan_onto_driver_temp_sends_nothing() {
        assertEquals(null, gesture(570, 100, 100, 40))
    }

    @Test fun slide_from_top_to_bottom_half_of_a_temp_sends_nothing() {
        assertEquals(null, gesture(100, 40, 100, 160))
    }

    @Test fun up_without_down_sends_nothing() {
        assertEquals(null, tap.up(570, 50, 1140, 200))
    }
}
