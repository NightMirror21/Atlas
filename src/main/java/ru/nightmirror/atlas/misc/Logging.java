package ru.nightmirror.atlas.misc;

import org.bukkit.Bukkit;

public abstract class Logging {

    private static boolean isDebugEnabled = false;

    public static void info(String message) {
        Bukkit.getLogger().info(String.format("[Atlas] %s", message));
    }

    public static void warn(String message) {
        Bukkit.getLogger().warning(String.format("[Atlas] %s", message));
    }

    public static void error(String message) {
        Bukkit.getLogger().severe(String.format("[Atlas] %s", message));
    }


    public static void debug(Object object, String message) {
        if (isDebugEnabled) {
            Bukkit.getLogger().info(String.format("[Atlas-%s] %s", object.getClass().getSimpleName(), message));
        }
    }

    public static boolean isDebugEnabled() {
        return isDebugEnabled;
    }

    public static void changeDebugEnabled() {
        isDebugEnabled = !isDebugEnabled;
    }
}
