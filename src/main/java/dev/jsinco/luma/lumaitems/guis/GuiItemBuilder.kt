package dev.jsinco.luma.lumaitems.guis

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class GuiItemBuilder(material: Material) {

    constructor(material: Material, amt: Int) : this(material) {
        itemStack.amount = amt
    }

    val itemStack = ItemStack(material)
    val meta = itemStack.itemMeta ?: throw NullPointerException("ItemMeta is null")

    fun setDisplayName(name: String): GuiItemBuilder {
        meta.setDisplayName(name)
        return this
    }

    fun setLore(lore: List<String>): GuiItemBuilder {
        meta.lore = lore
        return this
    }

    fun displayName(name: String): GuiItemBuilder {
        meta.displayName(MiniMessageUtil.mm(name))
        return this
    }

    fun lore(lore: List<String>): GuiItemBuilder {
        meta.lore(MiniMessageUtil.mml(lore))
        return this
    }

    fun data(datas: List<String>): GuiItemBuilder {
        for (data in datas) {
            meta.persistentDataContainer.set(NamespacedKey(LumaItems.getInstance(), data), PersistentDataType.SHORT, 1)
        }
        return this
    }

    fun glint(glint: Boolean): GuiItemBuilder {
        if (glint) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true)
        }
        return this
    }

    fun build(): ItemStack {
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        itemStack.itemMeta = meta
        return itemStack
    }

    companion object {
        fun create(material: Material): GuiItemBuilder {
            return GuiItemBuilder(material)
        }

        fun create(material: Material, amt: Int): GuiItemBuilder {
            return GuiItemBuilder(material, amt)
        }

        fun toggleGint(item: ItemStack) {
            val meta = item.itemMeta ?: return
            if (meta.hasEnchant(Enchantment.UNBREAKING)) {
                meta.removeEnchant(Enchantment.UNBREAKING)
            } else {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true)
            }
            item.itemMeta = meta
        }
    }
}