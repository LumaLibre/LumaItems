package dev.jsinco.luma.lumaitems.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class LumaItemsAPIReadyEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public LumaItemsAPIReadyEvent() {
    }

    public LumaItemsAPI getLumaItemsAPI() {
        return LumaItemsAPI.getInstance();
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
