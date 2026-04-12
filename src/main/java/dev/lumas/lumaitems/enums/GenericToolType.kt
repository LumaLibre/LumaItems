package dev.lumas.lumaitems.enums

import org.bukkit.Material
import org.bukkit.inventory.EquipmentSlot

enum class GenericToolType(vararg val toolTypes: ToolType) {
    ARMOR(ToolType.HELMET, ToolType.CHESTPLATE, ToolType.LEGGINGS, ToolType.BOOTS),
    WEAPON(ToolType.SWORD, ToolType.SPEAR, ToolType.AXE, ToolType.BOW, ToolType.CROSSBOW, ToolType.TRIDENT, ToolType.SHIELD, ToolType.MACE),
    TOOL(ToolType.PICKAXE, ToolType.AXE, ToolType.SHOVEL, ToolType.HOE, ToolType.FISHING_ROD);


    val equipmentSlot: EquipmentSlot
            get() = when (this) {
                ARMOR -> EquipmentSlot.CHEST
                WEAPON, TOOL -> EquipmentSlot.HAND
            }


    companion object {
        fun getGenericToolType(material: Material): GenericToolType? {
            for (genericToolType in entries) {
                for (toolType in genericToolType.toolTypes) {
                    if (toolType.matches(material)) {
                        return genericToolType
                    }
                }
            }
            return null
        }
    }
}
