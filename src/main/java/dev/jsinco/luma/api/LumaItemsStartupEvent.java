package dev.jsinco.luma.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class LumaItemsStartupEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public LumaItemsStartupEvent() {
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    // Required by Bukkit
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
