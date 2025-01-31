package dev.jsinco.luma.lumaitems.items.armor

import dev.jsinco.luma.lumaitems.enums.DefaultAttributes
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.NamespacedKey
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
        private val causeEffectWhiteList: List<EntityPotionEffectEvent.Cause> = listOf(
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
            .customEnchants("<gray>Boost I", "<#f5d7f3>Cauldron")
            .lore("Amplifies all consumable", "potion effects received.")
            .material(Material.NETHERITE_HELMET)
            .persistentData("wonderbeanie")
            .vanillaEnchants(Enchantment.UNBREAKING to 10, Enchantment.PROTECTION to 7, Enchantment.RESPIRATION to 5, Enchantment.AQUA_AFFINITY to 1, Enchantment.BLAST_PROTECTION to 7, Enchantment.MENDING to 1)
            .tier(Tier.WINTER_2024)
            .attributeModifiers(
                DefaultAttributes.NETHERITE_HELMET.appendThenGetAttributes(
                    Attribute.MAX_HEALTH, AttributeModifier(NamespacedKey(instance(), "wonderbeanie"), 4.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD)
                )
            )
            .buildPair()
    }

    override fun onPotionEffect(player: Player, event: EntityPotionEffectEvent) {
        if (!Util.isItemInSlot("wonderbeanie", EquipmentSlot.HEAD, player) || !causeEffectWhiteList.contains(event.cause)) {
            return
        }

        val effect = event.newEffect ?: return
        event.isCancelled = true

        player.addPotionEffect(PotionEffect(effect.type, (effect.duration + Random.nextInt(200, 400)), (effect.amplifier + Random.nextInt(1,3)), effect.isAmbient, effect.hasParticles(), effect.hasIcon()))
    }
}