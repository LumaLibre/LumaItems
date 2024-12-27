package dev.jsinco.luma.lumaitems.items.astral

import dev.jsinco.luma.lumaitems.manager.CustomItem
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

interface AstralSet : CustomItem {

    companion object {
        val BLANK_ITEMSTACK: ItemStack = ItemStack(Material.AIR)
    }

    /**
     * This method shouldn't be used for astral sets.
     * @see setItems
     */
    override fun createItem(): Pair<String, ItemStack> {
        return Pair(identifier(), BLANK_ITEMSTACK)
    }

    /**
     * Set the items for the set
     * @return A list of itemstacks
     */
    fun setItems(): List<ItemStack>

    /**
     * Set the identifier for the set
     * @return A string identifier
     */
    fun identifier(): String

}