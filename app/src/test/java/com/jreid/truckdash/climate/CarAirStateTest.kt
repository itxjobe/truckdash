package com.jreid.truckdash.climate

import com.szchoiceway.canbus.CarAirState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CarAirStateTest {
    /** Plays back one value per LAYOUT entry, chosen by name, and counts what was read. */
    private class Fake(private val values: Map<String, Any>) : CarAirState.Source {
        var index = 0
        private fun next(): Any = CarAirState.LAYOUT[index++].substring(2).let { values[it] ?: 0 }
        override fun readByte() = ((next() as? Boolean)?.let { if (it) 1 else 0 } ?: 0).toByte()
        override fun readInt() = next() as? Int ?: 0
        override fun readString() = next() as? String ?: ""
    }

    @Test fun record_is_68_values_long() = assertEquals(68, CarAirState.LAYOUT.size)

    @Test fun reads_every_value_so_the_parcel_stays_in_step() {
        val src = Fake(emptyMap())
        CarAirState.read(src)
        assertEquals(68, src.index)
    }

    @Test fun shown_fields_land_in_the_right_place() {
        val s = CarAirState.read(Fake(mapOf(
            "bAirOn" to true, "bAcOn" to true, "bRearOn" to true, "byFunStrength" to 5,
            "m_byLeftTemp" to "72°F", "m_byRighTemp" to "LO", "bLeftSeatHotLevel" to 2,
            "bRightSeatHotLevel" to 3, "byMaxFunStrengthStall" to 8,
        )))
        assertTrue(s.bAirOn); assertTrue(s.bAcOn); assertTrue(s.bRearOn)
        assertFalse(s.bOutCircleOn); assertFalse(s.bFanAuto)
        assertEquals(5, s.byFunStrength); assertEquals(8, s.byMaxFunStrengthStall)
        assertEquals("72°F", s.m_byLeftTemp); assertEquals("LO", s.m_byRighTemp)
        assertEquals(2, s.bLeftSeatHotLevel); assertEquals(3, s.bRightSeatHotLevel)
    }
}
