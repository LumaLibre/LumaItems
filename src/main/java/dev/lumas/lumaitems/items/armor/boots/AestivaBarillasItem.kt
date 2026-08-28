package dev.lumas.lumaitems.items.armor.boots

import dev.lumas.lumaitems.model.item.AttributeContainer
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.namespacedKey
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

class AestivaBarillasItem : CustomItemFunctions() {

    private companion object {
        private val KEY = "aestiva-barillas".namespacedKey()
    }

    override fun createItem() = ItemFactory.builder()
        .name("<b><gradient:#BC66FF:#FF6B6B:#FFA94D:#FFE066>Aestiva Barillas</gradient></b>")
        .customEnchants("<#FF6B6B>Bouncy")
        .material(Material.NETHERITE_BOOTS)
        .persistentData(KEY)
        .tier(Tier.LUMARINE_2026)
        .lore(
            "A pair of festive shoes",
            "that are perfect for",
            "summer.",
            "",
            "<#FF6B6B>While worn</#FF6B6B>, these shoes",
            "allow you to bounce off",
            "any surface."
        )
        .vanillaEnchants(
            Enchantment.PROTECTION to 7,
            Enchantment.FIRE_PROTECTION to 6,
            Enchantment.FEATHER_FALLING to 7,
            Enchantment.UNBREAKING to 10,
            Enchantment.MENDING to 1
        )
        .attributeModifiers(
            AttributeContainer.builder()
                .setKey(KEY)
                .setAttribute(Attribute.BOUNCINESS)
                .setOperation(AttributeModifier.Operation.ADD_NUMBER)
                .setSlot(EquipmentSlotGroup.FEET)
                .setAmount(1.0)
                .build(),
            AttributeContainer.builder()
                .setKey(KEY)
                .setAttribute(Attribute.SAFE_FALL_DISTANCE)
                .setOperation(AttributeModifier.Operation.ADD_NUMBER)
                .setSlot(EquipmentSlotGroup.FEET)
                .setAmount(10.0)
                .build()
        )
        .buildPair()
}