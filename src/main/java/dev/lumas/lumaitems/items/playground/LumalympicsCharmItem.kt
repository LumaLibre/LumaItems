package dev.lumas.lumaitems.items.playground

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

class LumalympicsCharmItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#ff4e50:#fc913a:#f9d62e:#eae374:#97c753>Lumalympics Charm</gradient></b>")
            .lore(
                "A neat little charm you",
                "earned for participating",
                "in summer minigames.",
                "",
                "You wonder what it does...",
                "Maybe you should keep it",
                "around for a while?"
            )
            .tier(Tier.SUMMER_2025)
            .customEnchants("<gradient:#ff4e50:#fc913a:#f9d62e:#eae374:#97c753>Charm</gradient>")
            .material(Material.RESIN_CLUMP)
            .persistentData("lumalympics-charm")
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .buildPair()
    }

}