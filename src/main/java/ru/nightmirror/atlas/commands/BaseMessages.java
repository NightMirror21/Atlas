package ru.nightmirror.atlas.commands;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.nightmirror.atlas.config.Config;

@RequiredArgsConstructor
public abstract class BaseMessages {

    private final Config baseConfig;

    public boolean hasPermission(CommandSender sender, String... permissions) {
        for (String permission : permissions) {
            if (sender.hasPermission(permission)) {
                return true;
            }
        }
        sender.sendMessage(baseConfig.getString("messages.only-for-players"));
        return false;
    }

    public boolean hasPermissionWithoutMessage(CommandSender sender, String... permissions) {
        for (String permission : permissions) {
            if (sender.hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(baseConfig.getString("messages.not-permission"));
        }
        return true;
    }

    public TextComponent createButton(String text, String hover, ClickEvent.Action action, String value) {
        TextComponent button = new TextComponent(text);
        button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
        button.setClickEvent(new ClickEvent(action, value));
        return button;
    }

    public boolean isNumberWithoutMessage(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public boolean isNumber(CommandSender sender, String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (Exception exception) {
            sender.sendMessage(baseConfig.getString("messages.not-a-number").replaceAll("%value%", text));
            return false;
        }
    }

    public TextComponent join(String text, TextComponent component, String separator) {
        TextComponent textComponent = new TextComponent(text + separator);
        textComponent.addExtra(component);
        return textComponent;
    }

    public TextComponent join(String text, TextComponent component) {
        return join(text, component, "");
    }
}
