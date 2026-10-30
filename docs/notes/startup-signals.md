# Start-up and sleep behavior

How the ZXW/Choiceway firmware (eventcenter) handles ignition, and what the dash does about it.
Found from the decompiled `com.szchoiceway.eventcenter` app plus logcat on a running unit.

## Ignition off (ACC sleep)

- ACC off is a deep sleep, not a reboot. Uptime keeps counting.
- At sleep, eventcenter force-stops every user app (`AccEvent` / `Utils.killapps`). The only
  exemptions are launcher3, `com.choiceway.logcapture` and `com.fibocom.multicamera`. No setting
  adds others, so any custom app is dead at wake and never hears the wake broadcast.

## Ignition on (ACC wake)

- eventcenter broadcasts `com.szchoiceway.eventcenter.EventUtils.ACTION_ACC_SLEEP_STATUS_EVT` with
  `ACC_Status=1` (sleep sends `0`).
- About 5.5 s later it restores the last source mode (`HANDLER_ACC_SLEEP_LAST_MODE`). Mode 30
  (SRC_CARCONSOLE) relaunches the stock Console app.
- Right after, `runAutoStartOpenAcc` launches the app in the vendor auto-start slot. This is how the
  dash comes up.

## The auto-start slot

- Stored in `content://com.szchoiceway.eventcenter.SysVarProvider/SysVar` as
  `SET_AutoStartPackageName_KEY` and `SET_AutoStartClassName_KEY`.
- The provider (package `com.szchoiceway.providers.settings`) only notifies eventcenter
  (`.../update/[]KEY=VALUE`) when the update uses `"keyname=?"` with selection args. eventcenter's
  cache refreshes only from that notice.
- Writing it from the shell with `content update --where "keyname='X'"` changes the database but not
  the cache. At the next wake eventcenter sees an empty slot, decides it is invalid and writes ""
  back over it.
- Fix: the dash writes both keys itself on every launch, the right way (`AutoStartSlot.kt`).

## Video after wake

The radio video input comes back dead after sleep. The dash repeats what the Console app does
before every open (`video/Feed.kt`):

1. Raw MCU command `49 14 01` (RequestCar8836LVDS), then wait 400 ms.
2. Write the stock LT8918 bridge timing to `/sys/lt8918/lt8918`:
   `hfp=20,hs=4,hbp=3,hact=800,vfp=6,vs=8,vbp=8,vact=480,rd_dly=552`.
   The Console app writes exactly this string on every open for this unit config. Sleep wipes it,
   and without it the input opens fine but delivers zero frames.
3. `setSurface`, `setSensorOutputFmt(1)`, `openCamera(0)`.

The dash only ever writes the vendor's own string, never custom values. If your unit's Console
writes something different (look for log tag `CamerasSignalDetection`, line `setLT8918State`), use
yours.

## Debugging tips

- Use `grep -a` on logcat dumps. The vendor camera library logs a garbage byte string, which makes
  plain grep treat the whole dump as binary and print nothing.
- Frames flowing show up as `zxw_ais_camera: fibo 111 ...` lines, about 60 per second.
- eventcenter logs what it actually sends to the MCU as `EventService: SRX sendData =0x0d 0x0a ...`.
