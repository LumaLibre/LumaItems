package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.namespacedKey
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack

class InfiniteMilkBucketItem : CustomItemFunctions() {

    private companion object {
        val KEY = "infinite-milk-bucket".namespacedKey()
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.Companion.builder()
            .name("<b><gradient:#FFFFFF:#AAAAAA>Infinite Milk Bucket</gradient></b>")
            .material(Material.MILK_BUCKET)
            .persistentData(KEY)
            .tier(Tier.PRIDE_2026)
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .customEnchants("<#FFFFFF>Bottomless")
            .lore(
                "A bottomless bucket of",
                "milk that never runs out.",
                "",
                "Drinking it will remove",
                "all your effects without",
                "consuming the bucket."
            )
            .buildPair()
    }

    override fun onConsumeItem(player: Player, event: PlayerItemConsumeEvent) {
        val item = event.item
        if (!item.isMatchingItem(KEY)) return

        event.replacement = item.clone()
    }

    override fun onPlayerEmptyBucket(player: Player, event: PlayerBucketEmptyEvent) {
        val item = event.itemStack ?: return
        if (!item.isMatchingItem(KEY)) return

        event.itemStack = item.clone()
        player.updateInventory()
    }
}