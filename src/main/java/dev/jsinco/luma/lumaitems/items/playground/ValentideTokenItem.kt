package dev.jsinco.luma.lumaitems.items.playground

import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ValentideTokenItem : CustomItem {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#954381:#EC60B0:#EE80C6:#954381>Valentide Stamp")
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .lore(
                "<gray>A cute little stamp",
                "<gray>for someone special!",
                "",
                "<gray>Event currency part of",
                "<gray>the 2025 Valentine's",
                "<gray>event."
            )
            .tier(Tier.VALENTIDE_2025)
            .persistentData("valentide-stamp")
            .material(Material.RED_DYE)
            .buildPair()
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        return false
    }
}