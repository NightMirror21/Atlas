package ru.nightmirror.atlas.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import ru.nightmirror.atlas.config.Config;
import ru.nightmirror.atlas.database.tables.Territory;
import ru.nightmirror.atlas.interfaces.config.IConfigContainer;
import ru.nightmirror.atlas.interfaces.managers.ITerritoryManager;
import ru.nightmirror.atlas.misc.utils.PlayerUtils;
import ru.nightmirror.atlas.misc.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TerritoryCommand extends BaseMessages implements TabExecutor {

    private final Config config;
    private final Config baseConfig;
    private final ITerritoryManager manager;

    public TerritoryCommand(IConfigContainer container, ITerritoryManager manager) {
        super(container.getBase());
        this.config = container.getTerritories();
        this.baseConfig = container.getBase();
        this.manager = manager;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender, "territory.player", "territory.admin"))
            return true;

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0]) {
            case "cancel":
                processCancel((Player) sender);
                break;
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
            case "delete_point":
                processDeletePoint((Player) sender, args);
                break;
            default:
                sendHelp(sender);
        }

        return true;
    }

    private void processDeletePoint(Player player, String[] args) {
        if (args.length < 3) return;

        if (!isNumber(player, args[1]) || !isNumber(player, args[2])) return;

        if (manager.isProcessing(player.getUniqueId())) {
            manager.removeSelectedPoint(player, Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        } else {
            player.sendMessage(config.getString("messages.errors.no-to-cancel"));
        }
    }

    private void processCancel(Player player) {
        if (manager.cancel(player.getUniqueId())) {
            player.sendMessage(config.getString("messages.errors.cancelled-response"));
        } else {
            player.sendMessage(config.getString("messages.errors.no-to-cancel"));
        }
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
                sendListPage(player, new ArrayList<>(manager.getByOwnerUUID(player.getUniqueId())), page, "own");
                break;
            case "all":
                sendListPage(player, new ArrayList<>(manager.getByOwnerUUID()), page, "all");
                break;
            default:
                UUID playerUUID = PlayerUtils.uuidFromNickname(args[1]);
                if (playerUUID == null) {
                    sendListPage(player, List.of(), page, "");
                } else {
                    sendListPage(player, new ArrayList<>(manager.getByOwnerUUID(playerUUID)), page, args[1]);
                }
        }
    }

    private void sendListPage(Player player, List<Territory> territories, int page, String type) {
        if (territories.size() == 0) {
            player.sendMessage(config.getString("messages.list-is-empty"));
            return;
        }

        int maxPage = territories.size() % 5 == 0 ? territories.size() / 5 : territories.size() / 5 + 1;

        if (page * 5 > territories.size())
            page = 0;

        if (page < 0)
            page = maxPage;

        player.sendMessage(config.getString("messages.list-header"));

        for (int i = 5 * page; i < Math.min((5 * page + 5), territories.size()); i++) {
            Territory territory = territories.get(i);
            TextComponent item = new TextComponent(setPlaceholders(config.getString("messages.list-item"), territory) + " ");
            TextComponent button = createButton(config.getString("messages.list-more-button"), config.getString("messages.list-more-button-hover"), ClickEvent.Action.RUN_COMMAND, "territory info " + territory.getUUID());
            item.addExtra(button);
            player.spigot().sendMessage(item);
        }

        TextComponent previousPage = createButton(config.getString("messages.list-previous-page-button"), config.getString("messages.list-next-page-button-hover"), ClickEvent.Action.RUN_COMMAND, String.format("territory list %s %d", type, page - 1));
        TextComponent nextPage = createButton(config.getString("messages.list-previous-page-button"), config.getString("messages.list-next-page-button-hover"), ClickEvent.Action.RUN_COMMAND, String.format("territory list %s %d", type, page + 1));
        TextComponent separator = new TextComponent(config.getString("messages.list-page-buttons-separator")
                .replaceAll("%current_page%", String.valueOf(page + 1))
                .replaceAll("%max_pages%", String.valueOf(maxPage)));

        previousPage.addExtra(separator);
        previousPage.addExtra(nextPage);

        player.spigot().sendMessage(previousPage);
    }

    private void processCreate(Player player) {
        if (manager.isProcessing(player.getUniqueId())) {
            if (!manager.create(player)) {
                player.sendMessage(config.getString("messages.errors.already-running-cancel-offer"));
                player.spigot().sendMessage(buildCancelButton());
            }
            return;
        }

        if (manager.getByOwnerUUID(player.getUniqueId()).size() >= config.getInt("settings.maximum-per-player")) {
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
            player.sendMessage(config.getString("messages.errors.territory-not-exist"));
            return;
        }

        Territory territory = manager.getById(uuid);

        config.getList("messages.info").forEach(line -> {
            line = setPlaceholders(line, territory);
            player.sendMessage(line);
        });

        TextComponent bottom = createButton(config.getString("messages.info-id-copy-button") + " ", config.getString("messages.info-id-copy-button-hover"), ClickEvent.Action.COPY_TO_CLIPBOARD, uuid.toString());

        if (hasPermissionWithoutMessage(player, "territory.admin") || manager.isOwner(player.getUniqueId(), uuid)) {
            bottom.addExtra(createButton(config.getString("messages.remove-button"), config.getString("messages.remove-button-hover"), ClickEvent.Action.RUN_COMMAND, "territory remove " + uuid.toString()));
        }

        player.spigot().sendMessage(bottom);
    }

    private String setPlaceholders(String text, Territory territory) {
        Location point = (Location) territory.getPoints().toArray()[0];
        String pointFormatted = config.getString("messages.territory-point-format")
                .replaceAll("%world_name%", point.getWorld().getName())
                .replaceAll("%x%", String.valueOf(point.getBlockX()))
                .replaceAll("%z%", String.valueOf(point.getBlockZ()));

        return text.replaceAll("%territory_name%", territory.getName())
                .replaceAll("%territory_description%", territory.getDescription())
                .replaceAll("%territory_owner%", PlayerUtils.nicknameFromUUID(UUID.fromString(territory.getOwnerUUID())))
                .replaceAll("%territory_updated%", TimeUtils.getFormattedTime(territory.getUpdatedAt()))
                .replaceAll("%territory_point%", pointFormatted)
                .replaceAll("%territory_created%", TimeUtils.getFormattedTime(territory.getUpdatedAt()));
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
            if (!hasPermissionWithoutMessage(player, "territory.admin") && !manager.isOwner(player.getUniqueId(), uuid)) {
                player.sendMessage(config.getString("messages.errors.territory-not-your"));
                return;
            }
        } else {
            player.sendMessage(config.getString("messages.errors.territory-not-exist"));
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

        if (manager.getByOwnerUUID(player.getUniqueId()).size() == 0) {
            player.sendMessage(config.getString("messages.errors.no-have-territories"));
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
            if (!player.hasPermission("territory.admin") && !manager.isOwner(player.getUniqueId(), uuid)) {
                player.sendMessage(config.getString("messages.errors.territory-not-your"));
                return;
            }
        } else {
            player.sendMessage(config.getString("messages.errors.territory-not-exist"));
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

        if (args.length == 1 && hasPermissionWithoutMessage(sender, "territory.player", "territory.admin")) {
            strings.addAll(List.of("create", "list", "info", "remove", "edit"));
        } else if (args.length == 2) {
            switch (args[0]) {
                case "list":
                    strings.addAll(List.of("own", "all"));
                    strings.addAll(manager.getByOwnerUUID().stream().map(Territory::getUUID).collect(Collectors.toList()));
                    break;
                case "info":
                    strings.addAll(manager.getByOwnerUUID().stream().map(Territory::getUUID).collect(Collectors.toList()));
                    break;
                case "remove":
                case "edit":
                    if (hasPermissionWithoutMessage(sender, "territory.admin")) {
                        strings.addAll(manager.getByOwnerUUID().stream().map(Territory::getUUID).collect(Collectors.toList()));
                    } else {
                        strings.addAll(manager.getByOwnerUUID(((Player) sender).getUniqueId()).stream().map(Territory::getUUID).collect(Collectors.toList()));
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
        return createButton(config.getString("messages.cancel-button"), config.getString("cancel-button-hover"), ClickEvent.Action.RUN_COMMAND, "territory cancel");
    }
}
