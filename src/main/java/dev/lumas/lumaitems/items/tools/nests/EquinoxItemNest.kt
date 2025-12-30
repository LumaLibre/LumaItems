package dev.lumas.lumaitems.items.tools.nests

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.particles.ParticleDisplay
import dev.lumas.lumaitems.particles.Particles
import dev.lumas.lumaitems.shapes.Sphere
import dev.lumas.lumaitems.util.AbilityUtil
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.Executors
import dev.lumas.lumaitems.util.QuickTasks
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.BlockUtil.setAirWithLog
import dev.lumas.lumaitems.util.tiers.Tier
import java.awt.Color
import kotlin.random.Random
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

abstract class EquinoxItemNest : CustomItemFunctions() {

    companion object {
        private val DYES = Material.entries.filter { it.name.matches(Regex(".*_DYE")) }
        private val COLORS = listOf("#feb17d", "#f9ce90", "#f9f2db", "#b8d1c0", "#af97c7", "#ed9bb0")
            .map { Color.decode(it) }
        private val ACTIVATOR_KEY = Util.namespacedKey("equinox-nest-activator")
        private const val SPEED_FACTOR = 3.0
        private const val MAX_FLY_TIME = 200 // 10s
    }


    abstract fun delegateBreakBlock(player: Player, block: Block): List<Item>?
    abstract fun key(): NamespacedKey
    abstract fun cooldownTime(): Long

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (QuickTasks.isOnCooldown(this, player.uniqueId)) return
        QuickTasks.addCooldown(this, player.uniqueId, cooldownTime())

        seed(player, 3)
    }

    private fun seed(player: Player, amt: Int) {
        val snowballs = mutableMapOf<Snowball, ParticleDisplay>()
        repeat(amt) {
            val spawnLoc = BukkitVectors.randomGoalLocation(player.location, 0.35, 1.0, 0.0)
            val snowball = SnowballGeyser.snowball(spawnLoc, player, true, key()).also {
                snowballs[it] = ParticleDisplay.of(Particle.DUST).withColor(COLORS.random())
            }
            SnowballGeyser.propelAway(player.location, spawnLoc, snowball, 0.25)
        }
        Executors.asyncTimer(0, 1) { task ->
            if (snowballs.keys.all { it.isDead || !it.isValid }) {
                task.cancel()
                return@asyncTimer
            }

            for ((ball, particleDisplay) in snowballs) {
                particleDisplay.spawn(ball.location)
            }
        }
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        (event.hitEntity as? LivingEntity)?.let { hitEntity ->
            if (hitEntity == player || AbilityUtil.noDamagePermission(player, hitEntity)) return@let
            hitEntity.damage(5.0, player)
        }

        val pin = event.entity.location

        pin.world.playSound(pin, Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, 0.3f, 2f)

        if (Util.hasPersistentKey(event.entity, ACTIVATOR_KEY)) {
            val snowballGeyser = SnowballGeyser(player, pin, 7 to 12, 5, key())
            snowballGeyser.startTicking()
            return
        }

        val sphere = Sphere(pin, 1.5, 0.0)
        sphere.getSphereFast { block ->
            val drops = delegateBreakBlock(player, block)
            if (drops != null) {
                flyToPlayerExecutor(drops, player)
            }
        }

        Executors.async { task ->
            val display = ParticleDisplay.of(Particle.DUST)
                .withColor(COLORS.random())
                .withLocation(pin)

            Particles.sphere(0.5, 5.0, display)
        }
    }

    private fun flyToPlayerExecutor(items: List<Item>, player: Player) {
        var count = 0

        Executors.syncTimer(0, 1) { task ->
            if (++count > MAX_FLY_TIME || items.all { isWithinDistance(it, player, 2.0) || !it.isValid }) {
                task.cancel()
            } else {
                items.forEach { item ->
                    val newVel = BukkitVectors.flyToLivingEntity(player, item, SPEED_FACTOR)
                    item.velocity = newVel
                }
            }
        }
    }

    private fun isWithinDistance(item: Item, player: Player, distance: Double): Boolean {
        if (item.world != player.world) return true
        return item.location.distanceSquared(player.location) <= distance * distance
    }


    private class SnowballGeyser(
        val player: Player,
        val pin: Location,
        val snowballCountRange: Pair<Int, Int>,
        val pulses: Int,
        val key: NamespacedKey,
        val pulseDelay: Long = 20,
    ) {

        val snowballs: MutableList<Snowball> = mutableListOf()
        val particleDisplay = ParticleDisplay.of(Particle.DUST).withColor(COLORS.random())
        var count: Long = pulseDelay
        var pulseCount = 0
        var started: Boolean = false


        fun tryPulse(addTicks: Long): Boolean {
            if (count < pulseDelay) {
                count += addTicks
                return true
            } else if (pulseCount >= pulses) {
                return false
            }
            count = 0
            pulseCount++


            val amt = Random.Default.nextInt(snowballCountRange.first, snowballCountRange.second)
            Executors.sync {
                pin.world.playSound(pin, Sound.ENTITY_WARDEN_HEARTBEAT, 0.5f, 7f)
                repeat(amt) {
                    val loc = BukkitVectors.randomGoalLocation(pin, 0.35, 1.0, 0.0)
                    val snowball = snowball(loc, player, false, key)
                    val vector = BukkitVectors.propelAway(pin, loc, 0.3, 1.5)
                    snowball.velocity = vector
                    snowballs.add(snowball)
                }
            }
            return true
        }

        fun startTicking() {
            if (this.started) return
            this.started = true
            val eyeDisplay = particleDisplay.clone().withLocation(pin)
            val lineEnd = pin.clone().add(0.0, 1.5, 0.0)
            Executors.asyncTimer(0, 1) { task ->
                snowballs.removeIf { it.isDead || !it.isValid }
                Particles.neopaganPentagram(1.5, 0.1, 0.0, 0.1, 300.0, eyeDisplay, eyeDisplay)
                Particles.line(pin, lineEnd, 0.3, eyeDisplay)
                if (snowballs.isEmpty() && !tryPulse(1)) {
                    task.cancel()
                    return@asyncTimer
                }
                for (snowball in snowballs) {
                    particleDisplay.spawn(snowball.location)
                }
            }
        }


        companion object {

            fun snowball(spawnLoc: Location, shooter: Player, seed: Boolean, key: NamespacedKey): Snowball {
                val ball = spawnLoc.world.spawn(spawnLoc, Snowball::class.java)
                if (seed) {
                    Util.setPersistentKey(ball, ACTIVATOR_KEY, PersistentDataType.BOOLEAN, true)
                } else {
                    ball.item = ItemStack(DYES.random())
                }

                Util.setPersistentKey(ball, key, PersistentDataType.SHORT, 1)
                ball.shooter = shooter

                return ball
            }


            fun propelAway(pin: Location, point: Location, entity: Entity, speed: Double = 0.3, y: Double = 1.5) {
                val playerVec = pin.toVector()
                val pointVec = point.toVector()

                val direction = pointVec.subtract(playerVec).normalize()

                direction.setY(y)

                entity.velocity = direction.multiply(speed)
            }
        }
    }
}


class EquinoxHatchetItem : EquinoxItemNest() {

    companion object {
        private val TREE_PATTERN = Regex(".*(LOG|WOOD|STEM|HYPHAE|LEAVES|WART_BLOCK|SHROOM).*")
        private val FORTUNE_5_AXE = ItemStack(Material.NETHERITE_AXE)
            .apply { addUnsafeEnchantment(Enchantment.FORTUNE, 5) }
        private val KEY = Util.namespacedKey("equinox-hatchet")
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.Companion.builder()
            .name("<b><gradient:#feb17d:#f9ce90:#f9f2db:#b8d1c0:#af97c7:#ed9bb0>Equinox Hatchet</gradient></b>")
            .customEnchants("<#f9f2db>Pentageyser")
            .persistentData(KEY)
            .material(Material.NETHERITE_AXE)
            .tier(Tier.HALLOWEEN_2025)
            .vanillaEnchants(
                Enchantment.FORTUNE to 5,
                Enchantment.EFFICIENCY to 8,
                Enchantment.MENDING to 1,
                Enchantment.UNBREAKING to 9
            )
            .lore(
                "<#f9f2db>Right-click</#f9f2db> to launch",
                "<#f9f2db>3</#f9f2db> geyser seeds in various",
                "directions around you.",
                "",
                "Geysers launched from this",
                "tool will pulse, launching",
                "several destructive balls",
                "that can break down trees.",
                "",
                "<red>Cooldown: 3m"
            )
            .buildPair()
    }


    override fun delegateBreakBlock(player: Player, block: Block): List<Item>? {
        if (!block.type.name.matches(TREE_PATTERN) || AbilityUtil.noBuildPermission(player, block)) {
            return null
        }
        val drops = block.getDrops(FORTUNE_5_AXE).map { itemStack ->
            block.world.dropItemNaturally(block.location, itemStack)
        }

        val sound =
            if (block.type.name.endsWith("_LEAVES")) { // lazy check for sfx
                Sound.BLOCK_GRASS_BREAK
            } else {
                Sound.BLOCK_WOOD_BREAK
            }
        block.world.playSound(block.location, sound, 0.5f, 1f)

        block.setAirWithLog(player)
        player.inventory.itemInMainHand.damage(1, player) // no checks
        return drops
    }

    override fun key(): NamespacedKey {
        return KEY
    }


    override fun cooldownTime(): Long {
        return 2400L
    }
}


class EquinoxSpadeItem : EquinoxItemNest() {

    companion object {
        // Based on: https://minecraft.wiki/w/Shovel
        private val SHOVEL_PATTERN = Regex(
            ".*CONCRETE_POWDER|.*DIRT.*|FARMLAND|GRASS_BLOCK|.*GRAVEL|MUD|MUDDY_MANGROVE_ROOTS|" +
                    "MYCELIUM|PODZOL|.*SAND|SNOW.*|SOUL_SOIL")
        private val FORTUNE_5_SHOVEL = ItemStack(Material.NETHERITE_SHOVEL)
            .apply { addUnsafeEnchantment(Enchantment.FORTUNE, 5) }
        private val KEY = Util.namespacedKey("equinox-harrower")
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#feb17d:#f9ce90:#f9f2db:#b8d1c0:#af97c7:#ed9bb0>Equinox Spade</gradient></b>")
            .customEnchants("<#f9f2db>Pentageyser")
            .persistentData(KEY)
            .material(Material.NETHERITE_SHOVEL)
            .tier(Tier.HALLOWEEN_2025)
            .vanillaEnchants(
                Enchantment.FORTUNE to 5,
                Enchantment.EFFICIENCY to 8,
                Enchantment.MENDING to 1,
                Enchantment.UNBREAKING to 9
            )
            .lore(
                "<#f9f2db>Right-click</#f9f2db> to launch",
                "<#f9f2db>3</#f9f2db> geyser seeds in various",
                "directions around you.",
                "",
                "Geysers launched from this",
                "tool will pulse, launching",
                "several destructive balls",
                "that can break blocks",
                "suitable for shovelling.",
                "",
                "<red>Cooldown: 3m"
            )
            .buildPair()
    }

    override fun delegateBreakBlock(player: Player, block: Block): List<Item>? {
        if (!block.type.name.matches(SHOVEL_PATTERN) || AbilityUtil.noBuildPermission(player, block)) {
            return null
        }

        val drops = block.getDrops(FORTUNE_5_SHOVEL).map { itemStack ->
            block.world.dropItemNaturally(block.location, itemStack)
        }
        block.world.playSound(block.location, Sound.BLOCK_GRAVEL_BREAK, 0.5f, 1f)
        block.setAirWithLog(player)
        player.inventory.itemInMainHand.damage(1, player) // no checks
        return drops
    }

    override fun key(): NamespacedKey {
        return KEY
    }

    override fun cooldownTime(): Long {
        return 3600L
    }
}