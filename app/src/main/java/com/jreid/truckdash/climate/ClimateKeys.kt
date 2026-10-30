package com.jreid.truckdash.climate

/** sendBuickAirkey codes for the GM path (CanType 16), from docs/notes/climate-keys.md. */
object ClimateKeys {
    const val POWER = 1
    const val DRIVER_DOWN = 2
    const val DRIVER_UP = 3
    const val PASS_DOWN = 4
    const val PASS_UP = 5
    const val MODE_FACE = 7
    const val MODE_FLOOR = 8
    const val FAN_DOWN = 9
    const val FAN_UP = 10
    const val LEFT_SEAT_HEAT = 11
    const val RIGHT_SEAT_HEAT = 13
    const val SYNC = 16
    const val MODE_WINDSHIELD = 18
    const val FRONT_DEFROST = 19
    const val REAR_DEFROST = 20
    const val AUTO = 21
    const val AC = 23
    const val RECIRC = 25
}
