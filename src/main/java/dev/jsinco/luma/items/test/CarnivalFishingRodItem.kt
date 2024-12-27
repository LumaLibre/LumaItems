package dev.jsinco.luma.items.test

import dev.jsinco.luma.util.tiers.Tier
import dev.jsinco.luma.items.ItemFactory
import dev.jsinco.luma.manager.CustomItemFunctions
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
        //val item = event.caught as? Item ?: return
        //if (!item.itemStack.type.toString().contains("APPLE")) {
//            player.sendMessage(Util.colorcode("""
//                        &eWell, this is awkward... I'm catching something but it isn't an apple. I'm not a really big fan of fish that
//                        aren't apples. Why do we even catch fish that aren't apples? I don't know. I'm just a fishing rod. I don't really want to be used
//                        for fishing anyways. Why was I made to catch apples anyways? Can you just put me back, I don't want to be a fishing rod anymore.
//                    """.trimIndent()))
           // event.isCancelled = true
       // }

        if (!player.world.name.contains("event")) {
            return
        }
    }
}