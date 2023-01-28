package ru.nightmirror.atlas.config;

import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import ru.nightmirror.atlas.misc.convertors.ColorsConvertor;

import java.util.List;

@RequiredArgsConstructor
public class Config {

    private final FileConfiguration config;

    public String getString(String path, String def) {
        return ColorsConvertor.convert(config.getString(path, def));
    }

    public String getString(String path) {
        return getString(path, "");
    }

    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }

    public int getInt(String path) {
        return getInt(path, -1);
    }

    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    public boolean getBoolean(String path) {
        return getBoolean(path, false);
    }

    public List<String> getList(String path) {
        return ColorsConvertor.convert(config.getStringList(path));
    }

    public ConfigurationSection getSection(String path) {
        return config.getConfigurationSection(path);
    }
}
