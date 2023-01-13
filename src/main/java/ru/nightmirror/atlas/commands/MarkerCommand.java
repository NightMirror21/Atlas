package ru.nightmirror.atlas.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import ru.nightmirror.atlas.config.Config;
import ru.nightmirror.atlas.database.tables.Marker;
import ru.nightmirror.atlas.interfaces.config.IConfigContainer;
import ru.nightmirror.atlas.interfaces.managers.IMarkersManager;
import ru.nightmirror.atlas.misc.utils.PlayerUtils;
import ru.nightmirror.atlas.misc.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MarkerCommand extends BaseMessages implements TabExecutor {

    private final Config config;
    private final Config baseConfig;
    private final IMarkersManager manager;

    public MarkerCommand(IConfigContainer container, IMarkersManager manager) {
        super(container.getBase());
        this.config = container.getMarkers();
        this.baseConfig = container.getBase();
        this.manager = manager;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender, "marker.player", "marker.admin"))
            return true;

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0]) {
            case "create":
                processCreate((Player) sender);
                break;
            case "list":
                processList((Player) sender, args);
                break;
            case "info":
                processInfo((Player) sender, args);
                break;
            case "remove":
                processRemove((Player) sender, args);
                break;
            case "edit":
                processEdit((Player) sender, args);
                break;
            default:
                sendHelp(sender);
        }

        return true;
    }

    private void processList(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(baseConfig.getString("messages.no-enough-arguments"));
            return;
        }

        int page = 0;
        if (args.length > 2 && isNumberWithoutMessage(args[2])) {
            page = Integer.parseInt(args[2]);
            if (page < 0) page = 0;
        }

        switch (args[1]) {
            case "own":
                sendListPage(player, new ArrayList<>(manager.getMarkers(player.getUniqueId())), page, "own");
                break;
            case "all":
                sendListPage(player, new ArrayList<>(manager.getMarkers()), page, "all");
                break;
            default:
                UUID playerUUID = PlayerUtils.uuidFromNickname(args[1]);
                if (playerUUID == null) {
                    sendListPage(player, List.of(), page, "");
                } else {
                    sendListPage(player, new ArrayList<>(manager.getMarkers(playerUUID)), page, args[1]);
                }
        }
    }

    private void sendListPage(Player player, List<Marker> markers, int page, String type) {
        if (markers.size() == 0) {
            player.sendMessage(config.getString("messages.list-is-empty"));
            return;
        }

        int maxPage = markers.size() % 5 == 0 ? markers.size() / 5 : markers.size() / 5 + 1;

        if (page * 5 > markers.size())
            page = 0;

        if (page < 0)
            page = maxPage;

        player.sendMessage(config.getString("messages.list-header"));

        for (int i = 5 * page; i < Math.min((5 * page + 5), markers.size()); i++) {
            Marker marker = markers.get(i);
            TextComponent item = new TextComponent(setPlaceholders(config.getString("messages.list-item"), marker) + " ");
            TextComponent button = createButton(config.getString("messages.list-more-button"), config.getString("messages.list-more-button-hover"), ClickEvent.Action.RUN_COMMAND, "marker info " + marker.getUUID());
            item.addExtra(button);
            player.spigot().sendMessage(item);
        }

        TextComponent previousPage = createButton(config.getString("messages.list-previous-page-button"), config.getString("messages.list-next-page-button-hover"), ClickEvent.Action.RUN_COMMAND, String.format("marker list %s %d", type, page - 1));
        TextComponent nextPage = createButton(config.getString("messages.list-previous-page-button"), config.getString("messages.list-next-page-button-hover"), ClickEvent.Action.RUN_COMMAND, String.format("marker list %s %d", type, page + 1));
        TextComponent separator = new TextComponent(config.getString("messages.list-page-buttons-separator")
                .replaceAll("%current_page%", String.valueOf(page + 1))
                .replaceAll("%max_pages%", String.valueOf(maxPage)));

        previousPage.addExtra(separator);
        previousPage.addExtra(nextPage);

        player.spigot().sendMessage(previousPage);
    }

    private void processCreate(Player player) {
        if (manager.isProcessing(player.getUniqueId())) {
            player.sendMessage(config.getString("messages.errors.already-running-cancel-offer"));
            player.spigot().sendMessage(buildCancelButton());
            return;
        }

        if (manager.getMarkers(player.getUniqueId()).size() >= config.getInt("settings.maximum-per-player")) {
            player.sendMessage(config.getString("messages.errors.limit-is-reached"));
            return;
        }

        manager.createNew(player);
    }

    private void processInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(baseConfig.getString("messages.no-enough-arguments"));
            return;
        }

        UUID uuid = null;
        try {
            uuid = UUID.fromString(args[1]);
        } catch (Exception exception) {
            player.sendMessage(baseConfig.getString("messages.argument-incorrect"));
            return;
        }

        if (!manager.isExists(uuid)) {
            player.sendMessage(config.getString("messages.errors.marker-not-exist"));
            return;
        }

        Marker marker = manager.getMarker(uuid);

        config.getList("messages.info").forEach(line -> {
            line = setPlaceholders(line, marker);
            player.sendMessage(line);
        });

        TextComponent bottom = createButton(config.getString("messages.info-id-copy-button") + " ", config.getString("messages.info-id-copy-button-hover"), ClickEvent.Action.COPY_TO_CLIPBOARD, uuid.toString());

        if (hasPermissionWithoutMessage(player, "marker.admin") || manager.isOwner(player.getUniqueId(), uuid)) {
            bottom.addExtra(createButton(config.getString("messages.remove-button"), config.getString("messages.remove-button-hover"), ClickEvent.Action.RUN_COMMAND, "marker remove " + uuid.toString()));
        }

        player.spigot().sendMessage(bottom);
    }

    private String setPlaceholders(String text, Marker marker) {
        return text.replaceAll("%marker_name%", marker.getName())
                .replaceAll("%marker_description%", marker.getDescription())
                .replaceAll("%marker_owner%", PlayerUtils.nicknameFromUUID(UUID.fromString(marker.getOwnerUUID())))
                .replaceAll("%marker_updated%", TimeUtils.getFormattedTime(marker.getUpdatedAt()))
                .replaceAll("%marker_created%", TimeUtils.getFormattedTime(marker.getUpdatedAt()));
    }

    private void processRemove(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(baseConfig.getString("messages.no-enough-arguments"));
            return;
        }

        UUID uuid = null;
        try {
            uuid = UUID.fromString(args[1]);
        } catch (Exception exception) {
            player.sendMessage(baseConfig.getString("messages.argument-incorrect"));
            return;
        }

        if (manager.isExists(uuid)) {
            if (!hasPermissionWithoutMessage(player, "marker.admin") && !manager.isOwner(player.getUniqueId(), uuid)) {
                player.sendMessage(config.getString("messages.errors.marker-not-your"));
                return;
            }
        } else {
            player.sendMessage(config.getString("messages.errors.marker-not-exist"));
            return;
        }

        if (manager.remove(uuid)) {
            player.sendMessage(config.getString("messages.removed-successfully"));
        } else {
            player.sendMessage(baseConfig.getString("messages.some-errors"));
        }
    }

    private void processEdit(Player player, String[] args) {
        if (manager.isProcessing(player.getUniqueId())) {
            player.sendMessage(config.getString("messages.errors.already-running-cancel-offer"));
            player.spigot().sendMessage(buildCancelButton());
            return;
        }

        if (manager.getMarkers(player.getUniqueId()).size() == 0) {
            player.sendMessage(config.getString("messages.errors.no-have-markers"));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(baseConfig.getString("messages.no-enough-arguments"));
            return;
        }

        UUID uuid = null;
        try {
            uuid = UUID.fromString(args[1]);
        } catch (Exception exception) {
            player.sendMessage(baseConfig.getString("messages.argument-incorrect"));
            return;
        }

        if (manager.isExists(uuid)) {
            if (!player.hasPermission("marker.admin") && !manager.isOwner(player.getUniqueId(), uuid)) {
                player.sendMessage(config.getString("messages.errors.marker-not-your"));
                return;
            }
        } else {
            player.sendMessage(config.getString("messages.errors.marker-not-exist"));
            return;
        }

        switch (args[2]) {
            case "name":
                manager.editName(player, uuid);
                break;
            case "desc":
                manager.editDescription(player, uuid);
                break;
            default:
                player.sendMessage(baseConfig.getString("messages.argument-incorrect"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> strings = new ArrayList<>();

        if (args.length == 1 && hasPermissionWithoutMessage(sender, "marker.player", "marker.admin")) {
            strings.addAll(List.of("create", "list", "info", "remove", "edit"));
        } else if (args.length == 2) {
            switch (args[0]) {
                case "list":
                    strings.addAll(List.of("own", "all"));
                    strings.addAll(manager.getMarkers().stream().map(Marker::getUUID).collect(Collectors.toList()));
                    break;
                case "info":
                    strings.addAll(manager.getMarkers().stream().map(Marker::getUUID).collect(Collectors.toList()));
                    break;
                case "remove":
                case "edit":
                    if (hasPermissionWithoutMessage(sender, "marker.admin")) {
                        strings.addAll(manager.getMarkers().stream().map(Marker::getUUID).collect(Collectors.toList()));
                    } else {
                        strings.addAll(manager.getMarkers(((Player) sender).getUniqueId()).stream().map(Marker::getUUID).collect(Collectors.toList()));
                    }
                    break;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("edit")) {
                strings.addAll(List.of("name", "desc"));
            }
        }

        return strings;
    }

    private void sendHelp(CommandSender sender) {
        config.getList("messages.help").forEach(sender::sendMessage);
    }

    private TextComponent buildCancelButton() {
        return createButton(config.getString("messages.cancel-button"), config.getString("cancel-button-hover"), ClickEvent.Action.RUN_COMMAND, "marker cancel");
    }
}
