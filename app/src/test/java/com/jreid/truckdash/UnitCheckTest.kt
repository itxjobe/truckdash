package com.jreid.truckdash

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UnitCheckTest {
    @Test fun tested_can_type_is_allowed() = assertTrue(UnitCheck.allowsCarCommands(16))
    @Test fun other_can_types_are_blocked() {
        assertFalse(UnitCheck.allowsCarCommands(15))
        assertFalse(UnitCheck.allowsCarCommands(0))
    }
    /** Unreadable means the vendor store did not answer; the check cannot judge, so it does not block. */
    @Test fun unreadable_can_type_is_allowed() = assertTrue(UnitCheck.allowsCarCommands(null))
}
