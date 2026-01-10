package dev.lumas.lumaitems.items.playground

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

class ChristmasCharmItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#1d7240:#5e9f52:#e4ba58:#f2b054:#f06f3f:#ee4631:#e4352b:#a61e20>Christmas Charm</gradient></b>")
            .lore(
                "A neat little charm you",
                "earned for participating",
                "in christmas minigames.",
                "",
                "You wonder what it does...",
                "Maybe you should keep it",
                "around for a while?"
            )
            .tier(Tier.CHRISTMAS_2025)
            .customEnchants("<gradient:#1d7240:#5e9f52:#e4ba58:#f2b054:#f06f3f:#ee4631:#e4352b:#a61e20>Charm</gradient>")
            .material(Material.RED_DYE)
            .persistentData("christmas-charm")
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .buildPair()
    }
}