package dev.lumas.lumaitems.items.tools.harrow

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.isTagged
import dev.lumas.lumaitems.util.tags.Kind
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.inventory.ItemStack

class UmbralHoeItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#222222:#555555:#995655:#885644:#cd7755>Umbral Hoe</gradient></b>")
            .customEnchants("<#995655>Shift")
            .material(Material.NETHERITE_HOE)
            .persistentData("umbral-hoe")
            .tier(Tier.WONDERLAND_2026)
            .vanillaEnchants(
                Enchantment.FORTUNE to 5,
                Enchantment.EFFICIENCY to 7,
                Enchantment.UNBREAKING to 9,
                Enchantment.MENDING to 1
            )
            .lore(
                "<#995655>Broken</#995655> crops will",
                "naturally be sent",
                "to your inventory."
            )
            .buildPair()
    }

    override fun onBlockDropItem(player: Player, event: BlockDropItemEvent) {
        if (event.blockState.type.isTagged(Kind.CROPS)) {
            val items = event.items
            for (item in items) {
                val itemStack = item.itemStack
                player.give(itemStack)
            }
            items.clear()
        }
    }
}