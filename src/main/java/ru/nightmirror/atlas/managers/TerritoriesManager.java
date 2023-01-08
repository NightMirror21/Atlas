package ru.nightmirror.atlas.managers;

import com.j256.ormlite.dao.Dao;
import org.bukkit.entity.Player;
import ru.nightmirror.atlas.controllers.intersection.IntersectionChecker;
import ru.nightmirror.atlas.database.DatabaseLoader;
import ru.nightmirror.atlas.database.tables.Territory;
import ru.nightmirror.atlas.interfaces.controllers.IPlayerController;
import ru.nightmirror.atlas.interfaces.managers.ITerritoryManager;
import ru.nightmirror.atlas.interfaces.managers.Manager;
import ru.nightmirror.atlas.misc.Logging;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TerritoriesManager implements ITerritoryManager {

    private final DatabaseLoader loader;
    private final IPlayerController controller;
    private final IntersectionChecker checker = new IntersectionChecker();

    private Dao<Territory, String> data;

    public TerritoriesManager(DatabaseLoader loader, IPlayerController controller) {
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
        // TODO
        return false;
    }

    @Override
    public boolean editName(Player player, UUID id) {
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
        Territory territory = getTerritory(id);
        return territory != null && territory.getOwnerUUID().equals(playerUUID.toString());
    }

    @Override
    public boolean cancel(Player player) {
        boolean contains = controller.containsAnyCallback(player.getUniqueId());
        controller.removeAllCallbacks(player.getUniqueId());
        return contains;
    }

    @Override
    public boolean isExists(UUID id) {
        try {
            return data.idExists(id.toString());
        } catch (Exception exception) {
            Logging.warn("Error during checking territory id: " + exception.getMessage());
            exception.printStackTrace();
            return false;
        }
    }

    @Override
    public int countOfTerritories(UUID playerUUID) {
        try {
            return data.queryForEq("owner_uuid", playerUUID.toString()).size();
        } catch (Exception exception) {
            Logging.warn("Error during getting count of territories: " + exception.getMessage());
            exception.printStackTrace();
            return 0;
        }
    }

    @Nullable
    @Override
    public Territory getTerritory(UUID id) {
        try {
            return data.queryForId(id.toString());
        } catch (Exception exception) {
            Logging.warn("Error during getting territory: " + exception.getMessage());
            exception.printStackTrace();
            return null;
        }
    }

    @Override
    public Set<Territory> getTerritories(UUID ownerUUID) {
        try {
            return new HashSet<>(data.queryForEq("owner_uuid", ownerUUID.toString()));
        } catch (Exception exception) {
            Logging.warn("Error during getting territory by owner: " + exception.getMessage());
            exception.printStackTrace();
            return Set.of();
        }
    }

    @Override
    public Set<Territory> getTerritories() {
        try {
            return new HashSet<>(data.queryForAll());
        } catch (Exception exception) {
            Logging.warn("Error during getting all territories: " + exception.getMessage());
            exception.printStackTrace();
            return Set.of();
        }
    }

    @Override
    public Manager load() {
        data = loader.getTerritoriesTable();
        Logging.debug(this, "Loaded");
        return this;
    }

    @Override
    public void stop() {
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
