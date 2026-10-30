package com.jreid.truckdash.mcu

/**
 * Frames the vendor canbus service accepts, copied from ATSLCarConsole's GM (CanType 16) path.
 * Every frame is 0x2e, cmd, len, payload..., checksum where checksum = (sum of cmd..payload) xor 0xff.
 */
object McuPackets {
    private const val HEADER = 0x2e
    private const val CMD_TOUCH = 0xe3 // sendBuickTouchPos
    private const val CMD_AIR_KEY = 0xe0 // sendBuickAirkey
    const val SRC_W = 800
    const val SRC_H = 480

    /** Touch at source coords (clamped to 800x480), little-endian x and y. */
    fun touch(x: Int, y: Int, down: Boolean): ByteArray {
        val cx = x.coerceIn(0, SRC_W - 1)
        val cy = y.coerceIn(0, SRC_H - 1)
        return frame(CMD_TOUCH, if (down) 1 else 0, cx and 0xff, cx shr 8, cy and 0xff, cy shr 8)
    }

    /** Climate key press (pressed=true) or release. */
    fun climateKey(key: Int, pressed: Boolean): ByteArray =
        frame(CMD_AIR_KEY, key and 0xff, if (pressed) 1 else 0)

    /** Prefix the eventcenter expects before forwarding to the canbus MCU. */
    fun wrap(packet: ByteArray): ByteArray = byteArrayOf(0x0d, 0x08) + packet

    private fun frame(cmd: Int, vararg payload: Int): ByteArray {
        val body = intArrayOf(cmd, payload.size) + payload
        val checksum = (body.sum() xor 0xff) and 0xff
        return (intArrayOf(HEADER) + body + checksum).map { it.toByte() }.toByteArray()
    }
}
