# Reverse signal (from decompiled eventcenter, EventService.java ~695-763)

- Enter reverse: eventcenter broadcasts (to all users) `com.choiceway.eventcenter.EventUtils.MCU_MSG_BACKCAR_START`,
  and sets system property `sys.backcar.state=1`.
- Leave reverse: broadcasts `com.choiceway.eventcenter.EventUtils.MCU_MSG_BACKCAR_END`, sets `sys.backcar.state=0`.
- Exception (not this truck): UI 44/138 with the Console app on top uses an in-page backcar instead.

TruckDash: dynamic receiver for both actions; read `sys.backcar.state` on start for the initial value.
