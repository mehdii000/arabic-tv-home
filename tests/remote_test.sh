#!/usr/bin/env bash
# Remote-control regression for the launcher, driven over adb with key events only.
# Usage: [YOUTUBE_PKG=com.google.android.youtube.tv] tests/remote_test.sh SERIAL OUTDIR
# YOUTUBE_PKG must match the package the APK was built for (build.py --youtube-package).
# Intended for an emulator, or a TV you own after you have installed the launcher yourself.
# It installs nothing and changes no settings; it presses keys and reads UI state.
set -u
S=$1; OUT=$2; mkdir -p "$OUT"
A=(adb -s "$S"); PKG=local.reroute.youtubehome; YT=${YOUTUBE_PKG:-com.google.android.youtube.tv}
pass=0; fail=0
log(){ echo "$*" | tee -a "$OUT/remote-test.txt"; }
resumed(){ "${A[@]}" shell dumpsys activity activities | grep -m1 mResumedActivity | sed 's/.*u0 \([^ ]*\).*/\1/'; }
focused(){ "${A[@]}" shell uiautomator dump /data/local/tmp/ui.xml >/dev/null 2>&1; "${A[@]}" exec-out cat /data/local/tmp/ui.xml > "$OUT/ui.xml"; python3 -I - "$OUT/ui.xml" <<'EOF'
import re,sys
s=open(sys.argv[1],encoding='utf-8').read()
f=[m for m in re.finditer(r'<node [^>]*>',s) if 'focused="true"' in m.group(0)]
for m in f:
    g=m.group(0); t=re.search(r' text="([^"]*)"',g).group(1); d=re.search(r'content-desc="([^"]*)"',g).group(1)
    print((t or d or '?')+' '+re.search(r'bounds="([^"]*)"',g).group(1))
print('focusables=%d'%len(re.findall(r'focusable="true"',s)))
EOF
}
check(){ if [ "$2" = "$3" ] || [[ "$2" == *"$3"* ]]; then log "PASS $1"; pass=$((pass+1)); else log "FAIL $1 (got: $2 | want: $3)"; fail=$((fail+1)); fi; }
check_exact(){ if [ "$2" = "$3" ]; then log "PASS $1"; pass=$((pass+1)); else log "FAIL $1 (got: $2 | want: $3)"; fail=$((fail+1)); fi; }
key(){ "${A[@]}" shell input keyevent "$@"; sleep 0.8; }
shot(){ "${A[@]}" exec-out screencap -p > "$OUT/$1.png"; }
: > "$OUT/remote-test.txt"
log "device: $("${A[@]}" shell getprop ro.product.model) / Android $("${A[@]}" shell getprop ro.build.version.release) / $("${A[@]}" shell wm size | tr -d '\r') / $("${A[@]}" shell wm density | tr -d '\r')"
log "launcher: $("${A[@]}" shell dumpsys package $PKG | grep -m1 versionName | tr -d ' \r')"

key KEYCODE_HOME; sleep 2
check "HOME shows launcher" "$(resumed)" "$PKG/.HomeActivity"
F=$(focused); check "YouTube action focused on entry" "$F" "يوتيوب"; check_exact "exactly one focusable control" "$(printf '%s\n' "$F" | sed -n 's/^focusables=//p')" "1"
shot 01-home-activity
for k in DPAD_UP DPAD_DOWN DPAD_LEFT DPAD_RIGHT DPAD_LEFT DPAD_UP; do key KEYCODE_$k; done
F=$(focused); check "arrows keep focus on YouTube" "$F" "يوتيوب"
key KEYCODE_BACK; check "BACK stays on Home" "$(resumed)" "$PKG/.HomeActivity"

key KEYCODE_MENU; sleep 1; check "single MENU: no maintenance" "$(focused)" "يوتيوب"
key KEYCODE_MENU KEYCODE_MENU; sleep 1; check "double MENU: no maintenance" "$(focused)" "يوتيوب"
sleep 2.5
"${A[@]}" shell input keyevent --longpress KEYCODE_MENU; sleep 1; check "held MENU: no maintenance" "$(focused)" "يوتيوب"
sleep 2.5
key KEYCODE_MENU KEYCODE_MENU
"${A[@]}" shell input keyevent --longpress KEYCODE_MENU; sleep 1
check "two taps then held MENU: no maintenance" "$(focused)" "يوتيوب"
sleep 2.5
key KEYCODE_MENU KEYCODE_DPAD_DOWN KEYCODE_MENU KEYCODE_MENU; sleep 1; check "MENU,arrow,MENU,MENU: sequence reset, no maintenance" "$(focused)" "يوتيوب"
sleep 2.5
key KEYCODE_MENU KEYCODE_MENU KEYCODE_MENU; sleep 1
F=$(focused); check "triple MENU opens maintenance with رجوع focused" "$F" "رجوع"
shot 02-maintenance
key KEYCODE_BACK; sleep 1; check "BACK closes maintenance, YouTube refocused" "$(focused)" "يوتيوب"

key KEYCODE_MENU KEYCODE_MENU KEYCODE_MENU; sleep 1
key KEYCODE_DPAD_RIGHT; check "RIGHT from رجوع stays on رجوع" "$(focused)" "رجوع"
key KEYCODE_DPAD_UP; check "UP stays inside the panel" "$(focused)" "رجوع"
key KEYCODE_DPAD_LEFT; check "LEFT moves to device settings" "$(focused)" "إعدادات الجهاز"
key KEYCODE_DPAD_LEFT; check "LEFT moves to weather refresh" "$(focused)" "تحديث الطقس"
key KEYCODE_DPAD_LEFT; check "LEFT at the end stays" "$(focused)" "تحديث الطقس"
key KEYCODE_DPAD_RIGHT; check "RIGHT back to device settings" "$(focused)" "إعدادات الجهاز"
log "Keeping existing device logs; reading launcher messages without clearing the buffer."
key KEYCODE_DPAD_CENTER; sleep 3
R=$(resumed); check "OK on device settings opens Settings" "$R" "settings"
"${A[@]}" logcat -d -s YouTubeHome:I | grep -E 'settings|maintenance' | tee -a "$OUT/remote-test.txt" >/dev/null
shot 03-device-settings
key KEYCODE_HOME; sleep 2; check "HOME from Settings returns to launcher" "$(resumed)" "$PKG/.HomeActivity"
check "YouTube focused again after returning" "$(focused)" "يوتيوب"

key KEYCODE_DPAD_CENTER; sleep 4
check "one OK opens YouTube ($YT)" "$(resumed)" "$YT"
shot 04-after-ok
key KEYCODE_HOME; sleep 2; check "HOME from YouTube returns to launcher" "$(resumed)" "$PKG/.HomeActivity"
check "YouTube focused on resume" "$(focused)" "يوتيوب"
log "RESULT pass=$pass fail=$fail"
"${A[@]}" shell rm -f /data/local/tmp/ui.xml
[ "$fail" = 0 ]
