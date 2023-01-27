package ru.nightmirror.atlas.controllers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.nightmirror.atlas.interfaces.controllers.IPlayerController;
import ru.nightmirror.atlas.interfaces.controllers.callbacks.PointSelectedCallback;
import ru.nightmirror.atlas.interfaces.controllers.callbacks.TextWroteCallback;
import ru.nightmirror.atlas.misc.Logging;

import java.util.HashMap;
import java.util.UUID;

public class PlayerController implements IPlayerController, Listener {

    private final HashMap<UUID, TextWroteCallback> textsCallback = new HashMap<>();
    private final HashMap<UUID, PointSelectedCallback> pointsCallback = new HashMap<>();

    @EventHandler
    private void onLeftBlockClick(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) && event.getClickedBlock() != null && pointsCallback.containsKey(event.getPlayer().getUniqueId())) {
            if (pointsCallback.get(event.getPlayer().getUniqueId()).execute(event.getPlayer(), event.getClickedBlock())) {
                pointsCallback.remove(event.getPlayer().getUniqueId());
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onText(AsyncPlayerChatEvent event) {
        if (textsCallback.containsKey(event.getPlayer().getUniqueId())) {
            if (textsCallback.get(event.getPlayer().getUniqueId()).execute(event.getPlayer(), event.getMessage())) {
                textsCallback.remove(event.getPlayer().getUniqueId());
            }
            event.setCancelled(true);
        }
    }

    @Override
    public void removeAllCallbacks(UUID uuid) {
        textsCallback.remove(uuid);
        pointsCallback.remove(uuid);
    }

    @Override
    public boolean containsAnyCallback(UUID uuid) {
        return textsCallback.containsKey(uuid) || pointsCallback.containsKey(uuid);
    }

    public void addTextWroteCallback(UUID uuid, TextWroteCallback callback) {
        textsCallback.put(uuid, callback);
    }

    public void addPointSelectedCallback(UUID uuid, PointSelectedCallback callback) {
        pointsCallback.put(uuid, callback);
    }

    public void reload() {
        textsCallback.clear();
        pointsCallback.clear();
        Logging.debug(this, "Callbacks cleared");
    }
}
