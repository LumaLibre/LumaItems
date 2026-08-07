package dev.lumas.lumaitems.items.misc.nests

import dev.lumas.lumaitems.annotations.FireAnyways
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.addCooldown
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.isOnCooldown
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.syncDelayed
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.entity.TropicalFish
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketEntityEvent
import org.bukkit.event.player.PlayerBucketFillEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
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
        if (player.isOnCooldown(this)) {
            event.isCancelled = true
            return
        }
        player.addCooldown(this, 1)
        event.itemStack = infiniteWaterBucket
        player.updateInventory()
    }

    override fun onBucketCaptureEntity(player: Player, event: PlayerBucketEntityEvent) {
        event.isCancelled = true
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
        if (player.isOnCooldown(this)) {
            event.isCancelled = true
            return
        }
        player.addCooldown(this, 1)
        event.itemStack = infiniteLavaBucket
        player.updateInventory()
    }

}

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

@FireAnyways(Action.BUCKET_RELEASE_ENTITY)
class InfiniteTropicalFishBucketItem : CustomItemFunctions() {

    private val infiniteTropicalFishBucket: ItemStack = ItemFactory.builder()
        .name("<b><gradient:#4498DB:#F5A623:#F5A623:#E94F37>Infinite Fish Bucket</gradient></b>")
        .customEnchants("<#0098de>Bottomless")
        .lore(
            "This bucket never",
            "runs out of <#F5A623>fish</#F5A623>",
            "or <#0098de>water</#0098de> to place!"
        )
        .material(Material.TROPICAL_FISH_BUCKET)
        .vanillaEnchants(Enchantment.UNBREAKING to 10)
        .tier(Tier.LUMARINE_2026)
        .persistentData("infinite-tropical-fish-bucket")
        .build()
        .createItem()

    private val bucketName: Component? = infiniteTropicalFishBucket.itemMeta?.customName()

    override fun createItem(): Pair<String, ItemStack> {
        return Pair("infinite-tropical-fish-bucket", infiniteTropicalFishBucket)
    }

    override fun onPlayerEmptyBucket(player: Player, event: PlayerBucketEmptyEvent) {
        if (player.isOnCooldown(this)) {
            event.isCancelled = true
            return
        }
        player.addCooldown(this, 15*20)
        event.itemStack = infiniteTropicalFishBucket
        player.updateInventory()
    }

    override fun onBucketReleaseEntity(event: CreatureSpawnEvent) {
        // future note: this is an okay way to check if the fish is ours, bukkit doesn't
        // fire enough events for this, and there's no way to grab store on entity PDC
        val fish = event.entity as? TropicalFish ?: return
        val name = fish.customName() ?: return
        if (name != bucketName) return
        fish.customName(null)
        fish.isCustomNameVisible = false
    }

}

class InfiniteAirBucketItem : CustomItemFunctions() {

    private companion object {
        val KEY = "infinite-air-bucket".namespacedKey()
        const val REFILL_COOLDOWN_TICKS = 60L
        const val BUBBLE_POPS = 5
    }
    // TODO: scope this to just removing liquids instead of refilling player air bars

    private val infiniteAirBucket: ItemStack = ItemFactory.builder()
        .name("<b><gradient:#B3E5FC:#E0F5FF:#B3E5FC:#E0F5FF>Infinite Air Bucket</gradient></b>")
        .customEnchants("<#B3E5FC>Bottomless")
        .lore(
            "This bucket drains any",
            "liquid without ever",
            "filling up.",
            "",
            "Press your <#B3E5FC>swap key (F)</#B3E5FC>",
            "to refill your air bar."
        )
        .material(Material.BUCKET)
        .vanillaEnchants(Enchantment.UNBREAKING to 10)
        .tier(Tier.LUMARINE_2026)
        .persistentData(KEY)
        .maxStackSize(1)
        .build()
        .createItem()

    override fun createItem(): Pair<String, ItemStack> {
        return Pair("infinite-air-bucket", infiniteAirBucket)
    }

    override fun onPlayerFillBucket(player: Player, event: PlayerBucketFillEvent) {
        if (player.isOnCooldown(this)) {
            event.isCancelled = true
            return
        }
        player.addCooldown(this, 1)
        event.itemStack = infiniteAirBucket
        player.updateInventory()
    }

    override fun onPlayerSwapHands(player: Player, event: PlayerSwapHandItemsEvent) {
        if (!event.mainHandItem.isMatchingItem(KEY) && !event.offHandItem.isMatchingItem(KEY)) return
        event.isCancelled = true

        if (player.remainingAir >= player.maximumAir) return
        if (player.isOnCooldown(this)) return // TODO: cooldown effects

        player.addCooldown(this, REFILL_COOLDOWN_TICKS)
        player.remainingAir = player.maximumAir
        playAirRefillSounds(player)
    }

    private fun playAirRefillSounds(player: Player) {
        repeat(BUBBLE_POPS) { i ->
            val t = i.toFloat() / (BUBBLE_POPS - 1)
            val delay = 1L + (t * t * 7f).toLong()
            val pitch = 0.8f + t * 0.8f + (random.nextFloat() - 0.5f) * 0.1f
            player.syncDelayed(delay) {
                player.playSound(player.location, Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, 1.5f, pitch)
            }
        }
        player.syncDelayed(9L) {
            player.playSound(player.location, Sound.AMBIENT_UNDERWATER_EXIT, 0.9f, 1.3f)
        }
    }
}

// TODO: Infinite Powder Snow Bucket - more suited for a winter event