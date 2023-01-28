package ru.nightmirror.atlas.api;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.nightmirror.atlas.database.tables.Territory;

@Getter
@Setter
public class TerritoryDeletedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Territory territory;
    private final boolean creatorIsPlayer;
    private boolean cancelled;

    public TerritoryDeletedEvent(Territory territory, boolean creatorIsPlayer) {
        this.territory = territory;
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
