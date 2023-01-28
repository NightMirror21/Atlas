package ru.nightmirror.atlas.interfaces.api;

import ru.nightmirror.atlas.database.tables.Marker;
import ru.nightmirror.atlas.database.tables.Territory;

import java.util.Set;

public interface AtlasAPI {
    Set<Marker> getMarkers();
    boolean updateMarker(Marker marker);
    boolean deleteMarker(Marker marker);
    boolean createMarker(Marker marker);

    Set<Territory> getTerritories();
    boolean updateTerritory(Territory territory);
    boolean deleteTerritory(Territory territory);
    boolean createTerritory(Territory territory);
}
