package dev.lumas.lumaitems.model.item

import dev.lumas.lumaitems.util.extensions.getHealth
import io.papermc.paper.persistence.PersistentDataContainerView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

/**
 * Pairs a PDC view with the [ItemStack] it was read from, when applicable.
 * The item is null when the PDC came from a non-item source (entity, etc.).
 *
 * For item-backed sources, [data] is Paper's live PDC view of the [ItemStack], which avoids cloning
 * the whole [ItemMeta] on hot paths. [meta] is only snapshotted on first access and then cached,
 * so callers can read or mutate it without incurring another itemMeta allocation.
 */
class PdcSource private constructor(
    val data: PersistentDataContainerView,
    val item: ItemStack?
) {

    val meta: ItemMeta? by lazy(LazyThreadSafetyMode.NONE) { item?.itemMeta }

    /**
     * True if the damageable item's health is 1.
     */
    fun isHealthTooLow(): Boolean {
        val health = item?.getHealth() ?: return false
        return health == 1
    }

    companion object {
        fun of(item: ItemStack): PdcSource? {
            if (item.type.isAir) return null // Air has no item meta
            return PdcSource(item.persistentDataContainer, item)
        }

        fun of(data: PersistentDataContainerView): PdcSource = PdcSource(data, null)
    }
}
