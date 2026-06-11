package dev.lumas.lumaitems.items.playground.event

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

class PrideMazeTokenItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#e40303:#ff8c00:#ffed00:#008026:#004dff>Pride Maze Token</gradient></b>")
            .lore(
                "<grey>A token given to those",
                "<grey>who have completed the",
                "<grey>Pride 2026 maze.",
            )
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .material(Material.PINK_PETALS)
            .hideEnchants(true)
            .tier(Tier.PRIDE_2026)
            .persistentData("pride-maze-token-2026")
            .buildPair()
    }

    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        event.isCancelled = true
    }
}