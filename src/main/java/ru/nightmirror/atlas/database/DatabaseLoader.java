package ru.nightmirror.atlas.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.logger.Level;
import com.j256.ormlite.table.TableUtils;
import ru.nightmirror.atlas.config.Config;
import ru.nightmirror.atlas.database.tables.Marker;
import ru.nightmirror.atlas.database.tables.Territory;
import ru.nightmirror.atlas.interfaces.database.Database;
import ru.nightmirror.atlas.misc.Logging;

import java.io.File;
import java.sql.SQLException;

public class DatabaseLoader implements Database {

    private final Config config;
    private final File folder;

    private JdbcConnectionSource connection = null;

    private Dao<Marker, String> markers = null;
    private Dao<Territory, String> territories = null;

    public DatabaseLoader(Config config, File pluginFolder) {
        this.config = config;
        folder = pluginFolder;

        com.j256.ormlite.logger.Logger.setGlobalLogLevel(Level.OFF);
    }

    @Override
    public boolean connect() {
        Logging.info("Connecting to database");

        try {
            connection = getConnectionSource();
            createTablesAndDao();
        } catch (SQLException exception) {
            Logging.error("Can't connect to database! " + exception.getMessage());
            exception.printStackTrace();
            return false;
        }

        if (markers == null || territories == null) {
            Logging.error("Can't load dao from database");
            return false;
        }

        return true;
    }

    private void createTablesAndDao() throws SQLException {
        TableUtils.createTableIfNotExists(connection, Marker.class);
        TableUtils.createTableIfNotExists(connection, Territory.class);

        markers = DaoManager.createDao(connection, Marker.class);
        territories = DaoManager.createDao(connection, Territory.class);
    }

    @Override
    public void close() {
        try {
            connection.close();
            Logging.info("Connection to database closed");
        } catch (Exception exception) {
            Logging.error("Can't close connection to database. " + exception.getMessage());
        }
    }

    @Override
    public boolean reload() {
        close();
        return connect();
    }

    @Override
    public boolean isConnected() {
        return connection != null;
    }

    @Override
    public Dao<Territory, String> getTerritoriesTable() {
        return territories;
    }

    @Override
    public Dao<Marker, String> getMarkersTable() {
        return markers;
    }

    private JdbcConnectionSource getConnectionSource() throws SQLException {
        final String databaseType = config.getString("database.type", "sqlite");

        if (databaseType.equalsIgnoreCase("sqlite") || databaseType.equalsIgnoreCase("h2"))
            return new JdbcConnectionSource("jdbc:" + databaseType + ":" + new File(folder, "database.db").getAbsolutePath());

        if (config.getBoolean("database.use-user-and-password", false)) {
            return new JdbcConnectionSource("jdbc:" + databaseType + "://" + config.getString("database.address") + File.separator + config.getString("database.name"),
                    config.getString("database.user"),
                    config.getString("database.password")
            );
        } else {
            return new JdbcConnectionSource("jdbc:" + databaseType + "://" + config.getString("database.address") + File.separator + config.getString("database.name"));
        }
    }
}
