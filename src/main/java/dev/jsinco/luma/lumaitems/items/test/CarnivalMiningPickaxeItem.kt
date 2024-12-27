package dev.jsinco.luma.lumaitems.items.test

import dev.jsinco.luma.lumaitems.util.tiers.Tier
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType

class CarnivalMiningPickaxeItem : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#8EC4F7:#ff9ccb>Carn</gradient><gradient:#ff9ccb:#d7f58d>ival</gradient><gradient:#d7f58d:#fffe8a> Pic</gradient><gradient:#fffe8a:#ffd365>kaxe</gradient></b>")
            .customEnchants(mutableListOf("<gray>Unbreakable"))
            .material(Material.DIAMOND_PICKAXE)
            .persistentData("carnivalminingpickaxe")
            .vanillaEnchants(mutableMapOf(Enchantment.EFFICIENCY to 2))
            .tier(Tier.CARNIVAL_2024)
            .unbreakable(true)
            .buildPair()
    }

    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        if (player.hasPotionEffect(PotionEffectType.HASTE)) {
            player.removePotionEffect(PotionEffectType.HASTE)
        }
        event.isCancelled = true
    }
}
