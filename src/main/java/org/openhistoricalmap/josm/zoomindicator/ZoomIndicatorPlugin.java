// SPDX-FileCopyrightText: 2026 OpenHistoricalMap
// SPDX-License-Identifier: GPL-2.0-or-later
package org.openhistoricalmap.josm.zoomindicator;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.lang.reflect.Field;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.projection.ProjectionRegistry;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapStatus;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.NavigatableComponent;
import org.openstreetmap.josm.gui.NavigatableComponent.ZoomChangeListener;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.OpenBrowser;

/**
 * Adds a status bar field showing the current MapView's approximate web-map
 * zoom level (z), computed from MapView.getScale().
 *
 * At z=0 the world's circumference (~40,075,016.686 m) fits in 256 px,
 * giving ~156543.034 m/px. zoom = log2(156543.034 / scale).
 *
 * The value is decimal (e.g. "z=14.27") to make small zoom adjustments visible.
 * A link button opens the current view in OHM or OSM depending on the configured API server.
 */
public class ZoomIndicatorPlugin extends Plugin {

    private static final double Z0_METERS_PER_PIXEL = 156543.03392804097;
    private static final double LN2 = Math.log(2);
    // Matches MapStatus.ImageLabel background color (#b8cfe5)
    private static final Color STATUS_BG = new Color(184, 207, 229);

    private JPanel statusPanel;
    private JLabel zoomLabel;
    private MapView attachedView;
    private volatile double currentZoom = Double.NaN;

    private final ZoomChangeListener zoomListener = this::updateLabel;

    public ZoomIndicatorPlugin(PluginInformation info) {
        super(info);
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (oldFrame != null && attachedView != null) {
            NavigatableComponent.removeZoomChangeListener(zoomListener);
            oldFrame.statusLine.remove(statusPanel);
            oldFrame.statusLine.revalidate();
            oldFrame.statusLine.repaint();
            attachedView = null;
            statusPanel = null;
        }
        if (newFrame != null) {
            attachedView = newFrame.mapView;
            statusPanel = buildStatusPanel();
            newFrame.statusLine.add(statusPanel, findInsertIndex(newFrame.statusLine));
            newFrame.statusLine.revalidate();
            NavigatableComponent.addZoomChangeListener(zoomListener);
            updateLabel();
        }
    }

    /**
     * Finds the component index immediately after the longitude field so our
     * panel appears next to the lat/lon display. Falls back to index 2 (after
     * the expected lat and lon positions) if reflection fails.
     */
    private int findInsertIndex(MapStatus statusLine) {
        try {
            Field f = MapStatus.class.getDeclaredField("lonText");
            f.setAccessible(true);
            Component lonComp = (Component) f.get(statusLine);
            Component[] comps = statusLine.getComponents();
            for (int i = 0; i < comps.length; i++) {
                if (comps[i] == lonComp) return i + 1;
            }
        } catch (Exception ignored) {}
        return Math.min(2, statusLine.getComponentCount());
    }

    private JPanel buildStatusPanel() {
        // Blue field matching MapStatus.ImageLabel style
        JPanel zoomField = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 1));
        zoomField.setBackground(STATUS_BG);
        zoomField.setOpaque(true);
        zoomField.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));
        zoomField.setToolTipText("Browser-equivalent zoom level");

        JLabel iconLabel = new JLabel();
        ImageIcon icon = loadScaledIcon("dialogs/zoomin", 12);
        if (icon != null) {
            iconLabel.setIcon(icon);
        } else {
            iconLabel.setText("⌕");
        }
        zoomField.add(iconLabel);

        zoomLabel = new JLabel(" z=– ");
        zoomLabel.setForeground(Color.BLACK);
        zoomLabel.setToolTipText("Browser-equivalent zoom level");
        zoomField.add(zoomLabel);

        // Square link button with Wikipedia-style external link icon
        int btnSize = 18;
        JButton linkButton = new JButton(externalLinkIcon(btnSize - 4));
        linkButton.setPreferredSize(new Dimension(btnSize, btnSize));
        linkButton.setMargin(new Insets(1, 1, 1, 1));
        linkButton.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));
        linkButton.setBackground(STATUS_BG);
        linkButton.setOpaque(true);
        linkButton.setContentAreaFilled(true);
        linkButton.setFocusPainted(false);
        linkButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkButton.setToolTipText("Open " + siteLabel() + " to this view");
        linkButton.addActionListener(e -> openInBrowser());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        panel.setOpaque(false);
        panel.add(zoomField);
        panel.add(linkButton);
        return panel;
    }

    /**
     * Paints a Wikipedia-style external link icon: a box at lower-left with an
     * arrow pointing out to the upper-right corner.
     */
    private static Icon externalLinkIcon(int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int s = size - 2;
                int o = 1;

                // Box occupying the lower-left ~70% of the area (dark outline, transparent fill)
                int boxSide = (s * 7) / 10;
                int bx = x + o;
                int by = y + o + (s - boxSide);
                g2.setColor(Color.DARK_GRAY);
                g2.drawRect(bx, by, boxSide, boxSide);

                // Arrow from box centre to upper-right corner, in red
                int ax1 = bx + boxSide / 2;
                int ay1 = by + boxSide / 2;
                int ax2 = x + o + s;
                int ay2 = y + o;
                g2.setColor(Color.RED);
                g2.drawLine(ax1, ay1, ax2, ay2);

                // Arrowhead (two short lines)
                int h = 3;
                g2.drawLine(ax2, ay2, ax2 - h, ay2);
                g2.drawLine(ax2, ay2, ax2, ay2 + h);

                g2.dispose();
            }

            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    private ImageIcon loadScaledIcon(String name, int size) {
        try {
            ImageIcon raw = ImageProvider.get(name);
            if (raw == null) return null;
            Image scaled = raw.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
    }

    private void updateLabel() {
        final MapView mv = attachedView;
        if (mv == null) return;
        final double scale = mv.getScale();
        final double zoom = Math.log(Z0_METERS_PER_PIXEL / scale) / LN2;
        currentZoom = zoom;
        SwingUtilities.invokeLater(() -> {
            if (zoomLabel != null)
                zoomLabel.setText(String.format(" z=%.2f ", zoom));
        });
    }

    private String siteLabel() {
        String url = Config.getPref().get("osm-server.url", "");
        return url.contains("openhistoricalmap.org") ? "OHM" : "OSM";
    }

    private void openInBrowser() {
        final MapView mv = attachedView;
        if (mv == null || Double.isNaN(currentZoom)) return;
        LatLon center = ProjectionRegistry.getProjection().eastNorth2latlon(mv.getCenter());
        int z = (int) currentZoom;
        String baseUrl = siteLabel().equals("OHM")
                ? "https://www.openhistoricalmap.org"
                : "https://www.openstreetmap.org";
        String url = String.format("%s/#map=%d/%.5f/%.5f", baseUrl, z, center.lat(), center.lon());
        OpenBrowser.displayUrl(url);
    }
}
