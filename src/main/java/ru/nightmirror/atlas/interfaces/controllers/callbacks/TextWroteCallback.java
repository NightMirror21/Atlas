package ru.nightmirror.atlas.interfaces.controllers.callbacks;

import org.bukkit.entity.Player;

public interface TextWroteCallback {
    boolean execute(final Player player, final String text);
}
