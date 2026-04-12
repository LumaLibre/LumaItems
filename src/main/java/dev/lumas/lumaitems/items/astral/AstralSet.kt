package dev.lumas.lumaitems.items.astral

import dev.lumas.lumaitems.model.item.CustomItem
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
        return Pair(setIdentifier(), BLANK_ITEMSTACK)
    }

    /**
     * Set of items
     * @return A list of itemstacks
     */
    fun setItems(): List<ItemStack>

    /**
     * Set the identifier for the set
     * @return A string identifier
     */
    fun setIdentifier(): String

}