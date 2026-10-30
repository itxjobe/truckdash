package com.jreid.truckdash.climate

import com.szchoiceway.canbus.CarAirState
import kotlin.math.roundToInt

/** Reads the canbus app's CarAirState the same way the vendor bottom bar does (docs/notes/climate-keys.md). */
object ClimateStateParser {
    /** GM seat heaters have three levels. */
    const val SEAT_MAX = 3

    fun parse(air: CarAirState?): ClimateState {
        if (air == null) return ClimateState.UNKNOWN
        val left = air.bLeftSeatHotLevel.coerceIn(0, SEAT_MAX)
        val right = air.bRightSeatHotLevel.coerceIn(0, SEAT_MAX)
        if (!air.bAirOn) return ClimateState(null, null, "OFF", false, false, air.bOutCircleOn, false, air.bRearOn, null, left, right)
        val fan = when {
            air.bSmallAutoOn || air.bFunStrengthAuto || air.bFanAuto -> "A"
            air.byMaxFunStrengthStall > 0 -> air.byFunStrength.coerceIn(0, air.byMaxFunStrengthStall).toString()
            else -> air.byFunStrength.toString()
        }
        return ClimateState(
            driverTemp = temp(air.m_byLeftTemp),
            passTemp = temp(air.m_byRighTemp),
            fan = fan,
            ac = air.bAcOn,
            auto = air.bSmallAutoOn,
            recirc = air.bOutCircleOn,
            frontDefrost = air.bMaxFrontOn,
            rearDefrost = air.bRearOn,
            outsideTempF = null,
            leftSeatHeat = left,
            rightSeatHeat = right,
        )
    }

    /** "72°F" -> "72°", "Lo" -> "LO", "" -> null. */
    private fun temp(raw: String?): String? {
        val t = raw?.trim().orEmpty()
        if (t.isEmpty()) return null
        if (t.equals("lo", true) || t.equals("hi", true)) return t.uppercase()
        return t.removeSuffix("F").removeSuffix("C").let { if (it.endsWith("°")) it else "$it°" }
    }

    /** Outside temp broadcast string ("75°F", "24°C", "--") to whole °F, or null when unknown. */
    fun outsideTempF(raw: String?): Int? {
        val m = Regex("""^\s*(-?\d+(?:\.\d+)?)\s*°?\s*([FC])\s*$""", RegexOption.IGNORE_CASE).find(raw ?: return null) ?: return null
        val v = m.groupValues[1].toDouble()
        return if (m.groupValues[2].equals("C", true)) (v * 9 / 5 + 32).roundToInt() else v.roundToInt()
    }
}
