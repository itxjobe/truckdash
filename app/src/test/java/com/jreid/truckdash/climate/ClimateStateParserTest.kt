package com.jreid.truckdash.climate

import com.szchoiceway.canbus.CarAirState
import org.junit.Assert.assertEquals
import org.junit.Test

class ClimateStateParserTest {
    private fun air(f: CarAirState.() -> Unit) = CarAirState().apply {
        bAirOn = true; m_byLeftTemp = ""; m_byRighTemp = ""; byMaxFunStrengthStall = 8
    }.apply(f)

    @Test fun typical_manual_state() {
        val s = ClimateStateParser.parse(air {
            m_byLeftTemp = "72°F"; m_byRighTemp = "70°F"; byFunStrength = 3
            bAcOn = true; bOutCircleOn = true; bRearOn = true
        })
        assertEquals(ClimateState(driverTemp = "72°", passTemp = "70°", fan = "3", ac = true, auto = false,
            recirc = true, frontDefrost = false, rearDefrost = true, outsideTempF = null,
            leftSeatHeat = 0, rightSeatHeat = 0), s)
    }

    @Test fun lo_and_hi_are_upper_case() {
        val s = ClimateStateParser.parse(air { m_byLeftTemp = "Lo"; m_byRighTemp = "HI" })
        assertEquals("LO", s.driverTemp)
        assertEquals("HI", s.passTemp)
    }

    @Test fun auto_fan_shows_A() {
        assertEquals("A", ClimateStateParser.parse(air { bSmallAutoOn = true; byFunStrength = 4 }).fan)
        assertEquals("A", ClimateStateParser.parse(air { bFanAuto = true }).fan)
        assertEquals(true, ClimateStateParser.parse(air { bSmallAutoOn = true }).auto)
    }

    @Test fun fan_capped_at_max_stall() {
        assertEquals("8", ClimateStateParser.parse(air { byFunStrength = 12 }).fan)
    }

    @Test fun system_off_hides_temps_and_says_off() {
        val s = ClimateStateParser.parse(air { bAirOn = false; m_byLeftTemp = "72°F"; bAcOn = true })
        assertEquals(null, s.driverTemp)
        assertEquals("OFF", s.fan)
        assertEquals(false, s.ac)
    }

    @Test fun short_or_bad_payload_is_unknown() {
        assertEquals(ClimateState.UNKNOWN, ClimateStateParser.parse(null))
        val blank = ClimateStateParser.parse(air { m_byLeftTemp = null; m_byRighTemp = "" })
        assertEquals(null, blank.driverTemp)
        assertEquals(null, blank.passTemp)
    }

    @Test fun outside_temp_parses_f_and_c() {
        assertEquals(75, ClimateStateParser.outsideTempF("75°F"))
        assertEquals(75, ClimateStateParser.outsideTempF("24°C"))
        assertEquals(-4, ClimateStateParser.outsideTempF("-20°C"))
    }

    @Test fun outside_temp_absent_is_null() {
        for (v in listOf(null, "", "--", "--°F", "garbage")) assertEquals(null, ClimateStateParser.outsideTempF(v))
    }

    @Test fun seat_heat_levels_pass_through_and_survive_hvac_off() {
        val s = ClimateStateParser.parse(air { bAirOn = false; bLeftSeatHotLevel = 3; bRightSeatHotLevel = 1 })
        assertEquals(3, s.leftSeatHeat)
        assertEquals(1, s.rightSeatHeat)
    }

    @Test fun seat_heat_out_of_range_is_clamped() {
        assertEquals(3, ClimateStateParser.parse(air { bLeftSeatHotLevel = 9 }).leftSeatHeat)
        assertEquals(0, ClimateStateParser.parse(air { bRightSeatHotLevel = -1 }).rightSeatHeat)
    }
}
