package ru.nightmirror.atlas.controllers;

import org.bukkit.plugin.java.JavaPlugin;
import ru.nightmirror.atlas.interfaces.api.AtlasAPI;

public class AwesomePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().isPluginEnabled("Atlas")) {
            AtlasAPI api = (AtlasAPI) getServer().getPluginManager().getPlugin("Atlas");
        } else {
            getLogger().info("Atlas is not enabled!");
        }
    }
}
