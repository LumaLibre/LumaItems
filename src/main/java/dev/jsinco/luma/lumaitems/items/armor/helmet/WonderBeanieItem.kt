package dev.jsinco.luma.lumaitems.items.armor.helmet

import dev.jsinco.luma.lumaitems.enums.DefaultAttributes
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import kotlin.random.Random

class WonderBeanieItem : CustomItemFunctions() {

    companion object {
        private val KEY = Util.namespacedKey("wonderbeanie")
        private val CAUSE_WHITE_LIST: Set<EntityPotionEffectEvent.Cause> = setOf(
            EntityPotionEffectEvent.Cause.AREA_EFFECT_CLOUD,
            EntityPotionEffectEvent.Cause.ARROW,
            EntityPotionEffectEvent.Cause.AXOLOTL,
            EntityPotionEffectEvent.Cause.DOLPHIN,
            EntityPotionEffectEvent.Cause.POTION_DRINK,
            EntityPotionEffectEvent.Cause.POTION_SPLASH,
            EntityPotionEffectEvent.Cause.TOTEM
        )
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#B9E4F7>W<#C4E4FA>o<#CEE5FC>n<#D9E5FF>d<#E2E3F6>e<#ECE1ED>r <#F5DCE9>B<#F5DAEE>e<#F5D7F3>a<#F1D7F6>n<#ECD7FA>i<#E8D7FD>e")
            .customEnchants("<#F178A4>Fortitude", "<#f5d7f3>Cauldron")
            .lore(
                "Grants <#F178A4>two</#F178A4> extra hearts",
                "while worn.",
                "",
                "<#f5d7f3>Amplifies</#f5d7f3> all consumable",
                "potion effects received."
            )
            .material(Material.NETHERITE_HELMET)
            .persistentData(KEY)
            .vanillaEnchants(Enchantment.UNBREAKING to 10, Enchantment.PROTECTION to 7, Enchantment.BLAST_PROTECTION to 7, Enchantment.MENDING to 1)
            .tier(Tier.CHRISTMAS_2025)
            .attributeModifiers(
                DefaultAttributes.NETHERITE_HELMET.appendThenGetAttributes(
                    KEY, Attribute.MAX_HEALTH, 4.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD
                )
            )
            .buildPair()
    }

    override fun onPotionEffect(player: Player, event: EntityPotionEffectEvent) {
        if (!Util.isItemInSlot(KEY, EquipmentSlot.HEAD, player) || !CAUSE_WHITE_LIST.contains(event.cause)) {
            return
        }

        val effect = event.newEffect ?: return
        event.isCancelled = true

        player.addPotionEffect(PotionEffect(effect.type, (effect.duration + Random.nextInt(200, 400)), (effect.amplifier + Random.nextInt(1,3)), effect.isAmbient, effect.hasParticles(), effect.hasIcon()))
    }
}