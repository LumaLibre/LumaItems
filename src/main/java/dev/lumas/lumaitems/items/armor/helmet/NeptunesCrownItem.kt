package dev.lumas.lumaitems.items.armor.helmet

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class NeptunesCrownItem : CustomItemFunctions() {

    companion object {
        private val key = Util.namespacedKey("neptunes-crown")
        private val WATER_BREATHING = PotionEffect(PotionEffectType.WATER_BREATHING, 300, 0, true, false, false)
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#006cd0>N<#0c76cf>e<#1780ce>p<#238acd>t<#2e94cc>u<#3a9ecb>n<#45a8ca>e<#53acc3>'<#63aab4>s <#73a8a5>C<#82a796>r<#92a587>o<#a2a378>w<#b2a169>n")
            .persistentData(key)
            .material(Material.AMETHYST_CLUSTER)
            .autoHat(true)
            .vanillaEnchants(Enchantment.PROTECTION to 8)
            .tier(Tier.SUMMER_2025)
            .lore(
                "Wear this shimmering crown to",
                "breathe underwater and avoid",
                "mining fatigue while submerged."
            )
            .buildPair()
    }

    override fun onRunnable(player: Player) {
        if (Util.isItemInSlot(key, EquipmentSlot.HEAD, player)) {
            player.removePotionEffect(PotionEffectType.MINING_FATIGUE)
            if (player.isInWater) {
                player.addPotionEffect(WATER_BREATHING)
            }
        }
    }
}