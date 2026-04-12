package dev.lumas.lumaitems.items.weapons.cutlass

import dev.lumas.lumaitems.model.item.AttributeContainer
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

class DarkMoonClaymoreItem : MidnightClaymoreItem() {

    private companion object {
        private const val KEY = "dark-moon-claymore"
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#342f6c:#7b75aa:#b58fc9>Dark Moon Claymore</gradient></b>")
            .customEnchants("<#7b75aa>Heavyweight")
            .material(Material.NETHERITE_SWORD)
            .persistentData(KEY)
            .tier(Tier.WONDERLAND_2026.alt())
            .attributeModifiers(
                AttributeContainer.of(KEY, Attribute.ATTACK_SPEED, AttributeModifier.Operation.ADD_NUMBER, -3.5, EquipmentSlotGroup.ANY),
                AttributeContainer.of(KEY, Attribute.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_NUMBER, -0.010, EquipmentSlotGroup.MAINHAND)
            )
            .vanillaEnchants(
                Enchantment.SHARPNESS to 10,
                Enchantment.SMITE to 8,
                Enchantment.BANE_OF_ARTHROPODS to 8,
                Enchantment.UNBREAKING to 10,
                Enchantment.LOOTING to 6,
                Enchantment.MENDING to 1
            )
            .lore(
                "A massive claymore that",
                "takes two hands to wield.",
                "",
                "Its heavy blade deals",
                "exponentially more damage",
                "the more enemies it hits",
                "at once."
            )
            .buildPair()
    }
}