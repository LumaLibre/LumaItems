package dev.lumas.lumaitems.model.item

import dev.lumas.lumaitems.util.extensions.getHealth
import dev.lumas.lumaitems.util.extensions.willBreak
import io.papermc.paper.persistence.PersistentDataContainerView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

/**
 * Pairs a PDC view with the [ItemStack] it was read from, when applicable.
 * The item is null when the PDC came from a non-item source (entity, etc.).
 *
 * For item-backed sources, [meta] is also cached so callers can read or mutate it without
 * incurring another `itemMeta` snapshot allocation.
 */
class PdcSource private constructor(
    val data: PersistentDataContainerView,
    val item: ItemStack?,
    val meta: ItemMeta?
) {

    /**
     * True if the damageable item's health is 1.
     */
    fun isHealthTooLow(): Boolean {
        val health = item?.getHealth() ?: return false
        return health == 1
    }

    companion object {
        fun of(item: ItemStack): PdcSource? {
            val meta = item.itemMeta ?: return null
            return PdcSource(meta.persistentDataContainer, item, meta)
        }

        fun of(data: PersistentDataContainerView): PdcSource = PdcSource(data, null, null)
    }
}