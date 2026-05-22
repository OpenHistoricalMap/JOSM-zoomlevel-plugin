package org.openhistoricalmap.josm.zoomindicator;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.NavigatableComponent;
import org.openstreetmap.josm.gui.NavigatableComponent.ZoomChangeListener;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Adds a small label to the JOSM status bar showing the current view's
 * approximate web-map zoom level (z), computed from MapView.getScale().
 *
 * At z=0 the world's circumference (~40,075,016.686 m) fits in 256 px,
 * giving ~156543.034 m/px. zoom = log2(156543.034 / scale).
 *
 * The value is decimal (e.g. "z=14.27") to make small zoom adjustments visible.
 */
public class ZoomIndicatorPlugin extends Plugin {

    private static final double Z0_METERS_PER_PIXEL = 156543.03392804097;
    private static final double LN2 = Math.log(2);

    private final JLabel label = new JLabel(" z=– ");
    private MapView attachedView;

    private final ZoomChangeListener zoomListener = this::updateLabel;

    public ZoomIndicatorPlugin(PluginInformation info) {
        super(info);
        label.setToolTipText("Approximate web-map zoom level (log2(156543.034 / JOSM scale))");
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (oldFrame != null && attachedView != null) {
            NavigatableComponent.removeZoomChangeListener(zoomListener);
            oldFrame.statusLine.remove(label);
            oldFrame.statusLine.revalidate();
            oldFrame.statusLine.repaint();
            attachedView = null;
        }
        if (newFrame != null) {
            attachedView = newFrame.mapView;
            newFrame.statusLine.add(label);
            newFrame.statusLine.revalidate();
            NavigatableComponent.addZoomChangeListener(zoomListener);
            updateLabel();
        }
    }

    private void updateLabel() {
        final MapView mv = attachedView;
        if (mv == null) return;
        final double scale = mv.getScale();
        final double zoom = Math.log(Z0_METERS_PER_PIXEL / scale) / LN2;
        SwingUtilities.invokeLater(() -> label.setText(String.format(" z=%.2f ", zoom)));
    }
}
