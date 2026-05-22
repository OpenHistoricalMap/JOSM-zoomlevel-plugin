# ohm-josm-zoom-indicator

A small JOSM plugin that displays the current map view's approximate
web-map zoom level (decimal, e.g. `z=14.27`) in the status bar.

Computed as `log2(156543.034 / MapView.getScale())`, assuming Web Mercator.
The label updates live as you zoom or pan.

## Build

    ./gradlew dist

The resulting plugin JAR will be in `build/dist/`.

## Install (development)

Copy the JAR to your JOSM plugins directory and restart JOSM:

    # macOS
    cp build/dist/ohm-josm-zoom-indicator.jar \
       ~/Library/Application\ Support/JOSM/plugins/

    # Linux
    cp build/dist/ohm-josm-zoom-indicator.jar ~/.josm/plugins/

Then enable it in **Preferences → Plugins**.

## License

[Unlicense](LICENSE).
