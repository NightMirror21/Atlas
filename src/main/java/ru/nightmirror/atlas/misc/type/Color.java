package ru.nightmirror.atlas.misc.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

import javax.annotation.Nullable;

@AllArgsConstructor
@Getter
public enum Color {

    BLACK(ChatColor.BLACK, "0x000000"),
    DARK_BLUE(ChatColor.DARK_BLUE, "0x0000AA"),
    DARK_GREEN(ChatColor.DARK_GREEN, "0x00AA00"),
    DARK_AQUA(ChatColor.DARK_AQUA, "0x00AAAA"),
    DARK_RED(ChatColor.DARK_RED, "0xAA0000"),
    DARK_PURPLE(ChatColor.DARK_PURPLE, "0xAA00AA"),
    GOLD(ChatColor.GOLD, "0xFFAA00"),
    GRAY(ChatColor.GRAY, "0xAAAAAA"),
    DARK_GRAY(ChatColor.DARK_GRAY, "0x555555"),
    BLUE(ChatColor.BLUE, "0x5555FF"),
    GREEN(ChatColor.GREEN, "0x55FF55"),
    AQUA(ChatColor.AQUA, "0x55FFFF"),
    RED(ChatColor.RED, "0xFF5555"),
    LIGHT_PURPLE(ChatColor.LIGHT_PURPLE, "0xFF55FF"),
    YELLOW(ChatColor.YELLOW, "0xFFFF55"),
    WHITE(ChatColor.WHITE, "0xFFFFFF");

    private final ChatColor minecraftColor;
    private final String hexRGB; // For DynMap

    @Nullable
    public static Color parse(String name) {
        for (Color color : Color.values()) {
            if (color.name().equalsIgnoreCase(name.trim()))
                return color;
        }
        return null;
    }
}
