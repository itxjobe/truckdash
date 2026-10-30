package com.jreid.truckdash

enum class StartupEvent { ACC_ON, ACC_OFF }

/** Whether the dash should take the screen back after the firmware restores its last app. */
object StartupPolicy {
    fun shouldBringDashForward(event: StartupEvent, dashVisible: Boolean, reverse: Boolean): Boolean =
        event == StartupEvent.ACC_ON && !dashVisible && !reverse
}
