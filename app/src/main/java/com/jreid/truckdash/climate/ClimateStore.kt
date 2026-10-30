package com.jreid.truckdash.climate

import com.szchoiceway.canbus.CarAirState

/** Latest climate inputs. Both arrive only on change, so sleep clears them rather than show stale values. */
class ClimateStore {
    private var air: CarAirState? = null
    private var outsideF: Int? = null

    fun onAir(a: CarAirState?) { air = a }
    fun onOutside(raw: String?) { outsideF = ClimateStateParser.outsideTempF(raw) }
    fun reset() { air = null; outsideF = null }
    fun current(): ClimateState = ClimateStateParser.parse(air).copy(outsideTempF = outsideF)
}
