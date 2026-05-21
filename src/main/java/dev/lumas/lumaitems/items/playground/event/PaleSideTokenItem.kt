package dev.lumas.lumaitems.items.playground.event

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

class PaleSideTokenItem : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#7B859D:#996779:#A7957B:#6B496B>Pale Side Token</gradient></b>")
            .lore(
                "<gray>Redeem at <#7B859D>/wonderland<gray>."
            )
            .tier(Tier.WONDERLAND_2026.alt())
            .addSpace(false)
            .hideEnchants(true)
            .material(Material.GHAST_TEAR)
            .persistentData("pale-side-token")
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .buildPair()
    }
}