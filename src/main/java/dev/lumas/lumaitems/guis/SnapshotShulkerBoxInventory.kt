package dev.lumas.lumaitems.guis

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.block.ShulkerBox
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

class SnapshotShulkerBoxInventory(
    private val shulkerBox: ShulkerBox
) : LumaItemsAbstractGui {

    companion object {
        private const val SHULKER_BOX_SIZE = 27
    }

    private val inventory: Inventory = fun (): Inventory {
        val name = shulkerBox.customName() ?: Component.text("Shulker Box")
        val inv = Bukkit.createInventory(this, SHULKER_BOX_SIZE, name)
        inv.contents = shulkerBox.snapshotInventory.contents
        return inv
    }()

    override fun onInventoryClick(event: InventoryClickEvent) {
        event.isCancelled = true
    }

    override fun onInventoryClose(event: InventoryCloseEvent) {
    }

    override fun getInventory(): Inventory {
        return inventory
    }


}