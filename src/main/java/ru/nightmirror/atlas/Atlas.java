package ru.nightmirror.atlas;

import org.bukkit.plugin.java.JavaPlugin;
import ru.nightmirror.atlas.config.ConfigContainer;
import ru.nightmirror.atlas.interfaces.IAtlas;
import ru.nightmirror.atlas.misc.Logging;

public class Atlas extends JavaPlugin implements IAtlas {

    @Override
    public void onEnable() {
        ConfigContainer configContainer = new ConfigContainer(this);
        Logging.info("Enabled");
    }

    @Override
    public void onDisable() {
        Logging.info("Disabled");
    }
}
