package dev.lumas.lumaitems.items.playground

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack

class Valentide2026CharmItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#954381:#ee78c0:#ec6e95:#cb354e>Valentide Charm</gradient></b>")
            .lore(
                "A neat little charm you",
                "earned for participating",
                "in valentide minigames.",
                "",
                "You wonder what it does...",
                "Maybe you should keep it",
                "around for a while?"
            )
            .tier(Tier.VALENTIDE_2026)
            .customEnchants("<gradient:#954381:#ee78c0:#ec6e95:#cb354e>Charm</gradient>")
            .material(Material.APPLE)
            .persistentData("valentide-2026-charm")
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .buildPair()
    }

    override fun onConsumeItem(player: Player, event: PlayerItemConsumeEvent) {
        event.isCancelled = true
    }
}