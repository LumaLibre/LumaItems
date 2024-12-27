package dev.jsinco.luma.items.astral.sets

import dev.jsinco.luma.items.astral.AstralSet
import dev.jsinco.luma.items.astral.AstralSetFactory
import dev.jsinco.luma.enums.Action
import dev.jsinco.luma.enums.DefaultAttributes
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.util.UUID

class ReforgedSet : AstralSet {
    override fun setItems(): List<ItemStack> {
        val astralSetFactory = AstralSetFactory("Reforged", mutableListOf("&#AC87FBUnwavering"))
        astralSetFactory.commonEnchants = mutableMapOf(
            Enchantment.MENDING to 1,
            Enchantment.UNBREAKING to 6,
            Enchantment.PROTECTION to 7
        )


        astralSetFactory.astralSetItem(
            Material.NETHERITE_HELMET,
            mutableMapOf(Enchantment.RESPIRATION to 3),
            mutableListOf("Increases max amount", "of health while worn"),
            true,
            DefaultAttributes.NETHERITE_HELMET.appendThenGetAttributes(Attribute.MAX_HEALTH,
                AttributeModifier(UUID.randomUUID(),"genericMaxHealth", 2.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD))
        )

        astralSetFactory.astralSetItem(
            Material.NETHERITE_CHESTPLATE,
            mutableMapOf(),
            mutableListOf("Increases max amount", "of health while worn"),
            true,
            DefaultAttributes.NETHERITE_CHESTPLATE.appendThenGetAttributes(Attribute.MAX_HEALTH,
                AttributeModifier(UUID.randomUUID(),"genericMaxHealth", 2.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.CHEST))
        )

        astralSetFactory.astralSetItem(
            Material.NETHERITE_LEGGINGS,
            mutableMapOf(),
            mutableListOf("Increases max amount", "of health while worn"),
            true,
            DefaultAttributes.NETHERITE_LEGGINGS.appendThenGetAttributes(Attribute.MAX_HEALTH,
                AttributeModifier(UUID.randomUUID(),"genericMaxHealth", 2.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.LEGS))
        )

        astralSetFactory.astralSetItem(
            Material.NETHERITE_BOOTS,
            mutableMapOf(Enchantment.FEATHER_FALLING to 4, Enchantment.DEPTH_STRIDER to 3),
            mutableListOf("Increases max amount", "of health while worn"),
            true,
            DefaultAttributes.NETHERITE_BOOTS.appendThenGetAttributes(Attribute.MAX_HEALTH,
                AttributeModifier(UUID.randomUUID(),"genericMaxHealth", 2.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.FEET))
        )

        return astralSetFactory.createdAstralItems
    }

    override fun identifier(): String {
        return "reforged-set"
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        return false
    }
}