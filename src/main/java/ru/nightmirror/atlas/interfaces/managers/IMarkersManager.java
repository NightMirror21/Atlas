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
    boolean editDescription(Player player, UUID id);
    boolean isOwner(Player player, UUID id);
    boolean cancel(Player player);
    boolean isExists(UUID id);
    int countOfMarkers(Player player);
    @Nullable Marker getMarker(UUID id);
    Set<Marker> getMarkers(UUID ownerUUID);
    Set<Marker> getMarkers();
}
