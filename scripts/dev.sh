#!/usr/bin/env bash
# Dev helper. Set TRUCK_ADB=ip:port (the wireless debugging port changes after every ignition cycle).
# Uses ./tools and ./platform-tools if present, otherwise your own JAVA_HOME, ANDROID_HOME and adb.
set -euo pipefail
cd "$(dirname "$0")/.."
[ -d tools/jdk ] && export JAVA_HOME=$PWD/tools/jdk
[ -d tools/android-sdk ] && export ANDROID_HOME=$PWD/tools/android-sdk
ADB_BIN=adb; [ -x platform-tools/adb ] && ADB_BIN=$PWD/platform-tools/adb
adb_() { "$ADB_BIN" -s "${TRUCK_ADB:?set TRUCK_ADB=ip:port}" "$@"; }
APK=app/build/outputs/apk/debug/app-debug.apk
LIB=app/src/main/jniLibs/arm64-v8a/libcamera_utils.so
for cmd in "$@"; do
  case "$cmd" in
    vendor-lib)  # copy the video library from YOUR head unit's own Console app (not redistributed here)
             tmp=$(mktemp -d); apk=$(adb_ shell pm path com.android.atslcarconsole | tr -d '\r' | sed 's/^package://')
             [ -n "$apk" ] || { echo "Console app (com.android.atslcarconsole) not found on the unit" >&2; exit 1; }
             adb_ pull "$apk" "$tmp/console.apk" >/dev/null
             mkdir -p "$(dirname "$LIB")"; unzip -p "$tmp/console.apk" lib/arm64-v8a/libcamera_utils.so > "$LIB"
             rm -rf "$tmp"; echo "wrote $LIB" ;;
    build)   [ -s "$LIB" ] || echo "warning: $LIB missing, the video window will stay black (run: scripts/dev.sh vendor-lib)" >&2
             ./gradlew -q assembleDebug ;;
    test)    ./gradlew -q testDebugUnitTest ;;
    install) [ -s "$LIB" ] || { echo "run: scripts/dev.sh vendor-lib  (first time only)" >&2; exit 1; }
             ./gradlew -q assembleDebug  # never install a stale APK
             adb_ install -r "$APK"
             adb_ shell cmd package set-home-activity com.jreid.truckdash/.DashActivity ;;
    uninstall) adb_ uninstall com.jreid.truckdash ;;
    log)     adb_ logcat -s TruckDash:* ;;
    shot)    shift; adb_ exec-out screencap -p > "${1:?shot needs a file}"; exit 0 ;;
    *)       echo "unknown: $cmd (vendor-lib build test install uninstall log shot)" >&2; exit 2 ;;
  esac
done
