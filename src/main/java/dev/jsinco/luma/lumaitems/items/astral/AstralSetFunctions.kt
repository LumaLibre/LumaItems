package dev.jsinco.luma.lumaitems.items.astral

import dev.jsinco.luma.lumaitems.enums.GenericToolType
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.inventory.ItemStack

abstract class AstralSetFunctions(val identifier: String) : CustomItemFunctions(), AstralSet {

    val key = Util.namespacedKey(identifier)

    /**
     * Verifies that the given [ItemStack] is of the tool type represented by this [genericToolType]
     * and that the item is part of this astral set (as defined by [identifier]).
     *
     * @param genericToolType The [GenericToolType] to check against.
     * @return `true` if the [ItemStack] is of the specified tool type and belongs to this astral set, `false` otherwise.
     */
    fun ItemStack.isToolType(genericToolType: GenericToolType): Boolean {
        if (!Util.hasPersistentKey(this, key)) {
            return false
        }
        val genericToolTypeValue = GenericToolType.getGenericToolType(this.type) ?: return false
        return genericToolTypeValue == genericToolType
    }

    fun String.astralColor(): String {
        return "<#AC87FB>$this</#AC87FB>"
    }

    override fun identifier(): String {
        return identifier
    }


    /**
     * This method shouldn't be used for astral sets.
     * @see setItems
     */
    override fun createItem(): Pair<String, ItemStack> {
        return Pair(identifier(), AstralSet.BLANK_ITEMSTACK)
    }
}
