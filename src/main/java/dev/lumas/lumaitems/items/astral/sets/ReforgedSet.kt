package dev.lumas.lumaitems.items.astral.sets

import dev.lumas.lumaitems.enums.DefaultAttributes
import dev.lumas.lumaitems.items.astral.AstralSetFactory
import dev.lumas.lumaitems.items.astral.AstralSetFunctions
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

class ReforgedSet : AstralSetFunctions("reforged-set") {
    override fun setItems(): List<ItemStack> {
        val astralSetFactory = AstralSetFactory("reforged-set","Reforged", mutableListOf("&#AC87FBUnwavering"))
        astralSetFactory.commonEnchants = mutableMapOf(
            Enchantment.MENDING to 1,
            Enchantment.UNBREAKING to 6,
            Enchantment.PROTECTION to 7
        )

        astralSetFactory.astralSetItem(
            Material.NETHERITE_HELMET,
            mutableMapOf(Enchantment.RESPIRATION to 3),
            mutableListOf("Increases max amount", "of health while worn."),
            true,
            DefaultAttributes.NETHERITE_HELMET.appendThenGetAttributes(Attribute.MAX_HEALTH,
                "${identifier()}-helm", 2.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD)
        )

        astralSetFactory.astralSetItem(
            Material.NETHERITE_CHESTPLATE,
            mutableMapOf(),
            mutableListOf("Increases max amount", "of health while worn."),
            true,
            DefaultAttributes.NETHERITE_CHESTPLATE.appendThenGetAttributes(Attribute.MAX_HEALTH,
                "${identifier()}-chest", 2.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST)
        )

        astralSetFactory.astralSetItem(
            Material.NETHERITE_LEGGINGS,
            mutableMapOf(),
            mutableListOf("Increases max amount", "of health while worn."),
            true,
            DefaultAttributes.NETHERITE_LEGGINGS.appendThenGetAttributes(Attribute.MAX_HEALTH,
                "${identifier()}-legs", 2.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.LEGS)
        )

        astralSetFactory.astralSetItem(
            Material.NETHERITE_BOOTS,
            mutableMapOf(Enchantment.FEATHER_FALLING to 4, Enchantment.DEPTH_STRIDER to 3),
            mutableListOf("Increases max amount", "of health while worn."),
            true,
            DefaultAttributes.NETHERITE_BOOTS.appendThenGetAttributes(Attribute.MAX_HEALTH,
                "${identifier()}-boots", 2.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.FEET)
        )

        return astralSetFactory.createdAstralItems
    }

}