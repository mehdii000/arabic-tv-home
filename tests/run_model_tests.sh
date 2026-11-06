#!/usr/bin/env bash
# Host JVM tests for the pure-Java model (calendar, digits, clock validity, weather states,
# Tangier solar times, palette contrast, MENU and launch gates). No Android SDK needed.
set -euo pipefail
ROOT=$(cd "$(dirname "$0")/.." && pwd)
OUT=$(mktemp -d); trap 'rm -rf "$OUT"' EXIT
javac -d "$OUT" "$ROOT/src/local/reroute/youtubehome/Model.java" "$ROOT/tests/ModelTest.java"
java -cp "$OUT" local.reroute.youtubehome.ModelTest
