package dev.lumas.lumaitems.enums

import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack

enum class EntityArmor {
    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS;


    fun setEntityArmorSlot(entity: LivingEntity, itemStack: ItemStack) {
        val equipment = entity.equipment ?: return
        when (this) {
            HELMET -> equipment.setHelmet(itemStack)
            CHESTPLATE -> equipment.setChestplate(itemStack)
            LEGGINGS -> equipment.setLeggings(itemStack)
            BOOTS -> equipment.setBoots(itemStack)
        }
    }

    companion object {
        fun getEquipmentSlotFromType(material: Material): EntityArmor? {
            val name = material.toString()

            for (equipment in entries) {
                if (name.contains(equipment.name)) {
                    return equipment
                }
            }
            return null
        }
    }
}
