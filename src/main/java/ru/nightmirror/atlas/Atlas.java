package ru.nightmirror.atlas;

import org.bukkit.Bukkit;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.nightmirror.atlas.api.*;
import ru.nightmirror.atlas.commands.AtlasCommand;
import ru.nightmirror.atlas.commands.MarkerCommand;
import ru.nightmirror.atlas.commands.TerritoryCommand;
import ru.nightmirror.atlas.config.ConfigContainer;
import ru.nightmirror.atlas.controllers.PlayerController;
import ru.nightmirror.atlas.database.DatabaseLoader;
import ru.nightmirror.atlas.database.tables.Marker;
import ru.nightmirror.atlas.database.tables.Territory;
import ru.nightmirror.atlas.hooks.DynMap;
import ru.nightmirror.atlas.hooks.Metrics;
import ru.nightmirror.atlas.interfaces.IAtlas;
import ru.nightmirror.atlas.interfaces.api.AtlasAPI;
import ru.nightmirror.atlas.interfaces.config.IConfigContainer;
import ru.nightmirror.atlas.interfaces.database.Database;
import ru.nightmirror.atlas.interfaces.managers.IMarkersManager;
import ru.nightmirror.atlas.interfaces.managers.ITerritoryManager;
import ru.nightmirror.atlas.interfaces.managers.Manager;
import ru.nightmirror.atlas.managers.MarkersManager;
import ru.nightmirror.atlas.managers.TerritoriesManager;
import ru.nightmirror.atlas.misc.Logging;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class Atlas extends JavaPlugin implements IAtlas, AtlasAPI {

    private ConfigContainer configContainer;
    private Manager markers;
    private Manager territories;
    private Database database;

    private DynMap dynMap = null;

    @Override
    public void onEnable() {
        configContainer = new ConfigContainer(this);
        if (configContainer.getBase().getBoolean("is-debug-enabled"))
            Logging.changeDebugEnabled();

        PlayerController controller = new PlayerController();
        getServer().getPluginManager().registerEvents(controller, this);

        database = new DatabaseLoader(configContainer.getBase(), getDataFolder());
        if (!database.connect()) {
            Logging.warn("Disabling...");
            getServer().getPluginManager().disablePlugin(this);
        }

        markers = new MarkersManager(configContainer, (DatabaseLoader) database, controller);
        territories = new TerritoriesManager(configContainer, (DatabaseLoader) database, controller);

        registerCommands();
        checkDynmap();

        try {
            new Metrics(this, 17576);
        } catch (Exception exception) {
            Logging.warn("Can't load metrics cause: " + exception.getMessage());
        }

        Logging.info("Enabled");
    }

    private void checkDynmap() {
        if (getServer().getPluginManager().isPluginEnabled("dynmap")) {
            dynMap = new DynMap(getServer().getPluginManager().getPlugin("dynmap"), this);
            Logging.info("Hooked with DynMap");
        } else {
            Logging.info("DynMap not found. Continue");
        }
    }

    @Override
    public void onDisable() {
        if (database != null)
            database.close();

        if (markers != null)
            markers.stop();

        if (territories != null)
            territories.stop();

        Logging.info("Disabled");
    }

    @Override
    public Plugin getPlugin() {
        return this;
    }

    @Override
    public AtlasAPI getAPI() {
        return this;
    }

    @Override
    public boolean reload() {
        boolean success = configContainer.reload() && database.reload() && markers.reload() && territories.reload();
        registerCommands();
        return success;
    }

    @Nullable
    @Override
    public DynMap getDynMap() {
        return dynMap;
    }

    @Override
    public ITerritoryManager getTerritoryManager() {
        return (ITerritoryManager) territories;
    }

    @Override
    public IMarkersManager getMarkerManager() {
        return (IMarkersManager) markers;
    }

    @Override
    public IConfigContainer getConfigContainer() {
        return configContainer;
    }

    @Override
    public String getAtlasVersion() {
        return getDescription().getVersion();
    }

    private void registerCommands() {
        TabExecutor atlasCmd = new AtlasCommand(this);
        getCommand("atlas").setExecutor(atlasCmd);
        getCommand("atlas").setTabCompleter(atlasCmd);

        MarkerCommand markerCmd = new MarkerCommand(configContainer, (IMarkersManager) markers);
        getCommand("marker").setExecutor(markerCmd);
        getCommand("marker").setTabCompleter(markerCmd);

        TerritoryCommand territoryCmd = new TerritoryCommand(configContainer, (ITerritoryManager) territories);
        getCommand("territory").setExecutor(territoryCmd);
        getCommand("territory").setTabCompleter(territoryCmd);
    }

    @Override
    public Set<Marker> getMarkers() {
        if (!database.isConnected()) return Set.of();
        try {
            return new HashSet<>(database.getMarkersTable().queryForAll());
        } catch (Exception exception) {
            Logging.warn(String.format("Error during getting markers by API: '%s'", exception.getMessage()));
            return Set.of();
        }
    }

    @Override
    public boolean updateMarker(Marker marker) {
        if (!database.isConnected()) return false;
        try {
            MarkerUpdatedEvent event = new MarkerUpdatedEvent(marker, false);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }

            database.getMarkersTable().update(marker);
            return true;
        } catch (Exception exception) {
            Logging.warn(String.format("Error during updating marker by API: '%s'", exception.getMessage()));
            return false;
        }
    }

    @Override
    public boolean deleteMarker(Marker marker) {
        if (!database.isConnected()) return false;
        try {
            MarkerDeletedEvent event = new MarkerDeletedEvent(marker, false);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }

            database.getMarkersTable().deleteById(marker.getUUID().toString());
            return true;
        } catch (Exception exception) {
            Logging.warn(String.format("Error during deleting marker by API: '%s'", exception.getMessage()));
            return false;
        }
    }

    @Override
    public boolean createMarker(Marker marker) {
        if (!database.isConnected()) return false;
        try {
            MarkerCreatedEvent event = new MarkerCreatedEvent(marker, false);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }

            database.getMarkersTable().create(marker);
            return true;
        } catch (Exception exception) {
            Logging.warn(String.format("Error during creating marker by API: '%s'", exception.getMessage()));
            return false;
        }
    }

    @Override
    public Set<Territory> getTerritories() {
        if (!database.isConnected()) return Set.of();
        try {
            return new HashSet<>(database.getTerritoriesTable().queryForAll());
        } catch (Exception exception) {
            Logging.warn(String.format("Error during getting territories by API: '%s'", exception.getMessage()));
            return Set.of();
        }
    }

    @Override
    public boolean updateTerritory(Territory territory) {
        if (!database.isConnected()) return false;
        try {
            TerritoryUpdatedEvent event = new TerritoryUpdatedEvent(territory, false);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }

            database.getTerritoriesTable().update(territory);
            return true;
        } catch (Exception exception) {
            Logging.warn(String.format("Error during updating territory by API: '%s'", exception.getMessage()));
            return false;
        }
    }

    @Override
    public boolean deleteTerritory(Territory territory) {
        if (!database.isConnected()) return false;
        try {
            TerritoryDeletedEvent event = new TerritoryDeletedEvent(territory, false);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }

            database.getTerritoriesTable().deleteById(territory.getUUID().toString());
            return true;
        } catch (Exception exception) {
            Logging.warn(String.format("Error during deleting territory by API: '%s'", exception.getMessage()));
            return false;
        }
    }

    @Override
    public boolean createTerritory(Territory territory) {
        if (!database.isConnected()) return false;
        try {
            TerritoryCreatedEvent event = new TerritoryCreatedEvent(territory, false);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }

            database.getTerritoriesTable().create(territory);
            return true;
        } catch (Exception exception) {
            Logging.warn(String.format("Error during creating territory by API: '%s'", exception.getMessage()));
            return false;
        }
    }
}
