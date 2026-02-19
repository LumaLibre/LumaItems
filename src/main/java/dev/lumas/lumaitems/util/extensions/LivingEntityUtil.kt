@file:JvmName("LivingEntityUtil")
package dev.lumas.lumaitems.util.extensions

import org.bukkit.Material
import org.bukkit.entity.LivingEntity

/**
 * Drops all equipment of the living entity (armor, main hand item, and offhand item) at the entity's eye location.
 * After dropping the items, it clears the entity's equipment.
 */
fun LivingEntity.dropEquipment() {
    val entityEquipment = equipment ?: return
    entityEquipment.let { equip ->
        equip.armorContents.forEach { it?.let { world.dropItemNaturally(eyeLocation, it) } }
        equip.itemInMainHand.let { world.dropItemNaturally(eyeLocation, it) }
        equip.itemInOffHand.let { world.dropItemNaturally(eyeLocation, it) }
    }
    entityEquipment.clear()
}

/**
 * Checks if the entity has any equipment (armor, main hand item, or off hand item).
 */
fun LivingEntity.hasEquipment(): Boolean {
    val entityEquipment = equipment ?: return false
    return entityEquipment.armorContents.any { it != null } ||
            entityEquipment.itemInMainHand.type != Material.AIR ||
            entityEquipment.itemInOffHand.type != Material.AIR
}
