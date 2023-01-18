package ru.nightmirror.atlas.interfaces.managers;

import org.bukkit.entity.Player;
import ru.nightmirror.atlas.database.tables.Territory;

public interface ITerritoryManager extends Manager<Territory> {
    boolean removeSelectedPoint(Player player, int x, int z);
    boolean create(Player player);
}
