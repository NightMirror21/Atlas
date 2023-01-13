package ru.nightmirror.atlas.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import ru.nightmirror.atlas.config.Config;
import ru.nightmirror.atlas.interfaces.IAtlas;

import java.util.ArrayList;
import java.util.List;

public class AtlasMessages extends BaseMessages implements TabExecutor {

    private Config config;
    private IAtlas plugin;

    public AtlasMessages(IAtlas plugin) {
        super(plugin.getConfigContainer().getBase());
        this.config = plugin.getConfigContainer().getBase();
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!hasPermission(sender, "atlas.admin"))
            return true;

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0]) {
            case "create":
                sendReload(sender);
                break;
            case "stats":
                sendStats(sender);
                break;
            default:
                sendHelp(sender);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> strings = new ArrayList<>();

        if (args.length == 1 && hasPermissionWithoutMessage(sender, "atlas.admin")) {
            strings.addAll(List.of("help", "reload", "stats"));
        }

        return strings;
    }

    private void sendReload(CommandSender sender) {
        if (plugin.reload()) {
            sender.sendMessage(config.getString("messages.plugin-reloaded-success"));
        } else {
            sender.sendMessage(config.getString("messages.plugin-reloaded-not-success"));
        }
    }

    private void sendHelp(CommandSender sender) {
        config.getList("messages.help").forEach(sender::sendMessage);
    }

    private void sendStats(CommandSender sender) {
        config.getList("messages.stats").forEach(line -> {
            line = line.replaceAll("%territories_count%", String.valueOf(plugin.getTerritories().getTerritories().size()))
                    .replaceAll("%markers_count%", String.valueOf(plugin.getMarkers().getMarkers().size()));
            sender.sendMessage(line);
        });
    }
}
