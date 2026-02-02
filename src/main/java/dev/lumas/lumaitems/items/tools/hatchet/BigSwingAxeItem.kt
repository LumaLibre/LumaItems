package dev.lumas.lumaitems.items.tools.hatchet

import dev.lumas.lumaitems.enums.DefaultAttributes
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.model.AttributeContainer
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

class BigSwingAxeItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        val key = "big-swing-axe"
        return ItemFactory.builder()
            .name("<b><gradient:#fc8585:#ffc86d:#fbdba2:#86cbd9:#68C3BB>Big Swing Axe</gradient></b>")
            .customEnchants("<#86cbd9>Vertical Swing", "<#68C3BB>Reach")
            .material(Material.NETHERITE_AXE)
            .tier(Tier.CHRISTMAS_2025)
            .persistentData(key)
            .lore(
                "With the large motor fitted",
                "on this axe, it should be able to cut",
                "through any type of log with ease.",
                "",
                "This tool breaks blocks in a",
                "large vertical area, at the cost",
                "of break speed.",
                "",
                "While holding this hatchet, you",
                "may reach up to <#86cbd9>two</#86cbd9> blocks",
                "further than normal."
            )
            .vanillaEnchants(
                Enchantment.UNBREAKING to 4,
                Enchantment.SILK_TOUCH to 1,
                Enchantment.MENDING to 1
            )
            .attributeModifiers(
                DefaultAttributes.NETHERITE_AXE.appendThenGetAttributes(
                    AttributeContainer.of(key, Attribute.BLOCK_BREAK_SPEED, AttributeModifier.Operation.ADD_NUMBER,-0.75, EquipmentSlotGroup.ANY),
                    AttributeContainer.of(key, Attribute.ATTACK_SPEED, AttributeModifier.Operation.ADD_NUMBER, -3.4, EquipmentSlotGroup.ANY),
                    AttributeContainer.of(key, Attribute.BLOCK_INTERACTION_RANGE, AttributeModifier.Operation.ADD_NUMBER, 2.0, EquipmentSlotGroup.MAINHAND),
                    AttributeContainer.of(key, Attribute.ENTITY_INTERACTION_RANGE, AttributeModifier.Operation.ADD_NUMBER, 2.0, EquipmentSlotGroup.MAINHAND)
                )
            )
            .buildPair()
    }


}