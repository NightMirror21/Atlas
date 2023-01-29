package ru.nightmirror.atlas.api;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.nightmirror.atlas.database.tables.Marker;

@Getter
@Setter
public class MarkerDeletedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Marker marker;
    private final boolean creatorIsPlayer;
    private boolean cancelled;

    public MarkerDeletedEvent(Marker marker, boolean creatorIsPlayer) {
        this.marker = marker;
        this.creatorIsPlayer = creatorIsPlayer;
        cancelled = false;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}