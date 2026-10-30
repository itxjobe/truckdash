# How it works

A map of the ZXW/Choiceway head unit, written so you can build your own dash. Everything here was
found by decompiling the vendor apps from a unit (jadx) and watching logcat while the stock apps ran.
Values are from the tested unit in the README; check them on yours.

Everything is reachable from a normal, unprivileged app. No root.

## The pieces on the unit

| Package | What it does |
|---|---|
| `com.android.atslcarconsole` | Stock "Console" app: factory radio video plus climate panel. The reference for almost everything below. |
| `com.szchoiceway.eventcenter` | System service. Owns the MCU link, ignition (ACC) handling, reverse, auto-start. |
| `com.szchoiceway.canbus` | Talks to the truck's CAN adapter; broadcasts climate state. |
| `com.szchoiceway.providers.settings` | `SysVarProvider`, the vendor settings store. |
| `com.szchoiceway.customerui` / SystemUI | The vendor status and bottom bars. |

## 1. Factory video

The factory radio screen arrives as 800x480 video through an LT8918 bridge into Qualcomm AIS
(`ais_server`). It is not an Android camera. The Console app reaches it through the vendor JNI
library `libcamera_utils.so`, which sits on the public library list, so any app can load it.

- JNI binds by class name, so your app needs a class `com.szchoiceway.camera.CameraUtils` with the
  native methods, and an interface `com.szchoiceway.camera.CameraCallBack`
  (`app/src/main/java/com/szchoiceway/camera/`). Without the callback interface `JNI_OnLoad` fails.
- Open: `setSurface(surface, 0)`, `setSensorOutputFmt(1)`, `openCamera(0)`.
- Close: `closeCamera()`, `deleteSurface(0)`.
- `/sys/lt8918/lt8918_signal` reads `1` when the radio is sending a picture.
- Only one client can hold the input. Close it before the reverse camera or the Console needs it.
- After ignition off and on, the input needs the same preparation the Console does: see
  [startup-signals.md](notes/startup-signals.md#video-after-wake).

Code: `video/Feed.kt`, `video/FeedController.kt` (open, retry, watchdog), `video/VideoPane.kt`.

## 2. Sending commands to the truck

Every command goes through eventcenter as a broadcast:

```
action: com.szchoiceway.eventcenter.EventUtils.ACTION_MCU_CMD_EVENT
extra:  EventUtils.MCU_CMD_DATA  (byte[])
```

- CAN frames are prefixed with `0d 08`. Raw MCU commands (like `49 14 01`) are sent without it.
- CAN frame format: `2e CMD LEN payload... CS`, where CS = (sum of CMD, LEN and payload) xor `ff`.

| What | CMD | Payload |
|---|---|---|
| Touch on the radio screen | `e3` | down (1) or up (0), then x and y in 800x480 source pixels, each 2 bytes little endian |
| Climate key | `e0` | key code, state (1 press, 0 release, 2 held-repeat) |
| Car key (radio keys) | `e1` | key code, state |

Climate key codes for the GM path (CanType 16):

| Control | Key | | Control | Key |
|---|---|---|---|---|
| Power | 1 | | Seat heat driver | 11 |
| Driver temp down / up | 2 / 3 | | Seat heat passenger | 13 |
| Passenger temp down / up | 4 / 5 | | Sync | 16 |
| Airflow face | 7 | | Windshield airflow | 18 |
| Airflow floor | 8 | | Max front defrost | 19 |
| Fan down / up | 9 / 10 | | Rear defrost | 20 |
| A/C | 23 | | Auto | 21 |
| Recirculation | 25 | | | |

The Console sends toggles as press then release back to back, and holds temp and fan with press,
repeat (state 2) every 400 ms, then release. The dash sends every tap as press then release.

To see what your own unit sends, open the Console app, tap a control and watch logcat for
`EventService: SRX sendData =0x0d 0x0a ...` (eventcenter's log of the bytes it actually sends).

Code: `mcu/McuPackets.kt`, `mcu/McuSender.kt`, `video/TouchMapper.kt`, `climate/ClimateKeys.kt`.
Full evidence and unknowns: [notes/climate-keys.md](notes/climate-keys.md).

## 3. Reading climate state

- The canbus app broadcasts `com.choiceway.canbus.carairstruct` with a Parcelable extra of class
  `com.szchoiceway.canbus.CarAirState`. To receive it, your app needs a class with exactly that name
  that reads the record in the same order. `CarAirState.java` here lists the 68-value layout and keeps
  only what the dash shows.
- Temperatures arrive as strings (`"72°F"`, `"LO"`, `"HI"`); fan as a level plus a max.
- Outside temperature: `com.choiceway.eventcenter.CanUtils.CAN_CAR_OUT_SIDE_TEMP_EVT`.

Code: `climate/ClimateMonitor.kt`, `climate/ClimateStateParser.kt`.

## 4. Ignition off and on

- Ignition off is a deep sleep. eventcenter kills every user app except a short fixed list.
- At wake, eventcenter relaunches the last source app, then the app in its auto-start slot.
- The dash puts itself in that slot on every launch, using the provider update eventcenter actually
  listens to.
- The dash is also the HOME app, so a cold boot lands on it.

Details and the pitfalls that cost the most time: [notes/startup-signals.md](notes/startup-signals.md).

## 5. Reverse camera

- Enter: broadcast `com.choiceway.eventcenter.EventUtils.MCU_MSG_BACKCAR_START`, and
  `sys.backcar.state=1`.
- Leave: `...MCU_MSG_BACKCAR_END`, `sys.backcar.state=0`.
- The dash closes the video input on enter so the vendor camera can take it, and reopens on leave.

Code: `video/ReverseMonitor.kt`. Notes: [notes/reverse-signal.md](notes/reverse-signal.md).

## 6. The bottom bar

The vendor bar cannot be trimmed, so the dash hides system bars and draws its own.
See [notes/bottom-bar.md](notes/bottom-bar.md).

## Adapting to another unit or vehicle

1. **Pull and decompile your Console app** (`adb shell pm path com.android.atslcarconsole`, then
   jadx). Search its `MainActivity` for `mCanType`, `mLogoType`, `sendBuickAirkey`, `setLT8918State`
   and `RequestCar8836LVDS`. Different CanType values take different code paths with different key
   codes and frame formats.
2. **Check the video path.** If your unit has no `/sys/lt8918/` it may use a different bridge
   (the Console checks `mIsLT9211Camera`). Copy whatever your Console does before `openCamera`.
3. **Check the climate codes** by tapping each control in the Console app and reading the `SRX`
   lines. Update `climate/ClimateKeys.kt`, then add your CanType to `UnitCheck.kt` (the vendor
   setting `Sys_CarType`; the dash logs it at start as `TruckDash: CanType=..`). Until then the dash
   sends no car commands on your unit.
4. **Check the screen size.** Layout is in `ui/DashLayout.kt` (1440x1920 portrait). The video window
   position and size are there too.
5. **Run the tests** (`scripts/dev.sh test`), install, and try every control with the truck parked.
