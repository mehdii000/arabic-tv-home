# YouTube Home

<!-- impeccable:product-schema 1 -->

## Platform

android

## Stack

Existing native Java Android Views launcher; Android 10 X96Q, 1280 × 720. Android SDK build-tools 36; the deployed APK keeps its original private signing key. No web wrapper.

## Users

An older Arabic-speaking viewer using a television remote, with her grandson maintaining the device.

## Product Purpose

Make watching YouTube easy from a welcoming Arabic home, with a readable clock, Arabic day/date, and weather for Tangier, Morocco. Guest video browsing must offer content without repeatedly entering searches.

## Operating Context

Home is the default launcher on an Android box cleaned up with the owner's authorisation (agent-performed; see the companion recovery notes). Device locale is fr-FR and must remain unchanged. Firmware integrity is unverified.

## Capabilities and Constraints

Latest owner instruction: exactly one visible action, YouTube, focused on entry. No Search button, query field, shortcuts, or additional viewer controls. Visual redesign 3.0 was done by Claude locally (see IMPLEMENTATION.md); Codex reviews and deploys it. Background follows real Tangier weather and the sun position, computed offline when there is no forecast.

Arabic only inside the launcher; Latin digits 0123456789 throughout. Real weather and time determine background. Clear D-pad focus, large readable text, deliberate controller access to maintenance settings. Owner authorizes installation and removal/disablement of unwanted apps. No account credentials requested. No fabricated weather, and useful offline fallback. Weather city is confirmed Tangier; viewing interests unspecified because owner prefers guest recommendations over curated shortcuts.

## Accessibility & Inclusion

TV viewing distance and remote-only use. High contrast, safe margins, limited actions and no disruptive animation. Accessible labels and deterministic focus. Design choices delegated by owner's “yes go for it”.

## Evidence on Hand

At the 2026-11-06 baseline the box date was wrong (2025-11-13), automatic time was off and the NTP host was invalid, so YouTube could not connect. Redesign 3.0 was first verified on an emulator and with offscreen renders; hardware results are summarised in [IMPLEMENTATION.md](IMPLEMENTATION.md).

## Codex hardware review — 2026-11-06

Version 3.0.1/code 5 installed on the X96Q after Codex reviewed Claude's 3.0. Single-action Arabic design preserved. 24 remote checks and native layout/JSON checks passed; Settings round trip and live clock correction verified. Device locale stays fr-FR, sole permission INTERNET. Physical TV colour and sofa-distance readability require owner observation.
