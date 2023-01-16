package ru.nightmirror.atlas.managers;

import com.j256.ormlite.dao.Dao;
import org.bukkit.entity.Player;
import ru.nightmirror.atlas.commands.BaseMessages;
import ru.nightmirror.atlas.database.DatabaseLoader;
import ru.nightmirror.atlas.database.tables.Marker;
import ru.nightmirror.atlas.interfaces.config.IConfigContainer;
import ru.nightmirror.atlas.interfaces.controllers.IPlayerController;
import ru.nightmirror.atlas.interfaces.managers.IMarkersManager;
import ru.nightmirror.atlas.interfaces.managers.Manager;
import ru.nightmirror.atlas.misc.Logging;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MarkersManager extends BaseMessages implements IMarkersManager {

    private final IConfigContainer configContainer;
    private final DatabaseLoader loader;
    private final IPlayerController controller;
    private Dao<Marker, String> data;

    private final HashMap<UUID, Marker> processing = new HashMap<>();

    public MarkersManager(IConfigContainer configContainer, DatabaseLoader loader, IPlayerController controller) {
        super(configContainer.getBase());
        this.configContainer = configContainer;
        this.loader = loader;
        this.controller = controller;
        load();
    }

    @Override
    public IPlayerController getPlayerController() {
        return controller;
    }

    @Override
    public boolean createNew(Player player) {

        return true;
    }

    @Override
    public boolean editName(Player player, UUID id) {
        // TODO
        return false;
    }

    @Override
    public boolean remove(UUID id) {
        // TODO
        return false;
    }

    @Override
    public boolean editDescription(Player player, UUID id) {
        // TODO
        return false;
    }

    @Override
    public boolean isOwner(UUID playerUUID, UUID id) {
        Marker marker = getById(id);
        return marker != null && marker.getOwnerUUID().equals(playerUUID.toString());
    }

    @Override
    public boolean cancel(UUID playerUUID) {
        boolean contains = controller.containsAnyCallback(playerUUID) || processing.containsKey(playerUUID);
        processing.remove(playerUUID);
        controller.removeAllCallbacks(playerUUID);
        return contains;
    }

    @Override
    public boolean isProcessing(UUID uuid) {
        return processing.containsKey(uuid) || controller.containsAnyCallback(uuid);
    }

    @Override
    public boolean isExists(UUID id) {
        try {
            return data.idExists(id.toString());
        } catch (Exception exception) {
            Logging.warn("Error during checking marker id: " + exception.getMessage());
            exception.printStackTrace();
            return false;
        }
    }

    @Override
    public int countByOwnerUUID(UUID playerUUID) {
        try {
            return data.queryForEq("owner_uuid", playerUUID.toString()).size();
        } catch (Exception exception) {
            Logging.warn("Error during getting count of markers: " + exception.getMessage());
            exception.printStackTrace();
            return 0;
        }
    }

    @Nullable
    @Override
    public Marker getById(UUID id) {
        try {
            return data.queryForId(id.toString());
        } catch (Exception exception) {
            Logging.warn("Error during getting marker: " + exception.getMessage());
            exception.printStackTrace();
            return null;
        }
    }

    @Override
    public Set<Marker> getByOwnerUUID(UUID ownerUUID) {
        try {
            return new HashSet<>(data.queryForEq("owner_uuid", ownerUUID.toString()));
        } catch (Exception exception) {
            Logging.warn("Error during getting markers by owner: " + exception.getMessage());
            exception.printStackTrace();
            return Set.of();
        }
    }

    @Override
    public Set<Marker> getByOwnerUUID() {
        try {
            return new HashSet<>(data.queryForAll());
        } catch (Exception exception) {
            Logging.warn("Error during getting all markers: " + exception.getMessage());
            exception.printStackTrace();
            return Set.of();
        }
    }

    @Override
    public Manager load() {
        data = loader.getMarkersTable();
        Logging.debug(this, "Loaded");
        return this;
    }

    @Override
    public void stop() {
        processing.clear();
        data.clearObjectCache();
        data = null;
        Logging.debug(this, "Stopped");
    }

    @Override
    public boolean reload() {
        try {
            stop();
            load();
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
        Logging.debug(this, "Reloaded");
        return true;
    }
}
