package ru.nightmirror.atlas.managers;

import com.j256.ormlite.dao.Dao;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import ru.nightmirror.atlas.commands.BaseMessages;
import ru.nightmirror.atlas.config.Config;
import ru.nightmirror.atlas.controllers.PlayerController;
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
    private final PlayerController controller;
    private Dao<Marker, String> data;

    private Config config;

    private final HashMap<UUID, Marker> processing = new HashMap<>();

    public MarkersManager(IConfigContainer configContainer, DatabaseLoader loader, PlayerController controller) {
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
        player.sendMessage(config.getString("messages.write-name"));
        controller.addTextWroteCallback(player.getUniqueId(), (sender, name) -> {
            if (checkCorrectNameLength(sender, name)) return false;

            Marker marker = new Marker();
            marker.setUUID(UUID.randomUUID().toString());
            marker.setOwnerUUID(player.getUniqueId().toString());
            marker.setName(name.trim());

            processing.put(player.getUniqueId(), marker);
            processWriteDescriptionCreateNew(player);
            return false;
        });
    }

    private void processWriteDescriptionCreateNew(Player player) {
        player.sendMessage(config.getString("messages.write-description"));
        controller.addTextWroteCallback(player.getUniqueId(), (sender, description) -> {
            if (checkCorrectDescriptionLength(sender, description)) return false;

            Marker marker = processing.get(player.getUniqueId());
            marker.setDescription(description);
            processing.put(player.getUniqueId(), marker);
            processSelectPointCreateNew(player);
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

    private void processSelectPointCreateNew(Player player) {
        player.sendMessage(config.getString("messages.mark-point"));
        controller.addPointSelectedCallback(player.getUniqueId(), (sender, block) -> {
            Marker marker = processing.get(player.getUniqueId());
            marker.setPoint(block.getLocation());
            marker.setCreatedAt(System.currentTimeMillis());
            try {
                data.createIfNotExists(marker);
                player.sendMessage(config.getString("messages.created-successfully"));
            } catch (Exception exception) {
                Logging.error(String.format("Can't create marker cause '%s'", exception.getMessage()));
                player.sendMessage(configContainer.getBase().getString("messages.some-errors"));
            }
            processing.remove(player.getUniqueId());
            return true;
        });
    }

    @Override
    public boolean editName(Player player, UUID id) {
        if (!isExists(id) || data == null) {
            return false;
        }

        try {
            Marker marker = data.queryForId(id.toString());
            player.sendMessage(config.getString("messages.write-name"));
            controller.addTextWroteCallback(player.getUniqueId(), (sender, name) -> {
                if (checkCorrectNameLength(sender, name)) return false;

                try {
                    marker.setName(name);
                    data.update(marker);
                    player.sendMessage(config.getString("messages.edited-successfully"));
                } catch (Exception exception) {
                    Logging.error(String.format("Can't update marker cause '%s'", exception.getMessage()));
                    player.sendMessage(configContainer.getBase().getString("messages.some-errors"));
                }
                return true;
            });
            return true;
        } catch (Exception exception) {
            Logging.error(String.format("Can't get marker cause '%s'", exception.getMessage()));
            return false;
        }
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
            return true;
        } catch (Exception exception) {
            Logging.error(String.format("Can't remove marker cause '%s'", exception.getMessage()));
            return false;
        }
    }

    @Override
    public boolean editDescription(Player player, UUID id) {
        if (!isExists(id) || data == null) {
            return false;
        }

        try {
            Marker marker = data.queryForId(id.toString());
            player.sendMessage(config.getString("messages.write-description"));
            controller.addTextWroteCallback(player.getUniqueId(), (sender, description) -> {
                if (checkCorrectDescriptionLength(sender, description)) return false;

                try {
                    marker.setDescription(description);
                    data.update(marker);
                    player.sendMessage(config.getString("messages.edited-successfully"));
                } catch (Exception exception) {
                    Logging.error(String.format("Can't update marker cause '%s'", exception.getMessage()));
                    player.sendMessage(configContainer.getBase().getString("messages.some-errors"));
                }
                return true;
            });
            return true;
        } catch (Exception exception) {
            Logging.error(String.format("Can't get marker cause '%s'", exception.getMessage()));
            return false;
        }
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
        config = configContainer.getMarkers();
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

    private TextComponent buildCancelButton() {
        return createButton(config.getString("messages.cancel-button"), config.getString("cancel-button-hover"), ClickEvent.Action.RUN_COMMAND, "marker cancel");
    }
}
