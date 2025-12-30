package dev.lumas.lumaitems.items.playground

import dev.lumas.lumaitems.util.tiers.Tier
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

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

    override fun onLeftClick(player: Player, event: PlayerInteractEvent) {
        event.isCancelled = true
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        event.isCancelled = true
    }

    override fun onGenericInteract(player: Player, event: PlayerInteractEvent) {
        event.isCancelled = true
    }

    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        event.isCancelled = true
    }
}
