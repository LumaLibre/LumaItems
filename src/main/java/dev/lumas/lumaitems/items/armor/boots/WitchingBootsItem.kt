package dev.lumas.lumaitems.items.armor.boots

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import dev.lumas.lumaitems.enums.DefaultAttributes
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.model.AttributeContainer
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class WitchingBootsItem : CustomItemFunctions() {

    companion object {
        private val KEY = Util.namespacedKey("witching-boots")
        private val JUMP_BOOST = PotionEffect(PotionEffectType.JUMP_BOOST, 340, 1, false, false, false)
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#320343:#5a2d3e:#876541:#8da649:#74eb62>Witching Boots</gradient></b>")
            .customEnchants("<gold>Jump Boost II", "<gold>Swift")
            .material(Material.NETHERITE_BOOTS)
            .persistentData(KEY)
            .tier(Tier.HALLOWEEN_2025)
            .lore(
                "A set of boots befitting",
                "a witch. These boots let",
                "the wearer run and jump",
                "more efficiently than",
                "normal.",
                "",
                "Perfect for things in",
                "in hard-to-reach places."
            )
            .vanillaEnchants(
                Enchantment.PROTECTION to 6,
                Enchantment.FEATHER_FALLING to 7,
                Enchantment.UNBREAKING to 8,
                Enchantment.MENDING to 1,
                Enchantment.SOUL_SPEED to 3,
                Enchantment.THORNS to 3
            )
            .attributeModifiers(
                DefaultAttributes.NETHERITE_BOOTS.appendThenGetAttributes(
                    AttributeContainer.builder()
                        .setKey(KEY)
                        .setAttribute(Attribute.MOVEMENT_SPEED)
                        .setSlot(EquipmentSlotGroup.FEET)
                        .setOperation(AttributeModifier.Operation.ADD_NUMBER)
                        .setAmount(0.025)
                        .build()
                )
            )
            .buildPair()
    }

    override fun onRunnable(player: Player) {
        if (Util.isItemInSlot(KEY, EquipmentSlot.FEET, player)) {
            player.addPotionEffect(JUMP_BOOST)
        }
    }

    override fun onArmorChange(player: Player, event: PlayerArmorChangeEvent) {
        if (Util.isItemInSlot(KEY, EquipmentSlot.FEET, player)) {
            player.addPotionEffect(JUMP_BOOST)
        } else {
            player.removePotionEffect(PotionEffectType.JUMP_BOOST)
        }
    }
}