package ru.nightmirror.atlas;

import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.nightmirror.atlas.commands.AtlasCommand;
import ru.nightmirror.atlas.config.ConfigContainer;
import ru.nightmirror.atlas.controllers.PlayerController;
import ru.nightmirror.atlas.database.DatabaseLoader;
import ru.nightmirror.atlas.interfaces.IAtlas;
import ru.nightmirror.atlas.interfaces.config.IConfigContainer;
import ru.nightmirror.atlas.interfaces.database.Database;
import ru.nightmirror.atlas.interfaces.managers.IMarkersManager;
import ru.nightmirror.atlas.interfaces.managers.ITerritoryManager;
import ru.nightmirror.atlas.interfaces.managers.Manager;
import ru.nightmirror.atlas.managers.MarkersManager;
import ru.nightmirror.atlas.managers.TerritoriesManager;
import ru.nightmirror.atlas.misc.Logging;

public class Atlas extends JavaPlugin implements IAtlas {

    private ConfigContainer configContainer;
    private Manager markers;
    private Manager territories;
    private Database database;

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

        markers = new MarkersManager((DatabaseLoader) database, controller);
        territories = new TerritoriesManager((DatabaseLoader) database, controller);

        registerCommands();

        Logging.info("Enabled");
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
    public boolean reload() {
        return !configContainer.reload() || !database.reload() || !markers.reload() || !territories.reload();
    }

    @Override
    public ITerritoryManager getTerritories() {
        return (ITerritoryManager) territories;
    }

    @Override
    public IMarkersManager getMarkers() {
        return (IMarkersManager) markers;
    }

    @Override
    public IConfigContainer getConfigContainer() {
        return configContainer;
    }

    private void registerCommands() {
        TabExecutor atlas = new AtlasCommand(this);
        getCommand("atlas").setExecutor(atlas);
        getCommand("atlas").setTabCompleter(atlas);
    }
}
