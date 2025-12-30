package dev.lumas.lumaitems.util.extensions

import dev.lumas.lumaitems.LumaItems
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object ItemUtil {

    fun Player.isWearing(identifier: String): Boolean {
        return isWearing(NamespacedKey(LumaItems.getInstance(), identifier))
    }

    fun Player.isWearing(identifier: NamespacedKey): Boolean {
        val armorDatas: List<PersistentDataContainer?> = this.inventory.armorContents
            .map { it?.itemMeta?.persistentDataContainer }

        for (data in armorDatas) {
            if (data != null && data.has(identifier, PersistentDataType.SHORT)) {
                return true
            }
        }

        return false
    }

    fun ItemStack.isMatchingItem(key: String): Boolean {
        return isMatchingItem(NamespacedKey(LumaItems.getInstance(), key))
    }

    fun ItemStack.isMatchingItem(key: NamespacedKey): Boolean {
        val meta = this.itemMeta ?: return false
        return meta.persistentDataContainer.has(key)
    }
}