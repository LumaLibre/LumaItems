package dev.lumas.lumaitems.items.misc.collectible

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

private val COLLECTIBLE_TIER = Tier("<b><#61C856>Collectible</#61C856></b>")

class StPatricksHelmetItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#7AC421:#2D8125:#5CE755>St. Patrick's Helmet</gradient></b>")
            .tier(COLLECTIBLE_TIER)
            .material(Material.TURTLE_HELMET)
            .persistentData("st-patricks-helmet")
            .lore(
                "<gray>A festive helm infused",
                "<gray>with shamrock blessings."
            )
            .buildPair()
    }
}