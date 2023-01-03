package ru.nightmirror.atlas.database;

import com.j256.ormlite.dao.Dao;
import ru.nightmirror.atlas.database.tables.Marker;
import ru.nightmirror.atlas.database.tables.Territory;
import ru.nightmirror.atlas.interfaces.database.Database;

public class DatabaseLoader implements Database {

    @Override
    public boolean connect() {
        return false;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public Dao<Territory, String> loadTerritoriesTable() {
        return null;
    }

    @Override
    public Dao<Marker, String> loadMarkersTable() {
        return null;
    }
}
