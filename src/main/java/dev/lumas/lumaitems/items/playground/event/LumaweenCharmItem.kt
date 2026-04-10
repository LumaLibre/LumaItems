package dev.lumas.lumaitems.items.playground.event

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

class LumaweenCharmItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#602749:#b14623:#f6921d>Lumaween Charm</gradient></b>")
            .lore(
                "A neat little charm you",
                "earned for participating",
                "in halloween minigames.",
                "",
                "You wonder what it does...",
                "Maybe you should keep it",
                "around for a while?"
            )
            .tier(Tier.HALLOWEEN_2025)
            .customEnchants("<gradient:#602749:#b14623:#f6921d>Charm</gradient>")
            .material(Material.GLOWSTONE_DUST)
            .persistentData("lumaween-charm")
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .buildPair()
    }
}