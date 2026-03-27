package dev.lumas.lumaitems.guis

import dev.lumas.core.model.gui.AbstractGui
import dev.lumas.core.model.gui.GuiItemAction
import dev.lumas.core.model.gui.items.IndexedGuiItem
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.ColorHolder
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

class ColorableItemGui(
    val color1: ColorHolder = ColorHolder(),
    val color2: ColorHolder = ColorHolder(),
    val color3: ColorHolder = ColorHolder(),
    val color4: ColorHolder = ColorHolder(),
    val color5: ColorHolder = ColorHolder(),
) : AbstractGui() {



    override fun onInventoryClick(event: InventoryClickEvent) {
        TODO("Not yet implemented")
    }

    override fun onInventoryClose(event: InventoryCloseEvent) {
        TODO("Not yet implemented")
    }

    override fun getInventory(): Inventory {
        TODO("Not yet implemented")
    }
}