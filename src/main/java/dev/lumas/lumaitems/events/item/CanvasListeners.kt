package dev.lumas.lumaitems.events.item

import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.Register
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.util.extensions.equipmentContainers
import io.canvasmc.canvas.event.EntityTeleportAsyncEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler

@Register(Autowire.LISTENER, requires = "io.canvasmc.canvas.event.EntityTeleportAsyncEvent")
class CanvasListeners : ItemListener() {

    @EventHandler(ignoreCancelled = true)
    fun onCanvasAsyncTeleport(event: EntityTeleportAsyncEvent) {
        val player = event.entity as? Player ?: return
        fire(player.equipmentContainers(), Action.CANVAS_ASYNC_PLAYER_TELEPORT, player, event)
    }
}