package io.canvasmc.canvas.event;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class EntityTeleportAsyncEvent extends Event {

    // Placeholder until canvas dev-bundles

    private static final HandlerList HANDLERS = new HandlerList();

    public Entity getEntity() {
        return null;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
