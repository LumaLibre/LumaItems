package dev.lumas.lumaitems.guis

import dev.lumas.lumacore.utility.Text
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.items.astral.upgrades.AstralSetUpgradeFactory
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.asComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class AstralUpgradeGui : LumaItemsAbstractGui {

    companion object {
        private val plugin: LumaItems = LumaItems.getInstance()
        private val EMPTY_SLOTS: List<Int> = listOf(11, 13, 15)
        private val BORDER: ItemStack = Util.createBasicItem("&0", listOf(), Material.PURPLE_STAINED_GLASS_PANE, listOf("gui-item"), false)
        private val CONFIRM_BUTTON: ItemStack = Util.createBasicItem("&a&lConfirm", listOf(), Material.LIME_STAINED_GLASS_PANE, listOf("gui-item", "confirm"), true)
    }

    override fun onInventoryClick(event: InventoryClickEvent) {

        val clickedItem = event.currentItem ?: return
        if (clickedItem.itemMeta?.persistentDataContainer?.has(NamespacedKey(plugin, "gui-item"), PersistentDataType.SHORT) == true) {
            event.isCancelled = true
        }
        val i = event.inventory
        val p = event.whoClicked as Player

        val astralTool = i.getItem(11) ?: return

        val upgradeCore = i.getItem(13) ?: return
        if (!isUpgradeCore(upgradeCore)) return

        val factory = AstralSetUpgradeFactory(astralTool)
        if (factory.upgrade()) {
            upgradeCore.amount -= 1
            i.setItem(15, astralTool)
            i.setItem(11, null)
            Text.msg(p, "Your Astral item has been upgraded.")
        } else {
            Text.msg(p, "This item cannot be upgraded any further.")
        }

    }

    override fun onInventoryClose(event: InventoryCloseEvent) {
        for (emptySlot in EMPTY_SLOTS) {
            if (event.inventory.getItem(emptySlot) != null) {
                Util.giveItem(event.player as Player, event.inventory.getItem(emptySlot)!!)
            }
        }
    }

    override fun getInventory(): Inventory {
        val inv = Bukkit.createInventory(this, 27, "<#b986f9><b>Astral Upgrades".asComponent())
        for (slot in inv.contents.indices) {
            if (!EMPTY_SLOTS.contains(slot)) inv.setItem(slot, BORDER)
        }
        inv.setItem(16, CONFIRM_BUTTON)
        return inv
    }

    private fun isUpgradeCore(item: ItemStack): Boolean {
        return item.itemMeta?.persistentDataContainer?.has(NamespacedKey(plugin, "astralupgradecore"), PersistentDataType.SHORT) ?: false
    }
}