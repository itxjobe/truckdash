# Climate keys and climate state (decompiled Console, canbus, CustomerUI; 2026-10-30)

Line references like `CON:857` point into jadx output of the Console app (`decomp/atslcarconsole/...`),
which is not in this repo: decompile the APK from your own unit to follow them.

Unit config: CanType 16, LogoType 349, UINumber 328, portrait. Not known yet: CustomerType,
AirSupplierID, CarYear (Sys_CarInfor_ID). They matter in a few places, noted below.

Paths:
- `CON` = `decomp/atslcarconsole/sources/com/android/atslcarconsole/MainActivity.java`
- `CUI` = `decomp/customerui/sources/com/szchoiceway/...` (vendor bottom bar, /product/app/CustomerUI)
- `CB` = canbus jadx output, NOT in the repo. Regenerate with
  `JAVA_HOME=$PWD/tools/jdk tools/jadx/bin/jadx -q -d <out> apks/com.szchoiceway.canbus.apk`.
  Line numbers below are from that default-mode output (`<out>/sources/com/szchoiceway/canbus/...`).
- JADX mangles `BuickClickEvent.onTouch` (shared "Code duplicated" blocks), so I re-ran it per class with
  `--single-class 'com.android.atslcarconsole.MainActivity$BuickClickEvent' -m fallback` (raw
  register/goto form). Key values in the table for onTouch buttons come from that output.

## Which code path applies to this unit

- Layout: CanType 16 + LogoType 349 picks `activity_main_silverado_7manual_22` if CarYear is 0 or 3,
  otherwise `activity_main_silverado_8auto_22` (CON:857-863). Both are in `res/layout-xhdpi*`.
  This branch comes before any AirSupplierID or UINumber check (the UINumber 44/138 and
  horizontal branches don't apply). Confidence: high.
- Listeners: `bindViewListenr` sends CanType 15/16/17/18/176/177 to `initBuickView()` (CON:1297-1300).
  That function puts `BuickClickEvent` on every climate button, using onTouch for the hold-to-repeat
  buttons (temp and fan) and onClick for the toggles (CON:1948-1983). No mCanType, mAirSupplierID or
  UINumber branch inside BuickClickEvent changes a climate key. The only conditional keys are
  btnPrev/btnNext/btnMedia (radio keys, LogoType 220/221/224/257-260 only). Confidence: high.
- The `chkAC`, `chkAuto`, `chkCarInnerLoop`, `chkFWndMaxDefrost`, `chkRWnd` and `chkDual` checkboxes
  belong to `initTDMondeoView` (Ford, CON:1829). They are never wired for CanType 16. In the Buick
  path the only wired checkboxes are `chkFWndDefrost` and the four seat checkboxes.

Frame sent (CON:2562 `sendBuickAirkey`): `2e e0 02 KEY STATE CS` with CS = (e0+02+KEY+STATE) xor ff.
`sendCanbusData` (CON:2808) wraps it as `0d 08 2e e0 02 KEY STATE CS`. It broadcasts
`com.szchoiceway.eventcenter.EventUtils.ACTION_MCU_CMD_EVENT` with byte[] extra `EventUtils.MCU_CMD_DATA`.
- Toggles (onClick): send STATE 1 and then STATE 0 back to back (CON:7084-7215).
- Hold buttons (onTouch): STATE 1 on ACTION_DOWN. After 400 ms, handler msg 508 sends STATE 2 and
  re-arms itself until ACTION_UP sends STATE 0 (CON:272-312). `arg2 == 1` means sendBuickCarKey
  (e1) and `arg2 == 2` means sendBuickAirkey (e0). In onTouch the default is 2, so all climate holds go
  to e0.
- Corroboration: the canbus app's own GM climate popup (`CB GMOld/BuickCanService.java:748-930`
  `btnBuickClickEvt`) sends the same key codes through an identical `2e e0 02` builder (CB
  GMOld/BuickCanService.java:2312).

## 1. Control to key code (sendBuickAirkey, 0xE0)

| Control | View id (icon) | Key dec | Key hex | Evidence | Confidence |
|---|---|---|---|---|---|
| Climate power | btnAirPower (sld_svg_fengshan_22) | 1 | 0x01 | CON:7091-7092; CB BuickCanService:778 case 0 | high |
| Driver temp down | btnLeftAirSub | 2 | 0x02 | onTouch, fallback dump `L1bf: r3=r13(2)`; CON:5956; CB case 4 | high |
| Driver temp up | btnLeftAirAdd | 3 | 0x03 | onTouch, default r3=3 to L1ce; CON:5907; CB case 3 | high |
| Passenger temp down | btnRightAirSub (8auto_22 only) | 4 | 0x04 | onTouch `La0: r3=4`; CB case 6 | high |
| Passenger temp up | btnRightAirAdd (8auto_22 only) | 5 | 0x05 | onTouch `La3: r3=5`; CON:6788; CB case 5 | high |
| Airflow face/vent | btnFanMid (sld_svg_renzhong) | 7 | 0x07 | CON:7119-7120; CB case 23 | high |
| Airflow floor | btnFanDown (sld_svg_renxia) | 8 | 0x08 | CON:7115-7116; CB case 24 | high |
| Fan down | btnFanSub | 9 | 0x09 | onTouch `L1c4: r3=9`; CON:5755; CB case 2 | high |
| Fan up | btnFanAdd | 10 | 0x0A | onTouch `L1c7: r3=10`; CON:5705; CB case 1 | high |
| Left seat heat | chkLeftSeatHot | 11 | 0x0B | CON:7200-7203; CB case 18 | high (not in the _22 layouts) |
| Left seat cool | chkLeftSeatCold | 12 | 0x0C | CON:7194-7197; CB case 17 | high (not in the _22 layouts) |
| Right seat heat | chkRightSeatHot | 13 | 0x0D | CON:7212-7215; CB case 20 | high (not in the _22 layouts) |
| Right seat cool | chkRightSeatCold | 14 | 0x0E | CON:7206-7209; CB case 19 | high (not in the _22 layouts) |
| Sync / dual | btnSync (8auto_22 only) | 16 | 0x10 | CON:7180-7181 | high |
| Windshield airflow (defog mode) | btnFWndDefrost (sld_svg_renshang), also chkFWndDefrost | 18 | 0x12 | CON:7107-7108, 7188-7191; CB case 22 | high for the code; medium that it is "defog mode" rather than max defrost (going by the icon) |
| Front defrost (max) | btnFWndMaxDefrost (sld_svg_qianchuang) | 19 | 0x13 | CON:7111-7112; CB case 15 | high |
| Rear defrost | btnRWnd (sld_svg_houchuang) | 20 | 0x14 | CON:7172-7173; CB case 16 | high |
| Auto | btnAuto (8auto_22 only) | 21 | 0x15 | CON:7095-7096; CB case 7 | high |
| A/C | btnAC | 23 | 0x17 | CON:7087-7088; CB case 8 | high |
| Recirculation | btnCarInnerLoop (sld_svg_neixunhuan) | 25 | 0x19 | CON:7103-7104; CB case 12 | high |
| Floor + defrost | btnFanUpDown (not in _22 layouts) | 32 | 0x20 | CON:7127-7128 | medium (btnAirOutletSub sends the same code from onTouch) |
| Bi-level | btnFanMidDown (not in _22 layouts) | 33 | 0x21 | CON:7123-7124 | medium (btnAirOutletAdd sends the same code from onTouch) |
| (unknown, canbus popup only) | AirButtonTag 9 | 24 | 0x18 | CB BuickCanService case 9 | low; maybe A/C max, unverified |

Layout contents: `7manual_22` has power, fan +/-, A/C, face, defog, recirc, floor, rear, max
defrost and driver temp +/- (single zone). `8auto_22` adds Auto, Sync and passenger temp +/-.
Non-climate keys on the same screen use sendBuickCarKey (0xE1): power-off/vol 1, home 2, fav 3,
prev 4, next 5, menu 6, phone 7, media 8, radio 9, back 10, tone 22.

## 2. Live climate state

### Primary source: parcelable broadcast (what the bottom bar and Console use)

- Action: `com.choiceway.canbus.carairstruct`
- Extra: `com.choiceway.canbus.carairstruct.airstate`, a Parcelable of class
  `com.szchoiceway.canbus.CarAirState`.
- Receivers: CUI `customerui/views/SystemUIView.java:161-165` (filter at :352), which drives
  `NaviBarView.refreshAirViewInfor` (CUI `customerui/views/NaviBarView.java:1286-1440`). Also CUI
  `base/BaseUIView.java:175-178` and CON:5382-5386 (filter CON:3322).
- Sent by: the canbus app with `sendBroadcastAsUser(..., ALL)` (CB `Base/BaseCanNew.java:1065-1067`,
  CB `carair/CarAirService.java:1953-1955`). eventcenter caches it and can re-send it
  (`decomp/eventcenter/.../EvtModel.java:772-773, 1179-1187`).
- To unparcel it, an app needs a class with exactly the name `com.szchoiceway.canbus.CarAirState`
  and the same field order. Otherwise reading any extra throws BadParcelableException. The Console,
  CustomerUI and canbus copies write identical sequences (diffed). Parcel order is in
  `decomp/atslcarconsole/sources/com/szchoiceway/canbus/CarAirState.java:328-398` (read side
  :251-320). In order:
  bAirOn, bAcOn, bOutCircleOn, bBigAutoOn, bSmallAutoOn, bDualOn, bMaxFrontOn, bRearOn,
  bFunDirectHead, bFunDirectLevel, bFunDirectFoot, bAcMax (each one byte, as writeByte),
  byFunStrength (int), m_byLeftTemp (String), m_byRighTemp (String), m_byTempUnit (int), bRearLock,
  bHuaFenOn (bytes), byLeftColdLevel, byRightColdLevel (ints), bAQSInCircle (byte),
  bLeftSeatHotLevel, bRightSeatHotLevel, byMaxFunStrengthStall (ints), bFunStrengthAuto,
  bSmallAutoHide, bDualHide, bFrontHotOn, bNanoeOn, bFanAuto, bFanDirection (bytes), m_OutSideTemp
  (int), bShowLeftAQS, bShowRightAQS (bytes), byLeftAQSLevel, byRightAQSLevel (ints), bECOOn
  (byte), byAirMode (int), mAirData (byteArray), mAirDataWithRear (byteArray), byRearFunStrength
  (int), m_byRearLeftTemp, m_byRearRighTemp (Strings), bRearAutoOn, bRearAirOn,
  bRearFunDirectLevel, bRearFunDirectFoot, bFunDirectHeadFoot, bFunDirectLevelFoot, bAjustAirSoft,
  bAjustAirFast, bAjustAirAutoInOut, bMaxFrontOnHide, bTempLevel (bytes), bMaxFanLevel (int),
  bRearFunDirectHead, bAutoBaiDong, bRequestShowAirInfo, bWheelHeat, bLeftAuto, bRightAuto,
  bLeftFULL, bRightFULL, bMONO, bFunDirectHead_Right, bFunDirectLevel_Right,
  bFunDirectFoot_Right, bESP, bDoorLock (bytes), bRearMaxFanLevel (int).

How the consumers read the parcel fields:
- Temps: `m_byLeftTemp` and `m_byRighTemp` are ready-to-show strings, for example "72°F", "22.0°C",
  localized "LO"/"HI" (`R.string.lbl_lo`/`lbl_hi`), or "" when the system is off or the value is
  invalid. CUI only parses them for the ticker animation, via `EventUtils.isTextValidDecimal` and
  `getFloatFromTemperatureString` (CUI NaviBarView.java:1338-1350). The Console reformats them only
  when they don't end in F (CON:3655-3670, EventUtils.LoadTempText).
- Fan: CUI shows `byFunStrength` capped at `byMaxFunStrengthStall`. It shows "A" when
  `bSmallAutoOn || bFunStrengthAuto || bFanAuto` (CUI NaviBarView.java:1313-1337).
- Airflow: CUI marks face if `bFunDirectLevel || bFunDirectLevelFoot`, floor if
  `bFunDirectFoot || bFunDirectLevelFoot || bFunDirectHeadFoot`, and windshield if
  `bFunDirectHead || bFunDirectHeadFoot` (CUI NaviBarView.java:1408-1422). The Console lights
  btnFanMid for level only, btnFanDown for foot only and btnFWndDefrost for head only (CON:3721-3747).
- Toggles: A/C = bAcOn, recirc = bOutCircleOn (despite the name, it lights the recirc button),
  max front defrost = bMaxFrontOn, rear defrost = bRearOn, Auto = bSmallAutoOn, power = bAirOn
  (CUI NaviBarView.java:1301-1306, 1431). The Console shows Sync as `!bDualOn` (CON:3716-3720).

### How canbus fills it for CanType 16 (byte offsets)

Which canbus service runs depends on CustomerType (CB `CanApp.java` onCreate. The fallback dump
shows that for CanType 16 the code reaches `if (mCustomerType == 4) new BuickCanService` and
otherwise, unless AirSupplierID 18 + Logo 221 or Logo 7/8 Cadillac, it runs
`FordCanParase + CarAirService`). BuickCanService has a dedicated 349 layout
(`activity_main_silverado_hi_22`, CB `GMOld/BuickCanService.java:488`) and special 349 handling, so
**BuickCanService (CustomerType 4) is the likely path. Medium confidence; needs a device check.**

Both paths use the same 8-byte payload d0..d7. For BuickCanService, the MCU frame is
`2e 03 LEN d0 d1 ... CS`, with d0 = frame[3] (CB `GMOld/BuickCanService.java:1228-1236, 1502-1541`;
framing header 0x2E, checksum = (sum of bytes 1..n-2) xor 0xFF, CB `CanbusRecDataService.java:357-367`).

| Field | BuickCanService rule (CB GMOld/BuickCanService.java) | CarAirService rule (CB carair/CarAirService.java) |
|---|---|---|
| power | d0 & 0x80 (:2383) | d0 & 0x80 (:1611) |
| A/C | d0 & 0x40 (:2388) | power && d0 & 0x40 (:1618) |
| recirc (bOutCircleOn) | d0 & 0x20 (:2408) | d0 & 0x20, cleared if d0 & 0x08 (:1633-1642) |
| auto recirc (bAQSInCircle) | d0 & 0x08 (:2413) | d0 & 0x08 |
| rear defrost | d0 & 0x10 (:2396) | d0 & 0x10 (:1623) |
| fan level | (d0 & 0x07) plus 8 if d1 & 0x10 (max stall becomes 8) (:2419-2423) | same, forced to 0 if power is off (:1644-1657) |
| fan auto | d6 & 0x01 sets bFanAuto (:2424) | d6 & 0x01 sets bFunStrengthAuto |
| dual | d1 & 0x20 (:2434) | power && d1 & 0x20 (:1659) |
| ECO | d1 & 0x40 (:2439) | n/a |
| airflow mode d1 & 0x0F | 0 off, 1 auto, 2 max defrost (bMaxFrontOn), 3 foot, 4 level+foot, 5 level, 6 head+level, 7 head, 8 head+foot, 9 all (:2444-2510) | same, except 8 = foot only (:1669-1730) |
| Auto (bSmallAutoOn) | for Logo 349: true only when (d1 & 0x0F) == 1 (:2448-2466). d6 & 0x02 (:2401) is overridden | power && d6 & 0x02 (:1628) |
| front defrost | bMaxFrontOn = (d1 & 0x0F) == 2 | same |
| temp unit flag | u = (d6 & 0x04) >> 2 (:2529) | none (always °C codes) |
| driver temp | d2: 0 = LO, 30 = HI, 255 = "". If u == 1: plain integer, shown as d2 + "°F". If u == 0: 1..28 gives (d2*5+165)/10 °C (17.0..30.5), 29 = 16.0, 31 = 16.5, 32 = 15.0, 33 = 15.5, 34 = 31.0, then converted to °F when the Sys_Tmp_Unit_set (Celsius) flag is false (:2549-2687) | d2 uses the °C table only, plus 35 = 31.5. Power off gives "" (:1752-1855) |
| passenger temp | d3, same rules (:2690-2734) | d3, same (:1894-1937) |
| seat heat | d4 >> 4 left, d4 & 0x0F right (:2736-2739) | same (:1941-1942) |
| seat vent | d7 >> 4 left, d7 & 0x0F right | same |
| power off | clears A/C, dual, ECO, both temp strings (:2745-2753) | same (:1946-1952) |

Outside temperature (not carried in CarAirState for these paths; `m_OutSideTemp` stays 0):
- BuickCanService: frame[8] (= d5) minus 40. The unit is °F if frame[9] & 0x04 (= d6 bit 2), else °C.
  Values 0x80 and 0xFF mean "--". It is broadcast only on change, as action
  `com.choiceway.eventcenter.CanUtils.CAN_CAR_OUT_SIDE_TEMP_EVT` with String extra
  `com.choiceway.eventcenter.CanUtils.CAN_CAR_OUT_SIDE_TEMP_EVT_EXTRA_STR`, for example "75°F"
  (CB GMOld/BuickCanService.java:1527-1537).
- BuickCanService has a second source, MCU cmd 0x31 (`2e 31 LEN ...`, onCanCarVehicleInfoEvt): frame[5]
  is sign-magnitude °C (bit 7 = minus, low 7 bits = value). It is converted to the user unit with
  LoadTempText and sent on the same action and extra (CB GMOld/BuickCanService.java:1354-1368). Which
  of the two the truck actually sends is unknown.
- CUI listens for the same action and extra (CUI `customerui/base/BaseMenuUIView.java:367-368`, filter :530).
- eventcenter relays it (only if `Sys_Show_Temp_key_set` is true) as
  `com.szchoiceway.SEND_APP_ACTION_EVT` with String extra `CarOutsideTemp`
  (`decomp/eventcenter/.../EvtModel.java:532-540`, `EventService.java:14719-14770`).
- The CarAirService/FordCanParase path sends no outside temperature.
- Since the broadcast only fires on change, a late subscriber gets nothing until the temperature
  changes. Consider also listening to SEND_APP_ACTION_EVT.

### Secondary raw source (only on the CarAirService path)

FordCanParase broadcasts `com.szchoiceway.eventcenter.EventUtils.MCU_CAR_CANBUS_AIR_INFO` (and
`...MCU_CAR_AIR_INFO`) with byte[] extra `EventUtils.CAR_AIR_DATA` = d0..d6/7/8, from MCU cmd 0x13
(CB `CanParase/FordCanParase.java` parseAirCmdEvt; decompile with `-m simple`). I found no
equivalent raw broadcast from BuickCanService.

### SystemUI

`apks/com.android.systemui.apk` decompiles (with JAVA_HOME set) but contains no climate code: no
`carairstruct` string in either dex. The live bottom-bar climate comes from CustomerUI, as described above.

## Open questions (need an on-device capture)

1. CustomerType / AirSupplierID / CarYear: run `logcat -s CanApp BuickCanService CarAirService FordCanParase`
   (CanApp logs "onCreate mCustomerType:" and "mCarType = ..., mAirSupplierID = ..."). This settles
   which decoder runs, and whether CarYear gives the 7manual or 8auto layout.
2. Temp encoding in practice: does d6 bit 2 (°F flag) arrive set, so temps come as integer °F, and
   what do LO/HI look like? A few BuickCanService logs, or one dumped CarAirState, would settle it.
3. Key 18 vs 19 on this truck: which one is "defog mode" and which is "max defrost"? Also key 24.
4. Whether keys 7/8/18 toggle a mode or cycle it (GM trucks have a single mode button).
5. Whether the hold-repeat STATE 2 is needed, or STATE 1 then 0 per step is enough for temp/fan.
