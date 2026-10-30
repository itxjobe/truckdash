package com.jreid.truckdash.mcu

import android.content.Context
import android.content.Intent

/** Sends frames to the vendor eventcenter, which forwards them to the canbus MCU. */
class McuSender(private val context: Context, private val carCommandsAllowed: Boolean = true) {
    /** A canbus frame (2e ...), forwarded through the MCU. Dropped on units the dash was not tested on. */
    fun send(packet: ByteArray) {
        if (carCommandsAllowed) sendRaw(McuPackets.wrap(packet))
    }

    /** A command for the MCU itself, no canbus wrapper. */
    fun sendRaw(command: ByteArray) {
        context.sendBroadcast(Intent(ACTION).putExtra(EXTRA, command))
    }

    companion object {
        const val ACTION = "com.szchoiceway.eventcenter.EventUtils.ACTION_MCU_CMD_EVENT"
        const val EXTRA = "EventUtils.MCU_CMD_DATA"
    }
}
