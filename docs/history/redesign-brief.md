> Historical brief given to Claude for the 3.0 redesign (2026-11-06). Reproduced verbatim except that local filesystem paths are replaced by placeholders and one device-access detail is removed. It describes the project as it stood *before* 3.0; the current behaviour is in [../IMPLEMENTATION.md](../IMPLEMENTATION.md).

I want you to take over the visual design and UX of the custom Android TV home screen we built for my grandma. Please read the actual project and implement a substantially better native interface. I’m delegating the design decisions to you. Codex will review your work and handle final device deployment afterward.

## The experience I want

My grandma should turn on the TV box, see a clear, beautiful Arabic home screen, and press OK once to open YouTube. That is the whole viewing flow. She should not need to choose between apps, navigate a dashboard, read instructions, or type a search before seeing something to watch.

There must be exactly ONE visible, focusable action: YouTube. Codex has already removed the Search button and installed that change. Do not bring Search back. Do not add favourites, categories, shortcut tiles, a carousel, a launcher app drawer, a visible Settings button, or other extra decisions. Time, date and weather are information, not interactive controls.

Make the screen feel thoughtfully designed, welcoming and polished. The current version is a functional starting point, not a visual direction you have to preserve. You can replace its composition, typography, colours, shapes and background treatment. Use the space confidently without filling it with clutter. This is a TV viewed from a sofa by an older person, so readability, calmness and immediate understanding matter more than visual novelty. It should still feel much better than a basic button on a background.

## Required content and behaviour

- Use natural, clear Arabic inside the launcher, with proper shaping, alignment and RTL handling.
- Always use digits 0123456789. Do not use ٠١٢٣٤٥٦٧٨٩ or ۰۱۲۳۴۵۶۷۸۹, including in the clock, date, temperature and status text.
- Show a large, readable time, the day written in Arabic, and an Arabic date. Existing code uses a 24-hour clock and Moroccan Arabic month names.
- Weather is for Tangier, Morocco — طنجة. Use the existing fixed-city weather integration; no GPS permission or location setup.
- Backgrounds must respond to BOTH weather AND time of day. A rainy night must differ from a rainy day. Cover clear/cloudy/rain/fog/snow/storm states as appropriate, plus dawn/day/dusk/night. Good contrast must survive every background.
- Prefer a coherent, attractive background treatment over a collection of obvious decorative shapes. Keep it smooth on this low-powered box. Avoid distracting or perpetual animation. If using any motion, respect the device animation settings and keep the default state fully visible.
- Do not invent weather values. Handle loading, unavailable service, stale forecasts, Wi-Fi arriving late at boot and a clock that has not synchronized yet. The clock and date should remain useful offline. Keep any weather attribution appropriate and unobtrusive.
- YouTube is focused when Home appears or resumes. One press of OK opens it. Directional buttons should not lead to irrelevant or invisible controls. Home returns to the launcher. Do not automatically launch YouTube at boot and skip the new home screen.
- Settings must be difficult to open accidentally but easy for me with the remote. The current approach is three distinct MENU presses within 2 seconds, followed by a maintenance dialog that defaults to Back. You can refine its presentation or make an equally deliberate remote-only method. A single accidental press or held-key repeat must not open Settings. Keep maintenance hidden from the normal viewing screen.
- Do NOT change the device language. Its locale is fr-FR. Arabic must be scoped to this app. Use Africa/Casablanca for local time and date.

## Working project

Project root:
<project root>

Main files:
- src/local/reroute/youtubehome/HomeActivity.java — native Activity, layout, focus, launch and hidden maintenance.
- src/local/reroute/youtubehome/SkyView.java — current native Canvas background.
- src/local/reroute/youtubehome/Model.java — Arabic calendar, digit conversion, weather/solar rules, retry delays and MENU sequence.
- src/local/reroute/youtubehome/WeatherClient.java — bounded HTTPS Open-Meteo request, forecast parsing and solar times.
- AndroidManifest.xml
- build.py and verify_apk.py
- tests/ModelTest.java and tests/SkyReview.java
- PRODUCT.md and IMPLEMENTATION.md

DESIGN.md and .impeccable/design.json document an earlier two-action version and are historical references. They are not binding. The current Java source, this prompt, and the single-action requirement take precedence. Earlier screenshots showing Search are also superseded.

Device: owner’s X96Q, Android 10, landscape 1280×720, physical density 213. This is a real native Java Android Views app built with the existing SDK, not a website. Keep the current stack; do not migrate it to a web wrapper, React, Compose or a new build system just for the redesign. Avoid introducing large dependencies or installing system packages. This machine is CachyOS/Arch; obey its AGENTS.md package rules.

App package: local.reroute.youtubehome. Current installed version: 2.1 / versionCode 3. Preserve the package identity and original signing key under private/. Never expose, replace or regenerate the key/password. Increment the version for the new build. Build with:
python3 -B build.py

The verifier checks the original signer fingerprint and currently allows only INTERNET permission, with no native payloads or bundled assets. Keep the narrow permission boundary. If a carefully chosen bundled font or image materially improves the result, update the build and verifier with an explicit asset allowlist; do not simply remove verification. No root, storage, microphone, location or new account access is needed in the launcher.

## Working behaviour to preserve

The official YouTube TV app is com.google.android.youtube.tv. Guest recommendations and playback now work without an account and were observed after a box reboot. Do not clear YouTube data, reset its guest identity/history, request sign-in, replace it, or try to implement a recommendation engine in the launcher. Its normal interface can remain in its current language; the Arabic requirement here is for Home.

Codex fixed the device clock, enabled automatic time and replaced an invalid NTP server with time.android.com. It synchronized successfully after reboot. Codex also removed the persistent vendor management app com.easycontrol.mdm for user 0 after it kept restarting despite being disabled. Preserve the cleaned package state and [device-access detail removed]. Do not change firmware, router configuration, system locale, Google-services state or other device settings.

Latest screenshot of the single-action Home:
<private evidence path omitted>

Other evidence:
<private evidence path omitted>

That directory contains guest-feed-postboot.png, normal/large-text examples, reboot state and a rain-solar-variants.png test render. The rain variants were rendered using the actual Android View offscreen; they are not production Activity screenshots. The original rollback APK/source are in rollback/.

## Scope, verification and handoff

Work LOCALLY for this redesign. Do not install your new build on the TV, reboot it or mutate its configuration. Codex will review and perform final deployment after I return. Existing screenshots are available for reference; if you can run a native emulator, use it for design evidence and clearly identify it. Do not claim a browser mockup is an Android screenshot.

Implement the real screen rather than stopping at design advice or a mockup. Test Arabic layout, Latin digits, contrast, focus, overscan margins and larger text. Preserve the model tests and add meaningful tests for any changed behaviour. Run the build and artifact verifier. Keep documentation aligned with the final implementation.

Two unfinished checks from Codex must remain visible in your handoff:
1. The triple-MENU maintenance dialog was verified, but a later automated D-pad attempt to enter actual device Settings failed with Home still resumed. The exact cause is unresolved. Inspect and improve the controller flow locally; do not claim the complete device Settings round trip has passed.
2. Startup weather retry/cache handling was just improved: 15-second initial retry with bounded exponential backoff, and cached weather is reconsidered after clock synchronization. The final cold-start regression check still needs completing on the box.

When finished, give me the actual changed files and built APK, a short explanation of the design choices, the exact checks you ran, honest preview screenshots if available, and any remaining issues for Codex. Stop before live deployment. I want a finished, reviewable native redesign with one obvious action, not more features.
