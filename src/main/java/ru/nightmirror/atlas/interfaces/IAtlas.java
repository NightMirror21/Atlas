package ru.nightmirror.atlas.interfaces;

import org.bukkit.plugin.Plugin;
import ru.nightmirror.atlas.hooks.DynMap;
import ru.nightmirror.atlas.interfaces.api.AtlasAPI;
import ru.nightmirror.atlas.interfaces.config.IConfigContainer;
import ru.nightmirror.atlas.interfaces.managers.IMarkersManager;
import ru.nightmirror.atlas.interfaces.managers.ITerritoryManager;

import javax.annotation.Nullable;

public interface IAtlas {
    Plugin getPlugin();
    AtlasAPI getAPI();
    boolean reload();
    ITerritoryManager getTerritoryManager();
    IMarkersManager getMarkerManager();
    IConfigContainer getConfigContainer();
    String getAtlasVersion();
    @Nullable
    DynMap getDynMap();
}
