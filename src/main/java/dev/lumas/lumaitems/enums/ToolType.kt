package dev.lumas.lumaitems.enums

import java.util.Locale
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * Enum for generic Minecraft tool types.
 * Determine their type without regard for the type of material.
 */
enum class ToolType {
    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS,
    SWORD,
    SPEAR,
    PICKAXE,
    AXE,
    SHOVEL,
    HOE,
    CROSSBOW,
    BOW,
    TRIDENT,
    SHIELD,
    ELYTRA,
    FISHING_ROD,
    MAGICAL,
    MACE,
    SHEARS;

    fun matches(material: Material): Boolean {
        return getToolType(material) == this
    }

    companion object {
        val magicMaterials = listOf("BLAZE_ROD", "BREEZE_ROD")


        fun getToolType(item: ItemStack): ToolType? {
            return getToolType(item.type.toString())
        }

        fun getToolType(material: Material): ToolType? {
            return getToolType(material.toString())
        }

        fun getToolType(string: String): ToolType? {
            var string = string
            string = string.uppercase(Locale.getDefault())
            if (magicMaterials.contains(string)) {
                return ToolType.MAGICAL
            }

            for (toolType in ToolType.entries) {
                if (string.contains(toolType.toString())) {
                    return toolType
                }
            }
            return null
        }
    }
}
