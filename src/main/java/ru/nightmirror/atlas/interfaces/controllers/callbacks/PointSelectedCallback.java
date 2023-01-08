package ru.nightmirror.atlas.interfaces.controllers.callbacks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface PointSelectedCallback {
    boolean execute(final Player player, final Block block);
}
