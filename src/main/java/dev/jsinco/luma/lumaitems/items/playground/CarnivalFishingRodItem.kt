package dev.jsinco.luma.lumaitems.items.playground

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.inventory.ItemStack

class CarnivalFishingRodItem : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#8EC4F7:#ff9ccb>Carni</gradient><gradient:#ff9ccb:#d7f58d>val F</gradient><gradient:#d7f58d:#fffe8a>ishin</gradient><gradient:#fffe8a:#ffd365>g Rod</gradient></b>")
            .customEnchants(mutableListOf("<gray>Unbreakable"))
            .material(Material.FISHING_ROD)
            .persistentData("carnivalfishingrod")
            .vanillaEnchants(mutableMapOf(Enchantment.LURE to 3))
            .tier(Tier.CARNIVAL_2024)
            .unbreakable(true)
            .buildPair()
    }

    override fun onFish(player: Player, event: PlayerFishEvent) {
        // handle this later
        //event.isCancelled = true
    }
}