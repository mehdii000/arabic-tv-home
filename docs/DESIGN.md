---
name: "YouTube Home"
description: "An Arabic television home overlooking the Strait, with one clear YouTube action."
colors:
  text: "#f7f6f1"
  text-muted: "#d3dde5"
  ink: "#10202f"
  ink-soft: "#415468"
  ivory: "#f7f2e8"
  focus-amber: "#f2b544"
  action-rest-fill: "#0e1f30cc"
  action-rest-edge: "#f7f6f166"
  time-divider: "#f7f6f155"
  maintenance-fill: "#101d2bf2"
  maintenance-edge: "#f7f6f144"
  maintenance-button-rest: "#f7f6f126"
  maintenance-status: "#f6c78a"
  youtube-fallback-red: "#e0261b"
  mark-white: "#ffffff"
  night-zenith: "#060f1c"
  night-band: "#0c1c31"
  night-horizon: "#1d3550"
  dawn-zenith: "#19223f"
  dawn-band: "#3d3a5c"
  dawn-horizon: "#c07a62"
  dusk-zenith: "#151a37"
  dusk-band: "#452f53"
  dusk-horizon: "#bd5f4a"
  day-zenith: "#0b3760"
  day-band: "#1b5682"
  day-horizon: "#6c9fbb"
  night-weather-tone: "#151b24"
  dawn-weather-tone: "#383a4a"
  dusk-weather-tone: "#383141"
  day-weather-tone: "#3a495c"
  storm-mix: "#2a2142"
  snow-mix: "#7d93ab"
  glyph-sun: "#f7c96a"
  glyph-moon: "#e8eef4"
  glyph-cloud: "#f1f4f8"
  glyph-storm-cloud: "#c3cad6"
  glyph-rain: "#a8d5f2"
  glyph-lightning: "#f6c457"
  glyph-fog: "#e4e9ef"
typography:
  clock:
    fontFamily: "sans-serif-light"
    fontSize: "120dp"
    fontWeight: 400
    letterSpacing: "-0.02em"
    fontFeature: "tnum"
  temperature:
    fontFamily: "sans-serif-light"
    fontSize: "58dp × textScale"
    fontWeight: 400
    fontFeature: "tnum"
  action-title:
    fontFamily: "sans-serif"
    fontSize: "40dp × textScale"
    fontWeight: 700
    fontFeature: "tnum"
  weekday:
    fontFamily: "sans-serif"
    fontSize: "36dp × textScale"
    fontWeight: 700
    fontFeature: "tnum"
  date:
    fontSize: "25dp × textScale"
    fontFeature: "tnum"
  conditions:
    fontSize: "23dp × textScale"
    fontFeature: "tnum"
  greeting-city:
    fontSize: "20dp × textScale"
    fontFeature: "tnum"
  action-hint:
    fontSize: "19dp × textScale"
    fontFeature: "tnum"
  ok-badge:
    fontFamily: "sans-serif"
    fontSize: "17dp × textScale"
    fontWeight: 700
    fontFeature: "tnum"
  weather-status:
    fontSize: "13dp × textScale"
    fontFeature: "tnum"
  maintenance-title:
    fontFamily: "sans-serif"
    fontSize: "28sp"
    fontWeight: 700
  maintenance-body:
    fontSize: "18sp"
    lineHeight: 1.15
  maintenance-button:
    fontFamily: "sans-serif"
    fontSize: "20sp"
    fontWeight: 700
  maintenance-details:
    fontSize: "13sp"
  maintenance-status:
    fontSize: "15sp"
rounded:
  action-fill: "28dp"
  action-ring: "34dp"
  maintenance-panel: "26dp"
  maintenance-button: "18dp"
spacing:
  safe-horizontal: "56dp"
  safe-top: "34dp"
  safe-bottom: "30dp"
  action-horizontal: "30dp"
  action-vertical: "18dp"
  action-inset: "7dp"
  copy-start: "22dp"
  copy-end: "16dp"
  rule-start: "26dp"
  rule-end: "24dp"
  weather-icon-gap: "12dp"
  status-top: "6dp"
  maintenance-horizontal: "36dp"
  maintenance-top: "30dp"
  maintenance-bottom: "28dp"
  maintenance-button-horizontal: "26dp"
  maintenance-button-vertical: "12dp"
  maintenance-button-gap: "14dp"
components:
  youtube-action-focused:
    backgroundColor: "{colors.ivory}"
    textColor: "{colors.ink}"
    typography: "{typography.action-title}"
    rounded: "{rounded.action-fill}"
    padding: "18dp 30dp"
    width: "540dp"
  youtube-action-rest:
    backgroundColor: "{colors.action-rest-fill}"
    textColor: "{colors.text}"
    typography: "{typography.action-title}"
    rounded: "{rounded.action-fill}"
    padding: "18dp 30dp"
    width: "540dp"
  ok-badge-focused:
    textColor: "{colors.ink}"
    typography: "{typography.ok-badge}"
    size: "max(56dp, 2.3 × textSizePx)"
  clock:
    textColor: "{colors.text}"
    typography: "{typography.clock}"
  weather-temperature:
    textColor: "{colors.text}"
    typography: "{typography.temperature}"
  maintenance-panel:
    backgroundColor: "{colors.maintenance-fill}"
    rounded: "{rounded.maintenance-panel}"
    padding: "30dp 36dp 28dp"
    width: "700dp"
  maintenance-button-focused:
    backgroundColor: "{colors.ivory}"
    textColor: "{colors.ink}"
    typography: "{typography.maintenance-button}"
    rounded: "{rounded.maintenance-button}"
    padding: "12dp 26dp"
  maintenance-button-rest:
    backgroundColor: "{colors.maintenance-button-rest}"
    textColor: "{colors.text}"
    typography: "{typography.maintenance-button}"
    rounded: "{rounded.maintenance-button}"
    padding: "12dp 26dp"
---

# Design System: YouTube Home

## Overview

**Creative North Star: "The View Across the Strait"**

The view across the Strait gives the television a calm sense of place: a marine sky, a sea horizon, the Tangier headland and a distant coast. A large clock and Arabic calendar sit above one centered YouTube card. The open middle of the scene keeps the screen welcoming and readable from a sofa.

This system records the implemented 3.0.1 redesign in native Java Android Views for the Android 10 X96Q at 1280 × 720. Exactly one viewer action is visible and focused on entry. Arabic resources are scoped to the Activity, with Latin digits and Africa/Casablanca time; the French device locale remains unchanged. Current source is the visual authority, superseding the earlier two-action design.

**Key Characteristics:**

- One centered action with a warm, unmistakable D-pad focus ring.
- An exceptionally large clock with a compact Arabic calendar lockup.
- A painted marine scene that distinguishes solar phase and known weather.
- A quiet state-change crossfade, with no continuous drawing loop.

## Colors

Deep marine fields transition into muted warm dawn and dusk; ivory, dark ink and amber identify the sole viewing action. Frontmatter uses CSS-compatible hex, including RGBA hex converted from Android ARGB without changing alpha.

### Primary

- **Focus amber** surrounds the focused card and maintenance button.
- **Ivory**, **ink** and **soft ink** provide the focused card's fill, title and supporting instruction.
- **YouTube fallback red** and **mark white** belong to the locally drawn fallback play mark. The runtime may instead use the installed YouTube icon when its intrinsic size is sufficient.

### Neutral

- **Text** carries clock, calendar and weather; **text muted** carries greeting, city and provenance. The weather status additionally uses view alpha 0.9.
- **Action rest fill/edge** describe the translucent unfocused card. **Time divider** is a translucent pale rule.
- **Maintenance fill/edge/button rest/status** describe the helper panel and its inline feedback. Details text uses alpha 0.85.
- **Solar triples:** each `night-*`, `dawn-*`, `dusk-*` and `day-*` group contains zenith, information-band and horizon values from `Model.base`. These are inputs to the sky rather than a single fixed background.
- **Weather tones and mixes:** `*-weather-tone`, `storm-mix` and `snow-mix` feed `Model.skyPalette`. Cloud cover blends toward the phase tone; fog lightens it, rain/storm darken it, snow cools it, and unknown weather applies a neutral phase-specific blend. Preserve the code's interpolation and scaling rather than inventing independent weather swatches.
- **Weather glyph colors:** the `glyph-*` entries distinguish sun, moon, cloud, rain, lightning and fog. Snow dots share mark white; storm clouds have their own subdued tint.

**The Known Weather Rule.** Only fresh, validated weather may show a temperature, condition glyph or weather-specific scene.

Model contrast checks compare main and muted text against palette stops, targeting 7:1 and 4.5:1 respectively. They do not prove those ratios for every composited pixel: sky glows, clouds, top shade, text shadow and crossfade are additional layers. Do not turn the palette-stop checks into a universal rendered-contrast claim.

## Typography

**Clock and temperature:** Android `sans-serif-light`, normal weight.

**Action, weekday, city, badge and maintenance headings/buttons:** Android `sans-serif`, bold where explicitly set.

**Other text:** native TextView defaults with system Arabic fallback; no font files are bundled.

The font-size notation in frontmatter mirrors the implementation. `textScale = max(1, min(1.3, configuration.fontScale))`. HomeScreen sets most text in pixels as base size × display density × textScale, represented here as `dp × textScale`. This is a deliberate clamp, not unrestricted native sp. The clock is fixed at 120dp. Maintenance uses native sp and does not share the HomeScreen clamp.

- **Clock:** the largest landmark, 24-hour `HH:mm`, LTR, single-line, no included font padding, slightly tightened letter spacing.
- **Temperature:** light, LTR and without included font padding; the fresh temperature is rounded to a whole degree.
- **Action title / hint:** bold Arabic `يوتيوب` over `اضغطي للمشاهدة`; text shadows are removed on the card.
- **Weekday / date / greeting:** a bold weekday, Moroccan-Arabic date and quieter greeting beside the clock.
- **City / conditions / weather status:** a bold muted city name, larger condition line and small update/provider line.
- **OK badge:** bold Latin letters, centered and without included font padding; its circle grows when necessary with text size.
- **Maintenance:** separate heading, body, button, details and status roles. Body uses a 1.15 line-spacing multiplier.

HomeScreen TextViews request tabular numbers (`tnum`) and default to RTL with start gravity; the clock, temperature and OK badge explicitly use LTR text direction. Weather labels override gravity to the physical left edge. No custom line-height or font file is inferred for the other roles.

## Layout

The full-screen FrameLayout places SkyView behind a safe-area frame, using the frontmatter padding. Its RTL top band contains a clock/calendar lockup on the right, a flexible spacer and the weather group flush left. A thin divider (2dp × 92dp) separates the clock from greeting, weekday and date, with the recorded start/end margins.

The only viewer control is bottom-centered, with its width and padding in frontmatter and height determined by content. In its RTL row a 78dp YouTube icon leads the copy; an expanding text column separates it from a circular OK badge. The badge diameter is the greater of 56dp and 2.3 times its text size in pixels, rounded to an integer. The weather glyph occupies 66dp square next to the temperature. There is no separate controller-help row.

The background is painted in a 1280 × 720 coordinate system, with its sea horizon at y=500, then scaled to the actual View. Content remains dp-based with the documented text scaling. There are no web breakpoints or mobile compositions.

**The One Action Rule.** The viewer-facing Home has exactly one focusable control: YouTube.

The hidden maintenance panel is a 700dp-wide, content-height native Dialog. Its three RTL buttons are Back, device settings and weather refresh, in an explicit closed focus path; up/down stay on the current button. It is outside the everyday viewing composition.

## Elevation & Depth

Painted sky gradients, radial light, cloud banks, a sea reflection and layered coastal silhouettes give depth to the scene. Home information text adds a native shadow (8dp radius, 0 horizontal offset, 1dp vertical offset, translucent black); card text removes that shadow. The weather glyph uses its own Canvas shadow. A top shade dims the information band without becoming a separate panel.

The focused action has native 10dp elevation; the unfocused state has zero. Its amber ring has a 4dp stroke, with an inset ivory fill. Android supplies the elevation shadow; no CSS box-shadow is part of the app. The maintenance window uses dim amount 0.55, with a translucent panel above the sky.

**The Stable Focus Rule.** Directional keys preserve the single YouTube focus; hidden maintenance opens with Back focused.

Scene updates crossfade over 1.6 seconds using ValueAnimator and the system animator scale, becoming immediate when animations are disabled. Updates before attach or while offscreen switch instantly. The scene is cached in runtime memory bitmaps; state changes, not a perpetual render loop, drive rebuilding. There are no bundled sky rasters.

## Shapes

The focused action layers an outer rounded ring over a smaller ivory fill: the outer-ring and fill radii are separate frontmatter tokens, with a 7dp inset around the fill. The resting fill uses the same inset, a 2dp translucent outline and zero elevation. Padding is explicitly restored after changing its background.

The OK badge is an outlined circle: dark 3dp stroke in focus, pale 2dp stroke at rest. The maintenance panel uses its own radius and 1dp border; its buttons use smaller corners, with 3dp amber outlines in focus and 1dp pale outlines at rest.

The fallback play mark, sun/moon glyphs, cloud banks, rain, snow, fog, headland and far coast are drawn with native paths, circles, gradients and strokes. The sea and coast are a stylized scene, not a geographic rendering.

## Components

### YouTube action

A single ivory focal point on the horizon. It displays the YouTube mark, Arabic title/instruction and remote-like OK badge, with an Arabic accessible description. Initial focus is restored on create, resume, window focus and Home re-entry. All directional keys keep or restore that focus. OK/Enter opens the official YouTube app, with a short duplicate-launch guard; Home does not auto-launch playback. Native Arabic message dialogs handle launch failure. No Search component exists.

### Clock and calendar lockup

A very large fixed-size clock and compact Arabic calendar remain stable at the right. Before the minimum valid clock date, the clock shows `--:--`, the greeting becomes `أهلاً بك`, the weekday slot shows `جارٍ ضبط الساعة…`, and the date is hidden. This state makes no weather claim.

### Tangier weather and WeatherGlyph

The left group shows `طنجة`, a static native weather glyph/temperature row, Arabic condition and update/provider. Fresh weather is accepted for up to three hours with a one-minute future tolerance. Cloud codes distinguish mostly clear, partly cloudy and overcast with inferred cover 0.25, 0.55 and 1. Drizzle and showers have distinct Arabic labels even though they share the rain drawing family.

Loading/unavailable/stale states hide the entire temperature/glyph row. Initial loading uses `جارٍ تحميل الطقس…`; failure uses `الطقس غير متاح الآن` and `سيظهر عند عودة الإنترنت`, staying steady during retries. Clock and weather-change handling belongs to HomeActivity; it does not add viewer controls.

### Strait sky

SkyView rebuilds an offscreen memory bitmap when solar phase, weather kind, cloud cover, quantized arc progress or View size changes. The progress is rounded to 1/48 of the arc. A three-stop sky gradient sits above sea, Tangier headland and far coast; cloud, precipitation, fog, stars, glow and reflection depend on the state. Unknown weather keeps a neutral time-of-day scene without a sun/moon disc or weather-specific clouds/precipitation; a subdued ambient glow may remain.

Daylight progression uses today's forecast sunrise/sunset when applicable and an offline Tangier solar calculation otherwise. The sun follows a stylized right-to-left daylight arc. The nighttime crescent follows a decorative darkness arc derived from sunset/sunrise; it is **not an actual lunar ephemeris or lunar phase calculation**. Do not describe either scene geometry as a measured celestial bearing.

### Maintenance panel and buttons

Three deliberate MENU releases within two seconds open a native custom helper Dialog. Held/repeated MENU and other-key interruptions do not count; MENU inside the panel is ignored. Back is focused first and dismisses safely. Settings and weather refresh sit to its left. Details show version and clock/weather state; an inline warm status line reports refresh or Settings failure. The window uses native Android dialog conventions while the panel and focus styles are explicitly drawn in Java.

The sidecar renders labeled native Java excerpts inside self-contained HTML code previews. Their HTML/CSS is documentation packaging, not app runtime UI, component implementation or a source of web interaction rules.

## Do's and Don'ts

### Do:

- **Do** preserve the 540dp centered card, generous safe margins and open middle scene.
- **Do** keep the clock fixed at 120dp and apply the implemented textScale clamp to other HomeScreen text.
- **Do** use Arabic app-local resources, Latin digits and fixed Tangier weather.
- **Do** distinguish every solar phase within weather changes, using Model.skyPalette as authority.
- **Do** keep unknown weather neutral and hide the temperature row when weather is unverified.
- **Do** use the system-scaled crossfade and an immediate fully drawn state when offscreen.
- **Do** keep the moon arc explicitly decorative and preserve the limits of palette-stop contrast evidence.

### Don't:

- **Don't** add Search, query fields, shortcuts or extra viewer controls.
- **Don't** claim actual lunar position, lunar phase or photographic geography.
- **Don't** infer continuous animation or bundled sky images from the memory bitmap cache.
- **Don't** replace native D-pad behavior with hover interactions or web navigation.
- **Don't** present stale weather as current or claim 7:1 contrast for every composited scene pixel.
- **Don't** extrapolate the verified TV layout into invented mobile breakpoints.

Source authority: `src/local/reroute/youtubehome/HomeScreen.java`, `SkyView.java`, `WeatherGlyph.java`, `Model.java`, `HomeActivity.java`, `Maintenance.java` and `WeatherClient.java`. Product context comes from `PRODUCT.md`; operational evidence and deployment status belong in `IMPLEMENTATION.md`. This is an extraction of implemented 3.0.1 design, not a new visual proposal.
