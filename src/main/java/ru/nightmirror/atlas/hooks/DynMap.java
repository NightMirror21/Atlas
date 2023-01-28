package ru.nightmirror.atlas.hooks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.GenericMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerSet;
import ru.nightmirror.atlas.api.*;
import ru.nightmirror.atlas.database.tables.Territory;
import ru.nightmirror.atlas.interfaces.IAtlas;
import ru.nightmirror.atlas.misc.Logging;

public class DynMap implements Listener {

    private final DynmapAPI api;
    private final IAtlas atlas;

    private final static String MARKER_SET_ID = "ru.nightmirror.atlas.markers";
    private final static String TERRITORIES_SET_ID = "ru.nightmirror.atlas.territories";

    private MarkerSet markerSet = null;
    private MarkerSet territoriesSet = null;

    public DynMap(Plugin api, IAtlas atlas) {
        this.api = (DynmapAPI) api;
        this.atlas = atlas;

        Bukkit.getServer().getPluginManager().registerEvents(this, atlas.getPlugin());
        init();
    }

    private void init() {
        markerSet = api.getMarkerAPI().getMarkerSet(MARKER_SET_ID);
        if (markerSet == null) {
            markerSet = api.getMarkerAPI().createMarkerSet(MARKER_SET_ID, atlas.getConfigContainer().getMarkers().getString("list-dynmap-name"), api.getMarkerAPI().getMarkerIcons(), false);
        }

        territoriesSet = api.getMarkerAPI().getMarkerSet(TERRITORIES_SET_ID);
        if (territoriesSet == null) {
            territoriesSet = api.getMarkerAPI().createMarkerSet(TERRITORIES_SET_ID, atlas.getConfigContainer().getTerritories().getString("list-dynmap-name"), api.getMarkerAPI().getMarkerIcons(), false);
        }

        refreshAll();
    }

    public void refreshAll() {
        markerSet.getMarkers().forEach(GenericMarker::deleteMarker);
        atlas.getAPI().getMarkers().forEach(this::drawMarker);

        territoriesSet.getAreaMarkers().forEach(GenericMarker::deleteMarker);
        atlas.getAPI().getTerritories().forEach(this::drawTerritory);

        Logging.info("Objects on DynMap updated");
    }

    private void drawMarker(ru.nightmirror.atlas.database.tables.Marker marker) {
        markerSet.createMarker(
                marker.getUUID(),
                marker.getName(),
                marker.getPoint().getWorld().getName(),
                marker.getPoint().getBlockX(),
                64,
                marker.getPoint().getBlockZ(),
                api.getMarkerAPI().getMarkerIcon("sign"),
                false)
                .setDescription(marker.getDescription());
    }

    @EventHandler
    private void drawMarker(MarkerCreatedEvent event) {
        drawMarker(event.getMarker());
    }

    @EventHandler
    private void refreshMarker(MarkerUpdatedEvent event) {
        Marker marker = markerSet.findMarker(event.getMarker().getUUID());
        if (marker == null) return;
        marker.setLabel(event.getMarker().getName());
        marker.setDescription(event.getMarker().getDescription());
    }

    @EventHandler
    private void clearMarker(MarkerDeletedEvent event) {
        Marker marker = markerSet.findMarker(event.getMarker().getUUID());
        if (marker == null) return;
        marker.deleteMarker();
    }

    private void drawTerritory(Territory territory) {
        Location[] points = new Location[territory.getPoints().size()];

        int j = 0;
        for (Location location : territory.getPoints()) {
            points[j] = location;
            j++;
        }

        double[] x = new double[points.length];
        double[] z = new double[points.length];

        for (int i = 0; i < points.length; i++) {
            x[i] = points[i].getBlockX();
            z[i] = points[i].getBlockZ();
        }

        AreaMarker area = territoriesSet.createAreaMarker(
                territory.getUUID(),
                territory.getName(),
                false,
                points[0].getWorld().getName(),
                x,
                z,
                false
        );
        area.setDescription(territory.getDescription());
        int color = Integer.parseInt(atlas.getTerritoryManager().getType(territory.getType()).getColor().getHexRGB().replaceAll("0x", ""), 16);
        area.setFillStyle(0.5D, color);
        area.setLineStyle(area.getLineWeight(), area.getLineOpacity(), color);
    }

    @EventHandler
    private void drawTerritory(TerritoryCreatedEvent event) {
        drawTerritory(event.getTerritory());
    }

    @EventHandler
    private void refreshTerritory(TerritoryUpdatedEvent event) {
        AreaMarker marker = territoriesSet.findAreaMarker(event.getTerritory().getUUID());
        if (marker == null) return;
        marker.setLabel(event.getTerritory().getName());
        marker.setDescription(event.getTerritory().getDescription());
    }

    @EventHandler
    private void clearTerritory(TerritoryDeletedEvent event) {
        AreaMarker marker = territoriesSet.findAreaMarker(event.getTerritory().getUUID());
        if (marker == null) return;
        marker.deleteMarker();
    }
}
