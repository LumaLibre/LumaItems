package dev.lumas.lumaitems.events

import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.Register
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.util.ItemExpiration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.InventoryHolder

@Register(Autowire.LISTENER)
class ExpirationListeners : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        player.scheduler.execute(LumaItems.getInstance(), { ItemExpiration.sweep(player) }, null, 1)
    }

    // Chests, barrels, shulkers, hoppers, ...
    @EventHandler(ignoreCancelled = true)
    fun onInventoryOpen(event: InventoryOpenEvent) {
        val removed = ItemExpiration.sweep(event.inventory)
        ItemExpiration.notifyRemoved(event.player as? Player ?: return, removed)
    }

    // Shelves and so on
    @EventHandler(ignoreCancelled = true)
    fun onInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val block = event.clickedBlock ?: return

        val holder = block.getState(false) as? InventoryHolder ?: return
        val removed = ItemExpiration.sweep(holder.inventory)
        ItemExpiration.notifyRemoved(event.player, removed)
    }

    @EventHandler(ignoreCancelled = true)
    fun onInventoryClick(event: InventoryClickEvent) {
        val now = System.currentTimeMillis()
        var removed = 0

        val clicked = event.currentItem
        if (clicked != null && ItemExpiration.isExpired(clicked, now)) {
            removed += clicked.amount
            event.currentItem = null
        }
        val cursor = event.cursor
        if (ItemExpiration.isExpired(cursor, now)) {
            removed += cursor.amount
            event.whoClicked.setItemOnCursor(null)
        }
        if (removed == 0) return

        event.isCancelled = true
        val player = event.whoClicked as? Player ?: return
        player.updateInventory()
        ItemExpiration.notifyRemoved(player, removed)
    }

    @EventHandler(ignoreCancelled = true)
    fun onPickup(event: EntityPickupItemEvent) {
        if (!ItemExpiration.isExpired(event.item.itemStack)) return
        event.isCancelled = true
        val amount = event.item.itemStack.amount
        event.item.remove()
        (event.entity as? Player)?.let { ItemExpiration.notifyRemoved(it, amount) }
    }

    @EventHandler(ignoreCancelled = true)
    fun onHopperPickup(event: InventoryPickupItemEvent) {
        if (!ItemExpiration.isExpired(event.item.itemStack)) return
        event.isCancelled = true
        event.item.remove()
    }
}
