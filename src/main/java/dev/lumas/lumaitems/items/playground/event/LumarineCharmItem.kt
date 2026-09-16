package dev.lumas.lumaitems.items.playground.event

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment

class LumarineCharmItem : CustomItemFunctions() {
    override fun createItem() = ItemFactory.builder()
        .name("<b><gradient:#1e8abf:#9be4df:#f8898a:#EDB172:#ffe494>Lumarine Charm</gradient></b>")
        .lore(
            "A neat little charm you",
            "earned for participating",
            "in lumarine minigames.",
            "",
            "You wonder what it does...",
            "Maybe you should keep it",
            "around for a while?"
        )
        .tier(Tier.LUMARINE_2026)
        .customEnchants("<gradient:#1e8abf:#9be4df:#f8898a:#EDB172:#ffe494>Charm</gradient>")
        .material(Material.YELLOW_DYE)
        .persistentData("lumarine-charm")
        .vanillaEnchants(Enchantment.UNBREAKING to 10)
        .buildPair()
}