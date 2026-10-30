package com.jreid.truckdash.climate

sealed class BarAction {
    data class Key(val code: Int) : BarAction()
    object OpenSheet : BarAction()
}

/** Five equal slots: driver temp, A/C, fan, mode, passenger temp. Temps and fan: top half up, bottom half down. */
object ClimateBarHit {
    const val SLOTS = 5

    fun actionAt(x: Int, y: Int, w: Int, h: Int): BarAction? {
        if (x < 0 || x >= w || y < 0 || y >= h) return null
        val up = y < h / 2
        return when (x * SLOTS / w) {
            0 -> BarAction.Key(if (up) ClimateKeys.DRIVER_UP else ClimateKeys.DRIVER_DOWN)
            1 -> BarAction.Key(ClimateKeys.AC)
            2 -> BarAction.Key(if (up) ClimateKeys.FAN_UP else ClimateKeys.FAN_DOWN)
            3 -> BarAction.OpenSheet
            else -> BarAction.Key(if (up) ClimateKeys.PASS_UP else ClimateKeys.PASS_DOWN)
        }
    }
}
