package ru.nightmirror.atlas.interfaces.managers;

import org.bukkit.entity.Player;
import ru.nightmirror.atlas.database.tables.Territory;
import ru.nightmirror.atlas.interfaces.controllers.IPlayerController;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

public interface ITerritoryManager extends Manager {
    IPlayerController getPlayerController();
    boolean createNew(Player player);
    boolean editName(Player player, UUID id);
    boolean remove(UUID id);
    boolean editDescription(Player player, UUID id);
    boolean isOwner(UUID playerUUID, UUID id);
    boolean cancel(UUID playerUUID);
    boolean isExists(UUID id);
    boolean isProcessing(UUID uuid);
    int countOfTerritories(UUID playerUUID);
    @Nullable Territory getTerritory(UUID id);
    Set<Territory> getTerritories(UUID ownerUUID);
    Set<Territory> getTerritories();
}
