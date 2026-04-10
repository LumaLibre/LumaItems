package dev.lumas.lumaitems.guis

import dev.lumas.core.model.gui.AbstractGui
import dev.lumas.core.model.gui.items.IndexedGuiItem
import dev.lumas.core.util.Text
import dev.lumas.lumaitems.items.astral.AstralSet
import dev.lumas.lumaitems.manager.ItemManager
import dev.lumas.lumaitems.model.Mixable
import dev.lumas.lumaitems.relics.RelicDisassembler
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.hasPersistentKey
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class MixerUpgradeGui : AbstractGui() {

    companion object {
        private const val INPUT_SLOT_1 = 11
        private const val INPUT_SLOT_2 = 13
        private const val OUTPUT_SLOT = 15

        private val EMPTY_SLOTS: List<Int> = listOf(INPUT_SLOT_1, INPUT_SLOT_2, OUTPUT_SLOT)

        private val BORDER: ItemStack = Util.createBasicItem("&0", listOf(), Material.YELLOW_STAINED_GLASS_PANE, listOf("gui-item"), false)
        private val CONFIRM_BUTTON: ItemStack = Util.createBasicItem("&a&lConfirm", listOf(), Material.LIME_STAINED_GLASS_PANE, listOf("gui-item"), true)
        private val GUI_ITEM_KEY = Util.namespacedKey("gui-item")
    }


    val confirmButton = IndexedGuiItem.of(
        16,
        CONFIRM_BUTTON,
    ) { event, self ->

        val inv = event.inventory
        val player = event.whoClicked as Player

        val mixableTool = inv.getItem(INPUT_SLOT_1) ?: return@of

        val otherTool = inv.getItem(INPUT_SLOT_2) ?: return@of

        val mixableToolHandle = ItemManager.getCustomItem(mixableTool) ?: run {
            Text.msg(player, "Invalid mixable tool")
            return@of
        }

        if (mixableToolHandle is AstralSet || otherTool.hasPersistentKey(RelicDisassembler.RELIC_RARITY_KEY)) {
            Text.msg(player, "Relics cannot be mixed. (Class assignable from AstralSet or RelicDisassembler rarity key)")
            return@of
        }

        if (mixableToolHandle is Mixable) {
            val output = mixableToolHandle.mix(player, mixableTool, otherTool)
            if (output == null) {
                Text.msg(player, "Mixing failed.")
                return@of
            }
            inv.setItem(INPUT_SLOT_1, null)
            inv.setItem(INPUT_SLOT_2, null)
            inv.setItem(OUTPUT_SLOT, output)
            Text.msg(player, "Mixing complete!")
        } else {
            Text.msg(player, "This item cannot be mixed.")
        }
    }

    val internalInv = Bukkit.createInventory(this, 27, Text.mm("<#b986f9><b>Mixer"))

    init {
        this.autoRegister()
        for (slot in internalInv.contents.indices) {
            if (!EMPTY_SLOTS.contains(slot) && internalInv.getItem(slot) == null) {
                internalInv.setItem(slot, BORDER)
            }
        }
    }

    override fun onInventoryClick(event: InventoryClickEvent) {
        val clickedItem = event.currentItem ?: return
        if (clickedItem.hasPersistentKey(GUI_ITEM_KEY)) {
            event.isCancelled = true
        }
    }

    override fun onInventoryClose(event: InventoryCloseEvent) {
        val inv = event.inventory
        val player = event.player as Player
        for (emptySlot in EMPTY_SLOTS) {
            val item = inv.getItem(emptySlot) ?: continue
            Util.giveItem(player, item)
        }
    }

    override fun getInventory(): Inventory {
        return internalInv
    }
}