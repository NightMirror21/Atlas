package ru.nightmirror.atlas.interfaces;

import org.bukkit.plugin.Plugin;
import ru.nightmirror.atlas.interfaces.config.IConfigContainer;
import ru.nightmirror.atlas.interfaces.managers.IMarkersManager;
import ru.nightmirror.atlas.interfaces.managers.ITerritoryManager;

public interface IAtlas {
    Plugin getPlugin();
    boolean reload();
    ITerritoryManager getTerritories();
    IMarkersManager getMarkers();
    IConfigContainer getConfigContainer();
}
