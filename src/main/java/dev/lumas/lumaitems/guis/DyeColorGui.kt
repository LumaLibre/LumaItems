package dev.lumas.lumaitems.guis

import dev.lumas.core.model.gui.AbstractGui
import dev.lumas.core.model.gui.items.IndexedGuiItem
import dev.lumas.core.util.Text
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.ColorHolder
import dev.lumas.lumaitems.util.DyeColorUtil
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class DyeColorGui : AbstractGui() {

    companion object {
        private const val PREVIEW_ITEM_INDEX = 22
    }

    private val inventory: Inventory = Bukkit.createInventory(this, 36, Text.mm("Color Selection"))
    val colorHolder = ColorHolder()

    val preview: IndexedGuiItem = IndexedGuiItem.of(
        PREVIEW_ITEM_INDEX,
        previewItem(colorHolder)
    ) { _, _ -> }

    val red: IndexedGuiItem = IndexedGuiItem.of(
        11,
        ItemFactory.builder()
            .name("<red><b>Red Hue")
            .material(Material.RED_DYE)
            .lore(
                "- Left-click to increase",
                "- Right-click to decrease"
            )
            .buildItem()
    ) { event, self ->
        when (event.click) {
            ClickType.LEFT -> colorHolder.adjustRed(10)
            ClickType.RIGHT -> colorHolder.adjustRed(-10)
            else -> {}
        }
        updatePreview()
    }

    val blue: IndexedGuiItem = IndexedGuiItem.of(
        13,
        ItemFactory.builder()
            .name("<blue><b>Blue Hue")
            .material(Material.BLUE_DYE)
            .lore(
                "- Left-click to increase",
                "- Right-click to decrease"
            )
            .buildItem()
    ) { event, self ->
        when (event.click) {
            ClickType.LEFT -> colorHolder.adjustBlue(10)
            ClickType.RIGHT -> colorHolder.adjustBlue(-10)
            else -> {}
        }
        updatePreview()
    }

    val green: IndexedGuiItem = IndexedGuiItem.of(
        15,
        ItemFactory.builder()
            .name("<green><b>Green Hue")
            .material(Material.GREEN_DYE)
            .lore(
                "- Left-click to increase",
                "- Right-click to decrease"
            )
            .buildItem()
    ) { event, self ->
        when (event.click) {
            ClickType.LEFT -> colorHolder.adjustGreen(10)
            ClickType.RIGHT -> colorHolder.adjustGreen(-10)
            else -> {}
        }
        updatePreview()
    }

    fun previewItem(colorHolder: ColorHolder): ItemStack {
        return ItemFactory.builder()
            .name("<${colorHolder.asHex()}><b>Preview")
            .material(DyeColorUtil.dyeMaterial(colorHolder))
            .lore("<white>Your color will appear as <b><${colorHolder.asHex()}>this</${colorHolder.asHex()}></b>.")
            .buildItem()
    }

    fun updatePreview() {
        val item = previewItem(colorHolder)
        inventory.setItem(PREVIEW_ITEM_INDEX, item)
    }

    init {
        autoRegister()
    }

    override fun onInventoryClick(event: InventoryClickEvent) {
        event.isCancelled = true
    }

    override fun onInventoryClose(event: InventoryCloseEvent) {
        TODO("Not yet implemented")
    }

    override fun getInventory(): Inventory {
        return inventory
    }
}