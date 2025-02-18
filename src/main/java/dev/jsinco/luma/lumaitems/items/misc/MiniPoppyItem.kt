package dev.jsinco.luma.lumaitems.items.misc

import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.obj.AttributeContainer
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

class MiniPoppyItem : CustomItem {

    override fun createItem(): Pair<String, ItemStack> {
        val key = "mini-poppy"
        return ItemFactory.builder()
            .name("<b><#FF5959>M<#FF6259>i<#FF6B58>n<#FF765F>i <#FE8D6C>P<#FD8A84>o<#FB869C>p<#FC7890>p<#FC6984>y")
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .customEnchants("<#ff5959>Flower Extract")
            .lore(
                "A wonderful smelling poppy,",
                "It's so small and cute!",
                "",
                "I wonder if holding it",
                "does anything special?"
            )
            .tier(Tier.VALENTIDE_2025)
            .persistentData(key)
            .material(Material.POPPY)
            .attributeModifiers(
                AttributeContainer.of(key, Attribute.SCALE, AttributeModifier.Operation.ADD_NUMBER, -0.5, EquipmentSlotGroup.ANY),
                AttributeContainer.of(key, Attribute.GRAVITY , AttributeModifier.Operation.ADD_NUMBER, -0.05, EquipmentSlotGroup.ANY),
                AttributeContainer.of(key, Attribute.SAFE_FALL_DISTANCE, AttributeModifier.Operation.ADD_NUMBER, 6.0, EquipmentSlotGroup.ANY)
            )
            .buildPair()
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        return false
    }
}