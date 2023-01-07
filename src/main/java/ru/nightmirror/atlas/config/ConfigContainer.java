package ru.nightmirror.atlas.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import ru.nightmirror.atlas.interfaces.config.ConfigLoader;
import ru.nightmirror.atlas.interfaces.config.IConfigContainer;
import ru.nightmirror.atlas.misc.Logging;

import java.io.File;

public class ConfigContainer implements ConfigLoader, IConfigContainer {

    private final Plugin plugin;
    private Config baseConfig;
    private Config markersConfig;
    private Config territoriesConfig;

    public ConfigContainer(Plugin plugin) {
        this.plugin = plugin;
        load();
    }

    @Override
    public boolean reload() {
        Logging.info("Reloading configs");
        return load();
    }

    @Override
    public Config getBase() {
        return baseConfig;
    }

    @Override
    public Config getTerritories() {
        return territoriesConfig;
    }

    @Override
    public Config getMarkers() {
        return markersConfig;
    }

    private boolean load() {
        baseConfig = new Config(loadConfig("config.yml"));
        markersConfig = new Config(loadConfig("markers.yml"));
        territoriesConfig = new Config(loadConfig("territories.yml"));

        if (baseConfig == null || markersConfig == null || territoriesConfig == null) {
            Logging.warn("Some config is null. Disabling...");
            plugin.getPluginLoader().disablePlugin(plugin);
            return false;
        }
        return true;
    }

    private FileConfiguration loadConfig(String fileName) {
        try {
            File file = new File(plugin.getDataFolder(), fileName);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                plugin.saveResource(fileName, false);
            }

            FileConfiguration yaml = new YamlConfiguration();
            yaml.load(file);
            Logging.debug(this, String.format("Loaded config '%s'", fileName));
            return yaml;
        } catch (Exception exception) {
            Logging.error(String.format("Can't load config '%s' error: '%s'", fileName, exception.getMessage()));
        }
        return null;
    }
}
