package com.jreid.truckdash.mcu

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class McuPacketsTest {
    private fun bytes(vararg v: Int) = ByteArray(v.size) { v[it].toByte() }
    private fun cs(vararg v: Int) = (v.sum() xor 0xff) and 0xff

    @Test fun touch_down_frame() =
        assertArrayEquals(
            bytes(0x2e, 0xe3, 0x05, 0x01, 0x1f, 0x03, 0xdf, 0x01, cs(0xe3, 0x05, 0x01, 0x1f, 0x03, 0xdf, 0x01)),
            McuPackets.touch(799, 479, true),
        )

    @Test fun touch_clamps_to_800x480() =
        assertArrayEquals(McuPackets.touch(799, 479, true), McuPackets.touch(5000, 900, true))

    @Test fun touch_coordinates_little_endian() {
        val p = McuPackets.touch(0x123, 0x45, false)
        assertEquals(0x00, p[3].toInt())
        assertEquals(0x23, p[4].toInt() and 0xff)
        assertEquals(0x01, p[5].toInt())
        assertEquals(0x45, p[6].toInt())
        assertEquals(0x00, p[7].toInt())
    }

    @Test fun checksum_is_sum_of_bytes_1_to_7_xor_ff() {
        val p = McuPackets.touch(10, 20, true)
        assertEquals((p.slice(1..7).sumOf { it.toInt() and 0xff } xor 0xff).toByte(), p[8])
    }

    // Console GM path sendBuickAirkey: 2e e0 02 <key> <state> <cs>. FAN_ADD is key 10 there.
    @Test fun climate_key_frame() =
        assertArrayEquals(bytes(0x2e, 0xe0, 0x02, 0x0a, 0x01, cs(0xe0, 0x02, 0x0a, 0x01)), McuPackets.climateKey(10, true))

    @Test fun climate_key_release_frame() =
        assertArrayEquals(bytes(0x2e, 0xe0, 0x02, 0x0a, 0x00, cs(0xe0, 0x02, 0x0a, 0x00)), McuPackets.climateKey(10, false))

    // Same checksum rule reproduces the radio-key packet actually captured on the truck:
    // "sendCanbusData:0x2e 0xe1 0x02 0x09 0x01 0x12" (sendBuickCarKey)
    @Test fun checksum_matches_captured_truck_packet() =
        assertEquals(0x12, cs(0xe1, 0x02, 0x09, 0x01))

    @Test fun wrap_prefixes_0d_08() =
        assertArrayEquals(bytes(0x0d, 0x08, 0x2e), McuPackets.wrap(bytes(0x2e)))
}
