package dev.lumas.lumaitems.model.item

import dev.lumas.lumaitems.api.ItemManager
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface Mixable {

    /**
     * @param player The player doing the mixing
     * @param item The item being mixed
     * @param other The item being mixed with
     * @return The resulting item, or null if the mix failed
     */
    fun mix(player: Player, item: ItemStack, other: ItemStack): ItemStack?

    companion object {
        fun isMixable(item: ItemStack, fromHandle: Boolean = false): Boolean {
            if (!fromHandle) {
                return item.persistentDataContainer.has(PersistentDataRecord.MIXABLE_KEY)
            }

            val handle = ItemManager.getCustomItem(item) ?: return false
            return handle is Mixable
        }
    }
}