# Arabic TV Home — implementation

**Current build:** 3.0.2 / versionCode 6. This is Claude's 3.0 redesign with Codex's review fixes (3.0.1) and a final change of launch target (3.0.2), all on 2026-11-06. On the family X96Q it is built with `--youtube-package com.google.android.youtube.tx`, a separately packaged YouTube TV build described in the companion research repository. The source in this repository defaults to the stock `com.google.android.youtube.tv`. That target constant is the only line that differs from the deployed source.

The design reference is [DESIGN.md](DESIGN.md). Product intent is in [PRODUCT.md](PRODUCT.md). The brief Claude worked from is in [history/redesign-brief.md](history/redesign-brief.md). An earlier two-action design is superseded.

## Screen

One composition on a 1280×720 TV, about 5% overscan margins (56dp sides, 34dp top, 30dp bottom), laid out right-to-left:

- **Information band (top).** Right: a 120dp Latin-digit 24-hour clock, a thin rule, then greeting, Arabic weekday (bold) and Moroccan-Arabic date (`6 نونبر 2026`). Left: `طنجة`, a native weather symbol with the temperature, the condition, and a small `تحديث HH:mm · Open-Meteo` attribution.
- **Sky (middle).** Left open on purpose. The view across the Strait: sky gradient, sea, the Tangier headland and the far coast. The sun follows a daylight arc from right (east) to left (west), clamped at the horizon during twilight. The moon follows a decorative night arc, not a lunar ephemeris.
- **The one action (bottom centre).** An ivory card 540dp wide with an amber focus ring, resting on the horizon: the YouTube play mark, `يوتيوب`, `اضغطي للمشاهدة`, and a round `OK` badge echoing the remote's centre button. It is the only focusable view on the screen.

Text sizes follow the accessibility font scale up to 130% (the tested maximum). The clock is fixed in dp because it is already at maximum size. `ModelTest` checks ≥7:1 main-text and ≥4.5:1 muted-text contrast at base palette stops. These tests do not prove contrast for every composited pixel after glow, clouds and text alpha; actual captures were also inspected for readability. A light top shade and soft text shadows add margin.

## Background

`SkyView` paints once into a bitmap whenever the phase, weather, cloud cover or sun position changes (the position is quantised to 1/48 of the arc, about every 13 minutes in daytime). There is no render loop. A change cross-fades over 1.6s through `ValueAnimator`, which follows the system animator scale (off means instant). Offscreen or before attach it switches instantly, so the default state is always fully drawn.

- **Phase:** dawn, day, dusk, night. Forecast sunrise and sunset are used when they belong to today; otherwise they come from an **offline solar calculation for Tangier** (`Model.sunTimes`). Time of day stays correct without weather or Internet.
- **Weather** (WMO code → kind): clear, cloud (with cover 0.25/0.55/1 for codes 1/2/3), fog, rain (drizzle/showers named separately), snow, storm. Each changes the palette (`Model.skyPalette`) and adds one quiet layer: soft cloud banks, haze bands, static rain streaks (lighter over the text band), or snow dots. Rain at night and rain by day differ in palette and light.
- **Unknown weather** (loading, unavailable, stale, unsynchronised clock) is drawn as a neutral sky for the time of day, with no sun disc, clouds or precipitation. Weather is never implied.

## Time, weather and failure handling

- Activity-scoped `ar-MA-u-nu-latn` resources; the device locale (fr-FR on the family box) is untouched. All formatting uses `Africa/Casablanca` and `Locale.US` digits.
- **Unsynchronised clock:** before 2026-11-01Z the box clock is treated as not set. The screen shows `--:--` and `جارٍ ضبط الساعة…` and makes no weather claim. `ACTION_TIME_CHANGED`, `TIMEZONE_CHANGED` and `DATE_CHANGED` re-render immediately, re-read the cached forecast and request fresh weather.
- **Ticks** are aligned to the next minute boundary (at most 15s apart), and `TIME_TICK` also re-renders.
- **Weather:** HTTPS Open-Meteo for fixed Tangier coordinates, 8s timeouts, 64KB cap, no redirects, no API key, no device identifier. Results are accepted only if fresh (≤3h, not in the future) and plausible. Refresh runs every 30 min while visible. A failure retries after 15s, then 30, 60, 120, 240, capped at 300s.
- **Late Wi-Fi:** a dynamic `CONNECTIVITY_ACTION` receiver (no extra permission) retries as soon as the network changes. Hints are coalesced, survive an in-flight request, and are scheduled after a 5s minimum gap; the sticky initial broadcast is ignored. A clock-invalidated forecast does not inherit the 30-minute recent-success delay. Responses are revalidated on the main thread against the current clock before acceptance.
- **Messages:** first attempt `جارٍ تحميل الطقس…`; after a failure `الطقس غير متاح الآن` / `سيظهر عند عودة الإنترنت`. The message stays steady during later retries, without flicker.

## Remote behaviour

- The YouTube card is focused on create, resume, window focus and `onNewIntent` (HOME while already Home). All four arrows are consumed and only restore focus. OK/Enter opens YouTube once; `LaunchGuard` ignores a second OK within 1.5s. YouTube is launched in a new task via the leanback launch intent, with the normal launch intent as fallback, and an Arabic message if missing. There is no auto-launch at boot.
- BACK on Home stays Home.
- **Maintenance:** three distinct MENU releases within 2s (`Model.MenuGate`). A key repeat, a held release at/above the system long-press timeout, cancellation, any other key between presses, or a slow third press resets the gesture or does not count. Opens a custom Arabic panel (`Maintenance`) with **رجوع focused**, then `إعدادات الجهاز` and `تحديث الطقس` to its left in a closed focus loop (up/down stay put). MENU inside the panel is ignored. The panel shows version and clock/weather status for the helper.
- **Settings launch** (`HomeActivity.launchSettings`): `Settings.ACTION_SETTINGS`, then `com.android.tv.settings/.MainSettings`, then `com.android.settings/.Settings`, each with `FLAG_ACTIVITY_NEW_TASK` and checked with `resolveActivity`. The outcome is logged under tag `YouTubeHome`. If nothing launches, an inline Arabic error appears in the panel.

## Files

- `src/.../Model.java`: pure Java calendar, digits, clock validity, weather states, solar maths, palettes and contrast, MENU and launch gates
- `src/.../HomeScreen.java`: the view tree, separate from the Activity so it can be rendered offscreen
- `src/.../SkyView.java`: background
- `src/.../WeatherGlyph.java`: weather symbol
- `src/.../Maintenance.java`: hidden maintenance panel
- `src/.../HomeActivity.java`: lifecycle, receivers, weather fetch, keys, launches
- `src/.../WeatherClient.java`: bounded fixed-city HTTPS forecast
- `tests/ModelTest.java` (`tests/run_model_tests.sh`): host JVM tests. `tests/SkyReview.java` and `tests/HomePreview.java`: on-device offscreen renders plus JSON, safe-area, overlap and no-clipping checks. `tests/remote_test.sh`: key-event regression over adb.
- `preview.py`: compiles the harness and, with `--serial`, runs it through `app_process` from `/data/local/tmp`, then deletes it. Installs nothing.
- `build.py` and `verify_apk.py`: INTERNET only, no assets or native code. No font or image is bundled; system Roboto/Noto Naskh and drawn shapes are used.

## Verification record

Results recorded on 2026-11-06. Device evidence beyond the files in [evidence/](evidence/) and [screenshots/](screenshots/) is kept privately because it contains device identifiers and the viewer's YouTube recommendations.

**Emulator (Claude, during the redesign).** Android 10 Android TV x86 image (`sdk_google_atv_x86`), 1280×720 at density 213, stock `tvlauncher` disabled on the emulator only. `ModelTest` passed; build and verifier passed; `SkyReview` JSON checks and the 28-state sky sheet passed; 16 `HomePreview` states passed safe-area/overlap/no-clip; `remote_test.sh` passed 23/23 (the 24th check, held MENU after two taps, was added during review). Cold start with no cache and no network showed the unavailable message, retried at 15s spacing and showed weather 8s after the network returned ([evidence/cold-start-emulator.txt](evidence/cold-start-emulator.txt)). Font scale 1.3 checked live.

**Real X96Q (Codex review and deployment).**

- Codex's review (a different AI agent from the one that wrote 3.0, not a human or security review) accepted the visual design and found three material edge cases, all fixed in 3.0.1: queued weather-recovery hints, held-MENU rejection, and twilight sun continuity.
- `remote_test.sh`: 24/24 on the device for 3.0.2, including the actual `com.android.tv.settings/.MainSettings` round trip and one OK opening YouTube ([evidence/remote-test-x96q-3.0.2.txt](evidence/remote-test-x96q-3.0.2.txt)). That log came from the pre-export version of the script, which hard-coded the target as `com.google.android.youtube.tx` and labelled the check "one OK opens patched YouTube". The published script is the same apart from taking the target from `YOUTUBE_PKG`. The `YouTubeHome` log lines in the file include entries from an earlier session because the buffer was not cleared; they are informational, and pass/fail comes from the resumed activity.
- 19 real-view offscreen states and the 28-state sky sheet rendered on the X96Q itself, including maintenance at 130% text. Samples: [clear day](screenshots/render-03-day-clear.png), [rain by day](screenshots/render-06-day-rain.png), [weather unavailable](screenshots/render-12-weather-unavailable.png), [130% text, rainy night](screenshots/render-14-large-text-130-night-rain.png), [all sky states](screenshots/sky-states-render.png).
- **Clock path:** a bounded root-only diagnostic briefly set the box to its known unsynchronised 2025 date, then restored accurate time. The live Activity went from `--:--` to the correct clock and cached forecast ([screenshots/clock-unsynced-x96q.png](screenshots/clock-unsynced-x96q.png), [screenshots/clock-restored-x96q.png](screenshots/clock-restored-x96q.png)). The launcher itself has no root or clock permission.
- **Connectivity receipt:** a protected system connectivity broadcast reached the Activity with INTERNET as the only permission. This checked receiver delivery; it was not a physical Wi-Fi unplug/replug test.
- **Real reboot:** Home came back as the foreground app; an initial DNS failure at boot was followed by connectivity two seconds later and a weather update 13 seconds after that. Time synchronised, device locale unchanged, one OK opened YouTube.

**Limits.** Framebuffer captures cannot establish television colour calibration, overscan settings or readability from the sofa; that needs a person in the room. Palette contrast is checked at palette stops, not at every composited pixel. The moon is decorative.

## Updating and rollback

Android only accepts an update signed with the same key, and a version downgrade can be rejected even with `adb install -d`. Do not uninstall or clear the launcher to downgrade it. To roll back, rebuild the older source with a higher `versionCode` and the original key.
