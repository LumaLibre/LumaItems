package dev.jsinco.luma.lumaitems.util.extensions

import dev.jsinco.luma.lumaitems.LumaItems
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
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
}