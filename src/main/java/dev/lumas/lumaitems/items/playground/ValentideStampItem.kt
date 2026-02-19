package dev.lumas.lumaitems.items.playground

import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItem
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ValentideStampItem : CustomItem {
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
            .material(Material.CLAY_BALL)
            .buildPair()
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        return false
    }
}