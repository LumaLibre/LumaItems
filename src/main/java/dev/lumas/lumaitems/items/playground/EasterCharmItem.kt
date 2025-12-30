package dev.lumas.lumaitems.items.playground

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

class EasterCharmItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#A687CA:#36CEDC:#8FE86A:#FFD054:#FF787C>Easter Charm</gradient></b>")
            .lore(
                "A neat little charm you",
                "earned for completing",
                "Explorer Miles.",
                "",
                "You wonder what it does...",
                "Maybe you should keep it",
                "around for a while?"
            )
            .tier(Tier.EASTER_2025)
            .customEnchants("<gradient:#A687CA:#36CEDC:#8FE86A:#FFD054:#FF787C>Charm</gradient>")
            .material(Material.RABBIT_FOOT)
            .persistentData("easter-charm")
            .vanillaEnchants(Enchantment.UNBREAKING to 10, Enchantment.KNOCKBACK to 4)
            .buildPair()
    }

}