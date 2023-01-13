package ru.nightmirror.atlas.misc.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerUtils {

    public static String nicknameFromUUID(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player != null) {
            return player.getName();
        }

        Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null) {
            return onlinePlayer.getName();
        }

        return "null";
    }

    @Nullable
    public static UUID uuidFromNickname(String nickname) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(nickname);
        if (player != null) {
            return player.getUniqueId();
        }

        Player onlinePlayer = Bukkit.getPlayer(nickname);
        if (onlinePlayer != null) {
            return onlinePlayer.getUniqueId();
        }

        return null;
    }

}
