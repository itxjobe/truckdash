package com.jreid.truckdash

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StartupPolicyTest {
    @Test fun acc_on_brings_dash_when_hidden() {
        assertTrue(StartupPolicy.shouldBringDashForward(StartupEvent.ACC_ON, dashVisible = false, reverse = false))
    }

    @Test fun does_not_steal_from_reverse_camera() {
        assertFalse(StartupPolicy.shouldBringDashForward(StartupEvent.ACC_ON, dashVisible = false, reverse = true))
    }

    @Test fun no_op_when_dash_already_showing() {
        assertFalse(StartupPolicy.shouldBringDashForward(StartupEvent.ACC_ON, dashVisible = true, reverse = false))
    }

    @Test fun acc_off_never_brings_dash() {
        assertFalse(StartupPolicy.shouldBringDashForward(StartupEvent.ACC_OFF, dashVisible = false, reverse = false))
    }
}
