package ru.nightmirror.atlas.managers;

import com.j256.ormlite.dao.Dao;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.nightmirror.atlas.commands.BaseMessages;
import ru.nightmirror.atlas.config.Config;
import ru.nightmirror.atlas.controllers.PlayerController;
import ru.nightmirror.atlas.controllers.intersection.IntersectionChecker;
import ru.nightmirror.atlas.database.DatabaseLoader;
import ru.nightmirror.atlas.database.tables.Territory;
import ru.nightmirror.atlas.interfaces.config.IConfigContainer;
import ru.nightmirror.atlas.interfaces.controllers.IPlayerController;
import ru.nightmirror.atlas.interfaces.managers.ITerritoryManager;
import ru.nightmirror.atlas.interfaces.managers.Manager;
import ru.nightmirror.atlas.misc.Logging;
import ru.nightmirror.atlas.misc.PointsConvertor;
import ru.nightmirror.atlas.models.Area;
import ru.nightmirror.atlas.models.Point;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class TerritoriesManager extends BaseMessages implements ITerritoryManager {

    private final IConfigContainer configContainer;
    private final DatabaseLoader loader;
    private final PlayerController controller;
    private final IntersectionChecker checker = new IntersectionChecker();
    private Dao<Territory, String> data;

    private Config config;

    private final HashMap<UUID, Territory> processing = new HashMap<>();

    public TerritoriesManager(IConfigContainer configContainer, DatabaseLoader loader, PlayerController controller) {
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
        if (countByOwnerUUID(player.getUniqueId()) >= config.getInt("settings.maximum-per-player") || data == null) {
            return false;
        }
        processWriteNameCreateNew(player);
        return true;
    }

    private void processWriteNameCreateNew(Player player) {
        Logging.debug(this, String.format("Started creating territory for player '%s'", player.getName()));
        player.sendMessage(config.getString("messages.write-name"));
        controller.addTextWroteCallback(player.getUniqueId(), (sender, name) -> {
            Logging.debug(this, String.format("Attempting to put name '%s' of territory for player '%s'", name, player.getName()));
            if (checkCorrectNameLength(sender, name)) return false;

            Territory territory = new Territory();
            territory.setUUID(UUID.randomUUID().toString());
            territory.setOwnerUUID(player.getUniqueId().toString());
            territory.setName(name.trim());

            processing.put(player.getUniqueId(), territory);
            Logging.debug(this, String.format("Putted name '%s' of territory for player '%s'", name, player.getName()));
            processWriteDescriptionCreateNew(player);
            return false;
        });
    }

    private void processWriteDescriptionCreateNew(Player player) {
        player.sendMessage(config.getString("messages.write-description"));
        controller.addTextWroteCallback(player.getUniqueId(), (sender, description) -> {
            Logging.debug(this, String.format("Attempting to put description '%s' of territory for player '%s'", description, player.getName()));
            if (checkCorrectDescriptionLength(sender, description)) return false;

            Territory territory = processing.get(player.getUniqueId());
            territory.setDescription(description);
            processing.put(player.getUniqueId(), territory);
            Logging.debug(this, String.format("Putted description '%s' of territory for player '%s'", description, player.getName()));
            processSelectPointsCreateNew(player);
            return true;
        });
    }

    private boolean checkCorrectDescriptionLength(Player sender, String description) {
        if (description.length() < config.getInt("settings.min-symbols-description-length")) {
            sender.sendMessage(config.getString("messages.description-is-too-short"));
            sender.spigot().sendMessage(buildCancelButton());
            return true;
        }

        if (description.length() > config.getInt("settings.max-symbols-description-length")) {
            sender.sendMessage(config.getString("messages.description-is-too-long"));
            sender.spigot().sendMessage(buildCancelButton());
            return true;
        }
        return false;
    }

    private void processSelectPointsCreateNew(Player player) {
        player.sendMessage(config.getString("messages.mark-points"));
        controller.addPointSelectedCallback(player.getUniqueId(), (sender, block) -> {
            Logging.debug(this, String.format("Player '%s' clicked on block with x=%d z=%d", player.getName(), block.getLocation().getBlockX(), block.getLocation().getBlockZ()));
            Territory territory = processing.get(player.getUniqueId());
            LinkedHashSet<Location> points = territory.getPoints();
            if (points == null) points = new LinkedHashSet<>();

            // Checking if point in another world
            if (points.size() > 0) {
                Location firstPoint = (Location) points.toArray()[0];
                if (!firstPoint.getWorld().getName().equals(block.getWorld().getName())) {
                    player.sendMessage(config.getString("messages.errors.point-in-different-world"));
                    return false;
                }
            }

            // Sending message
            TextComponent pointMessage = new TextComponent(config.getString("messages.point-marked").replaceAll("%number%", String.valueOf(points.size()+1)) + " ");
            TextComponent deleteButton = createButton(config.getString("messages.point-remove-button"), config.getString("messages.point-remove-button-hover"), ClickEvent.Action.RUN_COMMAND, String.format("territory delete_point %d %d", block.getLocation().getBlockX(), block.getLocation().getBlockZ()));
            pointMessage.addExtra(deleteButton);
            player.spigot().sendMessage(pointMessage);

            // Applying
            points.add(block.getLocation());
            territory.setPoints(points);
            processing.put(player.getUniqueId(), territory);

            Logging.debug(this, String.format("Point putted to future territory '%s'", territory.getUUID().toString()));

            return false;
        });
    }

    @Override
    public boolean editName(Player player, UUID id) {
        if (!isExists(id) || data == null) {
            return false;
        }

        Logging.debug(this, String.format("New edit name process for player '%s' and territory '%s'", player.getName(), id.toString()));

        try {
            Territory territory = data.queryForId(id.toString());
            player.sendMessage(config.getString("messages.write-name"));
            controller.addTextWroteCallback(player.getUniqueId(), (sender, name) -> {
                Logging.debug(this, String.format("Attempting to set name '%s' for territory '%s'", name, id.toString()));
                if (checkCorrectNameLength(sender, name)) return false;

                try {
                    territory.setName(name);
                    data.update(territory);
                    player.sendMessage(config.getString("messages.edited-successfully"));
                    Logging.debug(this, String.format("Setted name '%s' for territory '%s'", name, id.toString()));
                } catch (Exception exception) {
                    Logging.error(String.format("Can't update territory cause '%s'", exception.getMessage()));
                    player.sendMessage(configContainer.getBase().getString("messages.some-errors"));
                }
                return true;
            });
            return true;
        } catch (Exception exception) {
            Logging.error(String.format("Can't get territory cause '%s'", exception.getMessage()));
            return false;
        }
    }

    @Override
    public boolean removeSelectedPoint(Player player, int x, int z) {
        if (!isProcessing(player.getUniqueId())) return false;

        Territory territory = processing.get(player.getUniqueId());
        if (territory.getPoints() == null || territory.getPoints().size() == 0) return false;

        Logging.debug(this, String.format("Attempting to delete point x=%d z=%d for territory '%s' where owner '%s'", x, z, territory.getUUID(), player.getName()));

        LinkedHashSet<Location> newPoints = new LinkedHashSet<>();
        boolean deleted = false;
        for (Location point : territory.getPoints()) {
            if (point.getBlockX() == x && point.getBlockZ() == z) {
                deleted = true;
                continue;
            }
            newPoints.add(point);
        }

        if (deleted) {
            Logging.debug(this, String.format("Removed. Point counts: %d -> %d", territory.getPoints().size(), newPoints.size()));
            territory.setPoints(newPoints);
            processing.put(player.getUniqueId(), territory);
            player.sendMessage(config.getString("messages.point-removed-response"));
            return true;
        } else {
            player.sendMessage(config.getString("messages.errors.no-points-to-remove"));
            return false;
        }
    }

    @Override
    public boolean create(Player player) {
        if (!isProcessing(player.getUniqueId()) || !processing.get(player.getUniqueId()).isReadyToCreate()) return false;

        Territory territory = processing.get(player.getUniqueId());
        LinkedHashSet<Location> points = territory.getPoints();
        if (points.size() < 2) {
            player.sendMessage(config.getString("messages.errors.too-few-points"));
            return false;
        } else if (points.size() == 2) {
            points = PointsConvertor.fromTwoPointMakeSquare(points);
        }

        if (isIntersect(points)) {
            player.sendMessage(config.getString("messages.errors.territory-intersect"));
            return false;
        }

        territory.setCreatedAt(System.currentTimeMillis());
        territory.setUpdatedAt(System.currentTimeMillis());
        territory.setPoints(points);

        try {
            data.create(territory);
            player.sendMessage(config.getString(config.getString("messages.created-successfully")));
            cancel(player.getUniqueId());
        } catch (Exception exception) {
            Logging.error(String.format("Can't create territory cause '%s'", exception.getMessage()));
            player.sendMessage(configContainer.getBase().getString("messages.some-errors"));
        }

        return true;
    }

    private boolean isIntersect(LinkedHashSet<Location> points) {
        if (config.getBoolean("settings.can-intersect-other-territories")) return false;

        Area area = new Area(points.stream().map(point -> new Point(point.getBlockX(), point.getBlockZ())).collect(Collectors.toSet()));

        AtomicBoolean isIntersect = new AtomicBoolean(false);
        try {
            data.queryForAll().forEach(territoryForEach -> {
                if (isIntersect.get()) return;
                Area areaForEach = new Area(territoryForEach.getPoints().stream().map(point -> new Point(point.getBlockX(), point.getBlockZ())).collect(Collectors.toSet()));
                if (checker.isAreaIntersect(area, areaForEach)) {
                    isIntersect.set(true);
                }
            });
        } catch (Exception exception) {
            Logging.warn(String.format("Can't check territory intersect cause '%s'", exception.getMessage()));
        }

        return isIntersect.get();
    }

    private boolean checkCorrectNameLength(Player sender, String name) {
        if (name.length() < config.getInt("settings.min-symbols-name-length")) {
            sender.sendMessage(config.getString("messages.name-is-too-short"));
            sender.spigot().sendMessage(buildCancelButton());
            return true;
        }

        if (name.length() > config.getInt("settings.max-symbols-name-length")) {
            sender.sendMessage(config.getString("messages.name-is-too-long"));
            sender.spigot().sendMessage(buildCancelButton());
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(UUID id) {
        if (!isExists(id) || data == null) {
            return false;
        }

        try {
            data.deleteById(id.toString());
            Logging.debug(this, String.format("Territory '%s' deleted", id.toString()));
            return true;
        } catch (Exception exception) {
            Logging.error(String.format("Can't remove territory cause '%s'", exception.getMessage()));
            return false;
        }
    }

    @Override
    public boolean editDescription(Player player, UUID id) {
        if (!isExists(id) || data == null) {
            return false;
        }

        Logging.debug(this, String.format("New edit description process for player '%s' and territory '%s'", player.getName(), id.toString()));

        try {
            Territory territory = data.queryForId(id.toString());
            player.sendMessage(config.getString("messages.write-description"));
            controller.addTextWroteCallback(player.getUniqueId(), (sender, description) -> {
                Logging.debug(this, String.format("Attempting to set description '%s' for territory '%s'", description, id.toString()));

                if (checkCorrectDescriptionLength(sender, description)) return false;

                try {
                    territory.setDescription(description);
                    data.update(territory);
                    player.sendMessage(config.getString("messages.edited-successfully"));
                    Logging.debug(this, String.format("Setted description '%s' for territory '%s'", description, id.toString()));
                } catch (Exception exception) {
                    Logging.error(String.format("Can't update territory cause '%s'", exception.getMessage()));
                    player.sendMessage(configContainer.getBase().getString("messages.some-errors"));
                }
                return true;
            });
            return true;
        } catch (Exception exception) {
            Logging.error(String.format("Can't get territory cause '%s'", exception.getMessage()));
            return false;
        }
    }

    @Override
    public boolean isOwner(UUID playerUUID, UUID id) {
        Territory territory = getById(id);
        return territory != null && territory.getOwnerUUID().equals(playerUUID.toString());
    }

    @Override
    public boolean cancel(UUID playerUUID) {
        boolean contains = controller.containsAnyCallback(playerUUID) || processing.containsKey(playerUUID);
        processing.remove(playerUUID);
        controller.removeAllCallbacks(playerUUID);
        Logging.debug(this, String.format("Cancelled all for player with uuid '%s'", playerUUID.toString()));
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
            Logging.warn("Error during checking territory id: " + exception.getMessage());
            exception.printStackTrace();
            return false;
        }
    }

    @Override
    public int countByOwnerUUID(UUID playerUUID) {
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
    public Territory getById(UUID id) {
        try {
            return data.queryForId(id.toString());
        } catch (Exception exception) {
            Logging.warn("Error during getting territory: " + exception.getMessage());
            exception.printStackTrace();
            return null;
        }
    }

    @Override
    public Set<Territory> getByOwnerUUID(UUID ownerUUID) {
        try {
            return new HashSet<>(data.queryForEq("owner_uuid", ownerUUID.toString()));
        } catch (Exception exception) {
            Logging.warn("Error during getting territory by owner: " + exception.getMessage());
            exception.printStackTrace();
            return Set.of();
        }
    }

    @Override
    public Set<Territory> getByOwnerUUID() {
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
        config = configContainer.getTerritories();
        Logging.debug(this, "Loaded");
        return this;
    }

    @Override
    public void stop() {
        data.clearObjectCache();
        processing.clear();
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

    private TextComponent buildCancelButton() {
        return createButton(config.getString("messages.cancel-button"), config.getString("cancel-button-hover"), ClickEvent.Action.RUN_COMMAND, "territory cancel");
    }
}
