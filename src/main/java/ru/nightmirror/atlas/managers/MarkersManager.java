package ru.nightmirror.atlas.managers;

import com.j256.ormlite.dao.Dao;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import ru.nightmirror.atlas.api.MarkerCreatedEvent;
import ru.nightmirror.atlas.api.MarkerDeletedEvent;
import ru.nightmirror.atlas.api.MarkerUpdatedEvent;
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
import ru.nightmirror.atlas.misc.type.Type;

import javax.annotation.Nullable;
import java.util.*;

public class MarkersManager extends BaseMessages implements IMarkersManager {

    private final IConfigContainer configContainer;
    private final DatabaseLoader loader;
    private final PlayerController controller;
    private Dao<Marker, String> data;

    private Config config;

    private final HashMap<UUID, Marker> processing = new HashMap<>();
    private final LinkedHashSet<Type> markerTypes = new LinkedHashSet<>();

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

            processSelectType(player);

            return true;
        });
    }

    private void processSelectType(Player player) {
        player.sendMessage(config.getString("messages.select-type"));

        for (Type type : markerTypes) {
            String name = config.getString("messages.select-type-item-button").replaceAll("%type_name%", type.getColor().getMinecraftColor() + type.getName());
            TextComponent button = createButton(name,  config.getString("messages.select-type-item-button-hover"), ClickEvent.Action.RUN_COMMAND, ("/marker settype " + type.getName()));
            player.spigot().sendMessage(button);
        }
    }

    @Override
    public boolean update(Marker value) {
        try {
            if (isExists(UUID.fromString(value.getUUID()))) {
                MarkerUpdatedEvent event = new MarkerUpdatedEvent(value, true);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    return false;
                }

                data.update(value);
                Logging.debug(this, String.format("Marker '%s' updated", value.getUUID().toString()));
                return true;
            }
        } catch (Exception exception) {
            Logging.error(String.format("Can't update marker cause '%s'", exception.getMessage()));
        }
        return false;
    }

    @Override
    public void setSelectedType(Player player, String... rawType) {
        Type type = getType(toStr(rawType));
        if (type == null || !processing.containsKey(player.getUniqueId()))
            return;

        Marker marker = processing.get(player.getUniqueId());
        if (marker.getType() != null) return;
        marker.setType(type.getName());
        processing.put(player.getUniqueId(), marker);
        processSelectPointCreateNew(player);
    }

    private String toStr(String[] arr) {
        StringBuilder result = new StringBuilder();
        for (String str : arr) {
            result.append(str).append(" ");
        }
        return result.toString().trim();
    }

    @Nullable
    @Override
    public Type getType(String raw) {
        for (Type type : markerTypes) {
            if (type.getName().equalsIgnoreCase(raw)) {
                return type;
            }
        }
        return null;
    }

    private boolean checkCorrectDescriptionLength(Player sender, String description) {
        if (description.length() < config.getInt("settings.min-symbols-description-length")) {
            sender.sendMessage(config.getString("messages.errors.description-is-too-short"));
            sender.spigot().sendMessage(buildCancelButton());
            return true;
        }

        if (description.length() > config.getInt("settings.max-symbols-description-length")) {
            sender.sendMessage(config.getString("messages.errors.description-is-too-long"));
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
            marker.setUpdatedAt(System.currentTimeMillis());
            try {
                MarkerCreatedEvent event = new MarkerCreatedEvent(marker, true);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    return true;
                }

                data.create(marker);
                player.sendMessage(config.getString("messages.created-successfully"));
                Logging.debug(this, String.format("Marker '%s' created", marker.getUUID().toString()));
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

                marker.setName(name);
                marker.setUpdatedAt(System.currentTimeMillis());
                if (update(marker)) {
                    player.sendMessage(config.getString("messages.edited-successfully"));
                } else {
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
            sender.sendMessage(config.getString("messages.errors.name-is-too-short"));
            sender.spigot().sendMessage(buildCancelButton());
            return true;
        }

        if (name.length() > config.getInt("settings.max-symbols-name-length")) {
            sender.sendMessage(config.getString("messages.errors.name-is-too-long"));
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
            MarkerDeletedEvent event = new MarkerDeletedEvent(getById(id), true);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }

            data.deleteById(id.toString());
            Logging.debug(this, String.format("Marker '%s' deleted", id.toString()));
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
                marker.setDescription(description);
                marker.setUpdatedAt(System.currentTimeMillis());

                if (update(marker)) {
                    player.sendMessage(config.getString("messages.edited-successfully"));
                } else {
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
    public Set<Marker> getByOwner(UUID ownerUUID) {
        try {
            return new HashSet<>(data.queryForEq("owner_uuid", ownerUUID.toString()));
        } catch (Exception exception) {
            Logging.warn("Error during getting markers by owner: " + exception.getMessage());
            exception.printStackTrace();
            return Set.of();
        }
    }

    @Override
    public Set<Marker> getAll() {
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
        loadTypes();
        return this;
    }

    private void loadTypes() {
        ConfigurationSection section = config.getSection("types");
        if (section == null) {
            Logging.error("Can't load types of markers. Section is null");
            return;
        }

        section.getKeys(false).forEach(key -> {
            ConfigurationSection typeSection = section.getConfigurationSection(key);
            Type type = new Type(typeSection.getString("name"), typeSection.getString("color"));
            markerTypes.add(type);
        });

        Logging.debug(this, String.format("Loaded %d types", markerTypes.size()));
    }

    @Override
    public void stop() {
        processing.clear();
        markerTypes.clear();
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
        return createButton(config.getString("messages.cancel-button"), config.getString("messages.cancel-button-hover"), ClickEvent.Action.RUN_COMMAND, "/marker cancel");
    }
}
