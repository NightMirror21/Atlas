package ru.nightmirror.atlas.interfaces.database;

import com.j256.ormlite.dao.Dao;
import ru.nightmirror.atlas.database.tables.Marker;
import ru.nightmirror.atlas.database.tables.Territory;

public interface IDatabaseLoader {
    boolean isConnected();
    Dao<Territory, String> loadTerritoriesTable();
    Dao<Marker, String> loadMarkersTable();
}
