package ru.nightmirror.atlas.interfaces.managers;

import org.bukkit.entity.Player;
import ru.nightmirror.atlas.interfaces.controllers.IPlayerController;
import ru.nightmirror.atlas.misc.type.Type;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

public interface Manager<T> {
    Manager load();
    void stop();
    boolean reload();

    boolean createNew(Player player);
    boolean editName(Player player, UUID id);
    boolean remove(UUID id);
    boolean editDescription(Player player, UUID id);
    boolean isOwner(UUID playerUUID, UUID id);
    boolean cancel(UUID playerUUID);
    boolean isExists(UUID id);
    boolean isProcessing(UUID uuid);

    int countByOwnerUUID(UUID playerUUID);

    @Nullable
    T getById(UUID id);
    Set<T> getByOwnerUUID(UUID ownerUUID);
    Set<T> getByOwnerUUID();
    IPlayerController getPlayerController();
    @Nullable
    Type getType(String raw);

    void setSelectedType(Player player, String... rawType);
}
