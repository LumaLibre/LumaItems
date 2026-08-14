package dev.lumas.lumaitems.items.misc.nests

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.addCooldown
import dev.lumas.lumaitems.util.extensions.breakNaturallyWithLog
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.isOnCooldown
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.setAirWithLog
import dev.lumas.lumaitems.util.extensions.setBlockDataWithLog
import dev.lumas.lumaitems.util.extensions.setPersistentKey
import dev.lumas.lumaitems.util.extensions.syncDelayed
import kotlin.random.Random
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Levelled
import org.bukkit.block.data.Waterlogged
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.entity.TropicalFish
import org.bukkit.event.block.CauldronLevelChangeEvent
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketEntityEvent
import org.bukkit.event.player.PlayerBucketFillEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

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
        event.isCancelled = true
        placeFluid(player, event.block, Material.WATER, Sound.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS)
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
        event.isCancelled = true
        placeFluid(player, event.block, Material.LAVA, Sound.ITEM_BUCKET_EMPTY_LAVA, SoundCategory.BLOCKS)
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
}

class InfiniteTropicalFishBucketItem : CustomItemFunctions() {

    companion object {
        val INFINITE_FISH_KEY: NamespacedKey = "infinite-fish".namespacedKey()
        private const val ID = "infinite-tropical-fish-bucket"
        private val KEY = ID.namespacedKey()
        private const val FISH_COOLDOWN_TICKS = 4L
    }

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
        .persistentData(KEY)
        .build()
        .createItem()

    override fun createItem(): Pair<String, ItemStack> {
        return Pair(ID, infiniteTropicalFishBucket)
    }

    override fun onPlayerEmptyBucket(player: Player, event: PlayerBucketEmptyEvent) {
        event.isCancelled = true

        val target = event.block
        placeFluid(player, target, Material.WATER, Sound.ITEM_BUCKET_EMPTY_FISH, SoundCategory.NEUTRAL)

        if (player.isOnCooldown(this)) return
        player.addCooldown(this, FISH_COOLDOWN_TICKS)
        spawnFish(player, target)
    }

    private fun spawnFish(player: Player, target: Block) {
        val location = target.location.toCenterLocation()
        target.world.spawn(location, TropicalFish::class.java, CreatureSpawnEvent.SpawnReason.BUCKET) { fish ->
            fish.isFromBucket = true
            fish.setPersistentKey(INFINITE_FISH_KEY, PersistentDataType.SHORT, 1)
        }
        target.playBucketSound(player, Sound.ENTITY_TROPICAL_FISH_AMBIENT, SoundCategory.NEUTRAL)
    }

}

class InfiniteAirBucketItem : CustomItemFunctions() {

    private companion object {
        val KEY = "infinite-air-bucket".namespacedKey()
        const val REFILL_COOLDOWN_TICKS = 60L
        const val BUBBLE_POPS = 5
        val DRAINABLE_RESULTS = setOf(
            Material.WATER_BUCKET,
            Material.LAVA_BUCKET,
            Material.POWDER_SNOW_BUCKET
        )
    }

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
        event.isCancelled = true

        val result = event.itemStack?.type ?: return
        if (result !in DRAINABLE_RESULTS) return

        drainFluid(player, event.block)
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


// Helpers simulating the canceled bucket behavior:

private const val BUCKET_SOUND_RADIUS_SQUARED = 16.0 * 16.0

private val CAULDRONS = setOf(
    Material.CAULDRON,
    Material.WATER_CAULDRON,
    Material.LAVA_CAULDRON,
    Material.POWDER_SNOW_CAULDRON
)

@Suppress("DEPRECATION") // World#isUltraWarm: in contrast to world.getEnvironment(), this is backed by
// world.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES), so it should be more safe
private fun placeFluid(player: Player, target: Block, fluid: Material, emptySound: Sound, category: SoundCategory) {
    val world = target.world

    if (target.type in CAULDRONS) {
        fillCauldron(player, target, fluid, emptySound, category)
        return
    }

    if (fluid == Material.WATER && world.isUltraWarm) {
        val location = target.location.toCenterLocation()
        val pitch = 2.6f + (Random.nextFloat() - Random.nextFloat()) * 0.8f
        target.playBucketSound(player, Sound.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5f, pitch)
        world.spawnParticle(Particle.LARGE_SMOKE, location, 8, 0.25, 0.25, 0.25, 0.0)
        return
    }

    val data = target.blockData
    if (fluid == Material.WATER && data is Waterlogged) {
        if (!data.isWaterlogged) {
            data.isWaterlogged = true
            target.setBlockDataWithLog(player, data)
        }
        target.playBucketSound(player, emptySound, category)
        return
    }

    if (!target.isReplaceable) return
    if (!target.isLiquid && !target.type.isAir) {
        target.breakNaturallyWithLog(player, true, false)
    }
    target.setBlockDataWithLog(player, fluid)

    target.playBucketSound(player, emptySound, category)
}

private fun fillCauldron(player: Player, target: Block, fluid: Material, emptySound: Sound, category: SoundCategory) {
    val filled = when (fluid) {
        Material.WATER -> Material.WATER_CAULDRON.createBlockData().apply {
            (this as? Levelled)?.let { it.level = it.maximumLevel }
        }
        // Lava cannot be poured into a cauldron that is submerged, apparently
        Material.LAVA -> if (target.isUnderWater()) return else Material.LAVA_CAULDRON.createBlockData()
        else -> return
    }

    if (!target.changeCauldron(player, filled, CauldronLevelChangeEvent.ChangeReason.BUCKET_EMPTY)) return
    target.playBucketSound(null, emptySound, category)
}

private fun emptyCauldron(player: Player, target: Block): Boolean {
    val sound = when (target.type) {
        Material.LAVA_CAULDRON -> Sound.ITEM_BUCKET_FILL_LAVA
        Material.POWDER_SNOW_CAULDRON -> Sound.ITEM_BUCKET_FILL_POWDER_SNOW
        else -> Sound.ITEM_BUCKET_FILL
    }

    val emptied = Material.CAULDRON.createBlockData()
    if (!target.changeCauldron(player, emptied, CauldronLevelChangeEvent.ChangeReason.BUCKET_FILL)) return false

    target.playBucketSound(null, sound, SoundCategory.BLOCKS)
    return true
}

private fun Block.changeCauldron(player: Player, newData: BlockData, reason: CauldronLevelChangeEvent.ChangeReason): Boolean {
    val snapshot = state
    snapshot.blockData = newData

    val event = CauldronLevelChangeEvent(this, player, reason, snapshot)
    if (!event.callEvent()) return false

    setBlockDataWithLog(player, event.newState.blockData)
    return true
}

private fun Block.isUnderWater(): Boolean {
    val above = getRelative(BlockFace.UP)
    return above.type == Material.WATER || (above.blockData as? Waterlogged)?.isWaterlogged == true
}

private fun drainFluid(player: Player, target: Block): Boolean {
    val data = target.blockData
    val type = target.type

    if (type in CAULDRONS) return emptyCauldron(player, target)

    when {
        data is Waterlogged && data.isWaterlogged -> {
            data.isWaterlogged = false
            target.setBlockDataWithLog(player, data)
        }

        type == Material.WATER || type == Material.LAVA || type == Material.POWDER_SNOW -> {
            target.setAirWithLog(player)
        }

        else -> return false
    }

    val sound = when (type) {
        Material.LAVA -> Sound.ITEM_BUCKET_FILL_LAVA
        Material.POWDER_SNOW -> Sound.ITEM_BUCKET_FILL_POWDER_SNOW
        else -> Sound.ITEM_BUCKET_FILL
    }
    target.playBucketSound(player, sound, SoundCategory.PLAYERS)
    return true
}

private fun Block.playBucketSound(
    except: Player?,
    sound: Sound,
    category: SoundCategory,
    volume: Float = 1.0f,
    pitch: Float = 1.0f
) {
    val location = this.location.toCenterLocation()
    for (nearby in world.players) {
        if (nearby.uniqueId == except?.uniqueId) continue
        if (nearby.location.distanceSquared(location) > BUCKET_SOUND_RADIUS_SQUARED) continue
        nearby.playSound(location, sound, category, volume, pitch)
    }
}
