package com.jreid.truckdash.climate

import com.szchoiceway.canbus.CarAirState
import org.junit.Assert.assertEquals
import org.junit.Test

class ClimateStoreTest {
    @Test fun sleep_forgets_everything_so_nothing_stale_shows_at_wake() {
        val store = ClimateStore()
        store.onAir(CarAirState().apply { bAirOn = true; m_byLeftTemp = "72°F"; m_byRighTemp = "" })
        store.onOutside("58°F")
        assertEquals("72°", store.current().driverTemp)
        assertEquals(58, store.current().outsideTempF)
        store.reset()
        assertEquals(ClimateState.UNKNOWN, store.current())
    }
}
