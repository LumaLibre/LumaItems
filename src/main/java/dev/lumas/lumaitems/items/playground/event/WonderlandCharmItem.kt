package dev.lumas.lumaitems.items.playground.event

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack

class WonderlandCharmItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#5d85dc:#E56A91:#F3AA4C:#CA51CB>Wonderland Charm</gradient></b>")
            .lore(
                "A neat little charm you",
                "earned for participating",
                "in wonderland minigames.",
                "",
                "You wonder what it does...",
                "Maybe you should keep it",
                "around for a while?"
            )
            .tier(Tier.WONDERLAND_2026)
            .customEnchants("<gradient:#5d85dc:#E56A91:#F3AA4C:#CA51CB>Charm</gradient>")
            .material(Material.RED_MUSHROOM)
            .persistentData("wonderland-charm")
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .buildPair()
    }


    override fun onPlaceBlock(player: Player, event: BlockPlaceEvent) {
        if (event.itemInHand.isMatchingItem("wonderland-charm")) {
            event.isCancelled = true
        }
    }
}