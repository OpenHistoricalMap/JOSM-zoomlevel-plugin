# zoomlevel

A [JOSM](https://josm.openstreetmap.de/) plugin that shows the current map
view's web-map zoom level in the status bar, next to the lat/lon display.

## What it shows

The zoom field displays a decimal value such as `z=14.27`, computed from
JOSM's internal scale using the standard Web Mercator formula:

    zoom = log₂(156543.034 m/px ÷ current scale)

The decimal precision makes small zoom changes visible that would be invisible
as integers. Values update live as you zoom or pan.

## Screenshot

<img alt="screenshot of zoomlevel plugin info in JOSM status bar: zoom level and link out to OSM/OHM for same view" src="https://github.com/OpenHistoricalMap/JOSM-zoomlevel-plugin/blob/main/images/zoomlevel-plugin.jpg" />

## Features

- **Decimal zoom level** — displayed next to lat/lon in the status bar
- **Live updates** — responds to zoom and pan with no interaction required
- **Browser link** — link button opens the current view in OSM or OHM,
  auto-detected from the JOSM API server preference (`osm-server.url`)
- **Tooltips** — hover the zoom field for "Browser-equivalent zoom level";
  hover the link button for "Open OSM to this view" or "Open OHM to this view"

## Installation

### JOSM Plugin Manager

Search for **zoomlevel** in Preferences → Plugins → Download list.

### Manual

1. Download `zoomlevel.jar` from the releases page
2. Copy to your JOSM plugins directory:

       # macOS
       cp zoomlevel.jar "~/Library/Application Support/JOSM/plugins/"

       # Linux
       cp zoomlevel.jar ~/.josm/plugins/

3. Restart JOSM and enable in **Preferences → Plugins**

## Build from source

Requires Java 17. Uses the included Gradle wrapper.

    ./gradlew dist

Output: `build/dist/zoomlevel.jar`

## Compatibility

| Requirement | Version |
|---|---|
| JOSM | ≥ 18877 |
| Java (build) | 17 |
| Java (runtime) | 11+ |
| Gradle wrapper | 8.5 |

## License

GPL-2.0-or-later — see [LICENSE](LICENSE).
