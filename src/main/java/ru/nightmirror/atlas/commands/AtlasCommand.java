package ru.nightmirror.atlas.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import ru.nightmirror.atlas.config.Config;
import ru.nightmirror.atlas.interfaces.IAtlas;

import java.util.ArrayList;
import java.util.List;

public class AtlasCommand extends BaseMessages implements TabExecutor {

    private Config config;
    private IAtlas plugin;

    public AtlasCommand(IAtlas plugin) {
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
            case "reload":
                sendReload(sender);
                break;
            case "dynmap":
                sendDynmapRefresh(sender);
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

            if (plugin.getDynMap() != null) {
                strings.add("dynmap");
            }
        }

        return strings;
    }

    private void sendDynmapRefresh(CommandSender sender) {
        if (plugin.getDynMap() != null) {
            plugin.getDynMap().refreshAll();
            sender.sendMessage(config.getString("messages.dynmap-reload-success"));
        } else {
            sender.sendMessage(config.getString("messages.dynmap-not-hooked"));
        }
    }

    private void sendReload(CommandSender sender) {
        if (plugin.reload()) {
            sender.sendMessage(config.getString("messages.plugin-reloaded-success"));
        } else {
            sender.sendMessage(config.getString("messages.plugin-reloaded-not-success"));
        }
    }

    private void sendHelp(CommandSender sender) {
        config.getList("messages.help").forEach(line -> {
            sender.sendMessage(line.replaceAll("%plugin_version%", plugin.getAtlasVersion()));
        });
    }

    private void sendStats(CommandSender sender) {
        config.getList("messages.stats").forEach(line -> {
            line = line.replaceAll("%territories_count%", String.valueOf(plugin.getTerritoryManager().getAll().size()))
                    .replaceAll("%markers_count%", String.valueOf(plugin.getMarkerManager().getAll().size()));
            sender.sendMessage(line);
        });
    }
}
