package dev.lumas.lumaitems.items.misc.nests

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.inventory.ItemStack

class InfiniteWaterBucketItem : CustomItemFunctions() {

    private val infiniteWaterBucket: ItemStack = ItemFactory.builder()
        .name("<b><gradient:#4498DB:#778CF4>Infinite Wa</gradient><gradient:#778CF4:#2E5FD7>ter Bucket</gradient></b>")
        .customEnchants("<#0098de>Bottomless")
        .lore(
            "This bucket never runs",
            "out of <#0098de>water</#0098de>, place to",
            "your heart's content!"
        )
        .material(Material.WATER_BUCKET)
        .vanillaEnchants(Enchantment.UNBREAKING to 10)
        .tier(Tier.SUMMER_2025)
        .persistentData("infinite-water-bucket")
        .build()
        .createItem()

    override fun createItem(): Pair<String, ItemStack> {
        return Pair("infinite-water-bucket", infiniteWaterBucket)
    }

    override fun onPlayerEmptyBucket(player: Player, event: PlayerBucketEmptyEvent) {
        event.itemStack = infiniteWaterBucket
        //player.updateInventory() Currently unnecessary - any reports on this?
    }

}

class InfiniteLavaBucketItem : CustomItemFunctions() {

    private val infiniteLavaBucket: ItemStack = ItemFactory.builder()
        .name("<b><gradient:#E55F33:#E09738>Infinite L</gradient><gradient:#E09738:#d53e0f>ava Bucket</gradient></b>")
        .customEnchants("<#FF4500>Bottomless")
        .lore(
            "This bucket never runs",
            "out of <#FF4500>lava</#FF4500>, place to",
            "your heart's content!"
        )
        .material(Material.LAVA_BUCKET)
        .vanillaEnchants(Enchantment.UNBREAKING to 10)
        .tier(Tier.SUMMER_2025)
        .persistentData("infinite-lava-bucket")
        .build()
        .createItem()

    override fun createItem(): Pair<String, ItemStack> {
        return Pair("infinite-lava-bucket", infiniteLavaBucket)
    }

    override fun onPlayerEmptyBucket(player: Player, event: PlayerBucketEmptyEvent) {
        event.itemStack = infiniteLavaBucket
        //player.updateInventory() Currently unnecessary - any reports on this?
    }

}
