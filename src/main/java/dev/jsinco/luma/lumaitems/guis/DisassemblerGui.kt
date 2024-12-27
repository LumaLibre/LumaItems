package dev.jsinco.luma.lumaitems.guis

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.relics.RelicDisassembler
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class DisassemblerGui : AbstractGui {

    companion object {
        private val plugin: LumaItems = LumaItems.getInstance()
        private val BORDER: ItemStack = Util.createBasicItem("&0", listOf(), Material.GRAY_STAINED_GLASS_PANE, listOf("gui-item"), false)
        private val CONFIRM_BUTTON: ItemStack = Util.createBasicItem("&a&lConfirm", listOf(), Material.LIME_STAINED_GLASS_PANE, listOf("gui-item", "confirm"), true)
    }

    override fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        val clickedItem = event.currentItem ?: return

        if (clickedItem.itemMeta?.persistentDataContainer?.has(NamespacedKey(plugin, "gui-item"), PersistentDataType.SHORT) == true) {
            event.isCancelled = true
        }

        if (clickedItem.itemMeta?.persistentDataContainer?.has(NamespacedKey(plugin, "confirm"), PersistentDataType.SHORT) == true) {
            for (i in 0..35) {
                val item = event.inventory.getItem(i) ?: continue
                val command = RelicDisassembler.getCommandToExecute(item, player) ?: continue
                item.amount -= 1
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
            }
            player.closeInventory()
        }
    }

    override fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as Player
        for (i in 0..35) {
            if (event.inventory.getItem(i) != null) {
                Util.giveItem(player, event.inventory.getItem(i)!!)
            }
        }
    }

    override fun getInventory(): Inventory {
        val inv = Bukkit.createInventory(this, 45, Util.colorcode("&#F670F1&lDisassembler"))
        for (i in 36..44) {
            inv.setItem(i, BORDER)
        }
        inv.setItem(40, CONFIRM_BUTTON)
        return inv
    }

}