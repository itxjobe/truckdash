package com.jreid.truckdash.climate

/** What the climate bar shows. Null means unknown and is hidden, never shown as a dash or zero. */
data class ClimateState(
    val driverTemp: String?,
    val passTemp: String?,
    val fan: String?,
    val ac: Boolean?,
    val auto: Boolean?,
    val recirc: Boolean?,
    val frontDefrost: Boolean?,
    val rearDefrost: Boolean?,
    val outsideTempF: Int?,
    val leftSeatHeat: Int? = null,
    val rightSeatHeat: Int? = null,
) {
    companion object {
        val UNKNOWN = ClimateState(null, null, null, null, null, null, null, null, null)
    }
}
