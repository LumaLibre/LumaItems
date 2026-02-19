package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.util.extensions.ItemUtil.isMatchingItem
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack

class RecallCompass : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {

        return ItemFactory.builder()
            .name("<b><#BF44D7>R<#B635C7>e<#AD25B8>c<#A316A8>a<#9A0698>l<#A112A5>l <#B02BBE>C<#B838CA>o<#BF44D7>m<#B635C7>p<#AD25B8>a<#A316A8>s<#9A0698>s")
            .customEnchants("<#ff5959>Attunement")
            .lore(
                "A wonderful smelling poppy,",
                "It's so small and cute!",
                "",
                "I wonder if holding it",
                "does anything special?"
            )
            .tier(Tier.VALENTIDE_2026)
            .persistentData("recall-compass")
            .material(Material.COMPASS)
            .buildPair()
    }


    override fun onPlaceBlock(player: Player, event: BlockPlaceEvent) {
        val item = event.itemInHand
        if (!item.isMatchingItem(key)) {
            return
        }
        event.isCancelled = true
    }
}