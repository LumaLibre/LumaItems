package dev.jsinco.luma.lumaitems.items.astral

import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import org.bukkit.inventory.ItemStack

abstract class AstralSetFunctions : CustomItemFunctions(), AstralSet {

    /**
     * This method shouldn't be used for astral sets.
     * @see setItems
     */
    override fun createItem(): Pair<String, ItemStack> {
        return Pair(identifier(), AstralSet.BLANK_ITEMSTACK)
    }
}
