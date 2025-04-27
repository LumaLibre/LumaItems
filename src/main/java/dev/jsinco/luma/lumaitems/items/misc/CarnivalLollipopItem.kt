package dev.jsinco.luma.lumaitems.items.misc

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class CarnivalLollipopItem : CustomItemFunctions() {

    private val haste = PotionEffect(PotionEffectType.HASTE, 260, 0, false, false, false)
    private val speed = PotionEffect(PotionEffectType.SPEED, 260, 0, false, false, false)

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#FFB8B8>L<#C2DC9F>o<#85FF85>l<#7CFFB6>l<#72FFE6>i<#56B5FF>p<#979FFF>o<#D889FF>p</b>")
            .lore("A five dollar lollipop", "you bought.", "", "You aren't sure why it", "was so expensive.")
            .vanillaEnchants(mutableMapOf(Enchantment.UNBREAKING to 10))
            .persistentData("carnivallollipop")
            .customEnchants("<#FFB8B8>Sweetness")
            .material(Material.ALLIUM)
            .tier(Tier.CARNIVAL_2024)
            .buildPair()
    }

    override fun onRunnable(player: Player) {
        player.addPotionEffect(haste)
        player.addPotionEffect(speed)
    }
}