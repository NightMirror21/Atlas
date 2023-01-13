package ru.nightmirror.atlas.interfaces.managers;

import org.bukkit.entity.Player;
import ru.nightmirror.atlas.database.tables.Marker;
import ru.nightmirror.atlas.interfaces.controllers.IPlayerController;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

public interface IMarkersManager extends Manager {
    IPlayerController getPlayerController();
    boolean createNew(Player player);
    boolean editName(Player player, UUID id);
    boolean remove(UUID id);
    boolean editDescription(Player player, UUID id);
    boolean isOwner(UUID playerUUID, UUID id);
    boolean cancel(Player playerUUID);
    boolean isExists(UUID id);
    boolean isProcessing(UUID uuid);
    int countOfMarkers(UUID playerUUID);
    @Nullable Marker getMarker(UUID id);
    Set<Marker> getMarkers(UUID ownerUUID);
    Set<Marker> getMarkers();
}
