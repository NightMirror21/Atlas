package ru.nightmirror.atlas.interfaces.config;

import ru.nightmirror.atlas.config.Config;

public interface IConfigContainer {
    Config getBase();
    Config getTerritories();
    Config getMarkers();
}
