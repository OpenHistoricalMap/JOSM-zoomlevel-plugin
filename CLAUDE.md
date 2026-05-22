# zoomlevel

A JOSM plugin that displays the current MapView's web-map-style zoom
level (decimal, e.g. `z=14.27`) in the status bar. Updates live on
zoom/pan via `MapView.ZoomChangeListener`.

## Pinned design decisions

These were settled during planning. Don't relitigate without explicit
user direction.

- **Build system: Gradle.** Sibling plugin `ohm-josm-tag-validator`
  uses Ant; this one deliberately does not. Don't switch.
- **Display format: `z=14.27`** (two decimal places). Not integer, not
  dual-format. No toggle.
- **Non-interactive label.** No click handler, no context menu, no
  copy-to-clipboard.
- **Mercator-only.** The constant `156543.03392804097` bakes in
  EPSG:3857. Don't generalize to other projections.
- **Repo lives in the `OpenHistoricalMap` GitHub org** directly (not
  personal-first then migrated).
- **License: GPL-2.0-or-later.** Required for JOSM plugin compatibility; JOSM itself is GPL-2.0-or-later.
- **Java package: `org.openhistoricalmap.josm.zoomindicator`.**

## Build

    gradle wrapper --gradle-version 8.5   # one-time, if no ./gradlew yet
    ./gradlew dist

Output: `build/dist/zoomlevel.jar`.

## Install for local testing (macOS)

    cp build/dist/zoomlevel.jar \
       ~/Library/Application\ Support/JOSM/plugins/

Restart JOSM, enable in Preferences → Plugins.

## Testing

Load any data in JOSM, watch the status bar bottom-left, zoom in/out.
The displayed value should roughly match the `#map=` zoom in
openhistoricalmap.org URLs at the same view. Off-by-1 → constant
wrong. No update on zoom → listener didn't register.

## Conventions

- Sandboxed development sessions are referred to as `sbx`, not
  "Docker containers."
- SVG icons (if added later): filled paths only, no `stroke=` for
  visible line art.