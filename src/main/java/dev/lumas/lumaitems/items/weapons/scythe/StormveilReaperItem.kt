package dev.lumas.lumaitems.items.weapons.scythe

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent
import dev.lumas.lumaitems.annotations.Disable
import dev.lumas.lumaitems.annotations.FireAnyways
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.enums.WorldKey
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.particles.ParticleDisplay
import dev.lumas.lumaitems.particles.Particles
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.Executors
import dev.lumas.lumaitems.util.extensions.addCooldown
import dev.lumas.lumaitems.util.extensions.canDamage
import dev.lumas.lumaitems.util.extensions.isItemInSlot
import dev.lumas.lumaitems.util.extensions.isOnCooldown
import dev.lumas.lumaitems.util.extensions.removePersistentKey
import dev.lumas.lumaitems.util.extensions.setPersistentKey
import dev.lumas.lumaitems.util.extensions.spell
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.extensions.syncDelayed
import dev.lumas.lumaitems.util.extensions.syncTimer
import dev.lumas.lumaitems.util.extensions.toColor
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import java.awt.Color
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.ItemMergeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MainHand
import org.bukkit.loot.LootContext
import org.bukkit.loot.Lootable
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector

@Disable(WorldKey.PINATA)
@FireAnyways(Action.ENTITY_DEATH)
class StormveilReaperItem : CustomItemFunctions() {

    private companion object {
        val KEY = Util.namespacedKey("stormveil-reaper")

        const val SEARCH_RADIUS = 6.0
        const val LOOM_TICKS = 900
        const val DAMAGE_MULTIPLIER = 2.0
        const val COOLDOWN_TICKS = 23 * 20L

        const val VOID_TICKS = 30

        const val BOUNTY_CHANCE = 0.12
        const val BOUNTY_MIN_MULTIPLIER = 1.5
        const val BOUNTY_MAX_MULTIPLIER = 2.0
        const val VOID_CHANCE = 0.10

        const val DROP_STAGGER = 2L
        const val NO_MERGE_TICKS = 600L

        /** Only a fraction of landings are heard: one plink per drop would be a rattle. */
        const val DRIP_SOUND_CHANCE = 0.25
        const val DRIP_SOUND_VOLUME = 0.08f

        const val WIND_SOUND_INTERVAL = 70
        const val WIND_SOUND_VOLUME = 0.25f
        const val FORM_SOUND_VOLUME = 0.5f
        const val FORM_SOUND_PITCH = 1.4f

        const val THUNDER_VOLUME = 0.8f
        const val THUNDER_PITCH = 1.5f

        const val CLOUD_HEIGHT = 2.0
        const val DROP_RADIUS = 1.5
        const val FALL_SPEED = 1.1
        const val DROP_INTERVAL = 2

        const val HAND_SIDE_OFFSET = 0.4
        const val HAND_FORWARD_OFFSET = 0.4
        const val HAND_DROP_OFFSET = 1.0
        const val CAST_DURATION_TICKS = 12
        const val CAST_SAMPLE_DISTANCE = 0.45
        const val CAST_TURNS = 2.0
        const val CAST_MIN_RADIUS = 0.08
        const val CAST_MAX_RADIUS = 0.28
        const val CAST_MIN_ARC_HEIGHT = 0.4
        const val CAST_MAX_ARC_HEIGHT = 2.0

        val RAIN = ParticleDisplay.of(Particle.RAIN)

        /** Wisps in flight. The mark lands on arrival, so this guards a recast in the meantime. */
        val SEEDING: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

        val LOOT_RANDOM: ThreadLocal<java.util.Random> = ThreadLocal.withInitial { java.util.Random() }
        val BOUNTY = "#97dcfb".spell()
        val DUST = ParticleDisplay.of(Particle.DUST).withColor(Color.DARK_GRAY)
        val STANDARD_COLORS = listOf("#3f4a63", "#6f82b6", "#97dcfb", "#f9ecde", "#c9d4e8").map { it.toColor() }
        val CAST_ACCENT = "#97dcfb".toColor()

        fun expiryOf(holder: PersistentDataHolder): Long? =
            Util.getPersistentKey(holder, KEY, PersistentDataType.LONG)

        fun looming(holder: PersistentDataHolder): Boolean =
            (expiryOf(holder) ?: return false) > System.currentTimeMillis()


        fun heldColor(player: Player): Color {
            val name = player.inventory.itemInMainHand.itemMeta?.customName() ?: return STANDARD_COLORS.random()
            val color = nameColors(name).randomOrNull() ?: return STANDARD_COLORS.random()
            return Color(color.red(), color.green(), color.blue())
        }


        private fun nameColors(component: Component): List<TextColor> {
            val found = mutableListOf<TextColor>()

            fun walk(node: Component) {
                node.color()?.let { found.add(it) }
                node.children().forEach(::walk)
            }

            walk(component)
            return found
        }
    }

    override fun createItem() = ItemFactory.builder()
        .name("<b><gradient:#3f4a63:#6f82b6:#97dcfb:#f9ecde>Stormveil Reaper</gradient></b>")
        .customEnchants("<#97dcfb>Overcast")
        .persistentData(KEY)
        .material(Material.NETHERITE_HOE)
        .tier(Tier.LUMARINE_2026)
        .vanillaEnchants(
            Enchantment.SHARPNESS to 8,
            Enchantment.BANE_OF_ARTHROPODS to 8,
            Enchantment.LOOTING to 6,
            Enchantment.UNBREAKING to 5,
            Enchantment.MENDING to 1
        )
        .lore(
            "<#97dcfb>Right-click</#97dcfb> to hang storm",
            "clouds over nearby mobs",
            "in your line of sight.",
            "",
            "When damaged, entities with",
            "clouds may take up to <#97dcfb>2x</#97dcfb>",
            "the damage dealt to them.",
            "",
            "A cloud may rain <#97dcfb>double</#97dcfb>",
            "the loot, or swallow it",
            "whole.",
            "",
            "<red>Cooldown: 23s"
        )
        .buildPair()

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (player.isOnCooldown(this) || !player.isItemInSlot(KEY, EquipmentSlot.HAND)) return

        val loc = (player.getTargetEntity(50) as? LivingEntity)?.location
            ?: player.getTargetBlockExact(15)?.location
            ?: player.location.add(player.location.direction.multiply(10))

        val targets = loc.getNearbyLivingEntities(SEARCH_RADIUS).filter { target ->
            target != player && !looming(target) && target.uniqueId !in SEEDING &&
                player.canDamage(target) && target !is Player && target !is ArmorStand
        }
        if (targets.isEmpty()) return

        player.addCooldown(this, COOLDOWN_TICKS)

        castTrails(player, targets)
        player.world.playSound(loc, Sound.ENTITY_EVOKER_PREPARE_ATTACK, 0.1f, 1.5f)
    }

    private fun castTrails(player: Player, targets: Collection<LivingEntity>) {
        val eye = player.eyeLocation
        val yaw = Math.toRadians(eye.yaw.toDouble())
        val side = Vector(-cos(yaw), 0.0, -sin(yaw))
        if (player.mainHand == MainHand.LEFT) side.multiply(-1)

        val origin = eye.clone()
            .add(side.multiply(HAND_SIDE_OFFSET))
            .add(eye.direction.multiply(HAND_FORWARD_OFFSET))
            .subtract(0.0, HAND_DROP_OFFSET, 0.0)

        origin.world.spawnParticle(Particle.CLOUD, origin, 6, 0.12, 0.08, 0.12, 0.01)

        targets.forEach { target ->
            SEEDING.add(target.uniqueId)

            castWisp(origin, target.eyeLocation.add(0.0, CLOUD_HEIGHT, 0.0), heldColor(player)) {
                SEEDING.remove(target.uniqueId)
                // Back onto the target's own region: loomOver writes its PDC.
                target.sync {
                    if (target.isValid && !target.isDead) loomOver(target)
                }
            }
        }
    }

    private fun castWisp(origin: Location, end: Location, color: Color, onArrival: () -> Unit) {
        val path = end.toVector().subtract(origin.toVector())
        val length = path.length()
        if (length < 0.001) return

        val direction = path.clone().multiply(1.0 / length)
        val reference = if (abs(direction.y) > 0.9) Vector(1, 0, 0) else Vector(0, 1, 0)
        val side = direction.clone().crossProduct(reference).normalize()
        val up = side.clone().crossProduct(direction).normalize()
        val arcHeight = (length * 0.12).coerceIn(CAST_MIN_ARC_HEIGHT, CAST_MAX_ARC_HEIGHT)
        val displays = listOf(
            DUST.clone().withColor(color, 1.15f),
            DUST.clone().withColor(CAST_ACCENT, 0.85f)
        )

        fun centerAt(progress: Double): Location = origin.clone()
            .add(path.clone().multiply(progress))
            .add(up.clone().multiply(sin(Math.PI * progress) * arcHeight))

        fun strandAt(progress: Double, phase: Double): Location {
            val radius = CAST_MIN_RADIUS + sin(Math.PI * progress) * (CAST_MAX_RADIUS - CAST_MIN_RADIUS)
            val angle = (Math.TAU * CAST_TURNS * progress) + phase

            return centerAt(progress)
                .add(side.clone().multiply(cos(angle) * radius))
                .add(up.clone().multiply(sin(angle) * radius))
        }

        var tick = 0
        Executors.asyncTimer(0, 1) { task ->
            val previous = tick.toDouble() / CAST_DURATION_TICKS
            tick++
            val progress = (tick.toDouble() / CAST_DURATION_TICKS).coerceAtMost(1.0)
            val samples = ceil(length * (progress - previous) / CAST_SAMPLE_DISTANCE)
                .toInt()
                .coerceAtLeast(1)

            repeat(samples) { sample ->
                val point = previous + (progress - previous) * ((sample + 1.0) / samples)
                displays.forEachIndexed { strand, display ->
                    display.spawn(strandAt(point, strand * Math.PI))
                }
            }

            val head = centerAt(progress)
            if (tick % 2 == 0) {
                head.world.spawnParticle(Particle.ELECTRIC_SPARK, head, 2, 0.08, 0.08, 0.08, 0.02)
            }

            if (progress >= 1.0) {
                head.world.spawnParticle(Particle.CLOUD, head, 14, 0.4, 0.15, 0.4, 0.02)
                head.world.spawnParticle(Particle.ELECTRIC_SPARK, head, 8, 0.3, 0.2, 0.3, 0.03)
                head.sync {
                    head.world.playSound(head, Sound.ENTITY_GENERIC_SPLASH, FORM_SOUND_VOLUME, FORM_SOUND_PITCH)
                }

                task.cancel()
                onArrival()
            }
        }
    }


    override fun onEntityDamagedByPlayer(player: Player, event: EntityDamageByEntityEvent) {
        val victim = event.entity as? LivingEntity ?: return
        val expiry = expiryOf(victim) ?: return

        if (expiry <= System.currentTimeMillis()) {
            victim.removePersistentKey(KEY)
            return
        }

        event.damage *= random.nextDouble(1.0, DAMAGE_MULTIPLIER)
        lightningStrike(victim, heldColor(player))
    }

    override fun onPlayerKnockbackEntity(player: Player, event: EntityKnockbackByEntityEvent) {
        val victim = event.entity as? LivingEntity ?: return
        if (!looming(victim)) return
        event.isCancelled = true
    }


    override fun onEntityDeath(player: Player, event: EntityDeathEvent) {
        val victim = event.entity
        if (!looming(victim)) return

        val drops = event.drops.toList()
        if (drops.isEmpty()) return
        event.drops.clear()

        val thrower = victim.killer?.uniqueId ?: player.uniqueId
        val cloud = victim.eyeLocation.add(0.0, CLOUD_HEIGHT, 0.0)
        val roll = Random.nextDouble()

        when {
            roll < BOUNTY_CHANCE -> {
                bountyBurst(cloud, victim.location.add(0.0, victim.height / 2.0, 0.0))
                val multiplier = random().nextDouble(BOUNTY_MIN_MULTIPLIER, BOUNTY_MAX_MULTIPLIER)
                rainLoot(victim.location, cloud.y, singles(drops, naturalLoot(victim), multiplier), thrower)
            }
            roll < BOUNTY_CHANCE + VOID_CHANCE -> voidBurst(cloud)
            else -> rainLoot(victim.location, cloud.y, singles(drops, emptyMap(), 1.0), thrower)
        }
    }

    private fun bountyBurst(cloud: Location, death: Location) {
        listOf(cloud, death).forEach { loc ->
            loc.world.spawnParticle(Particle.INSTANT_EFFECT, loc, 60, 0.7, 0.4, 0.7, 0.0, BOUNTY)
        }
        cloud.world.playSound(cloud, Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1f, 1.4f)
    }

    private fun voidBurst(cloud: Location) {
        Overcast.sour(cloud)

        var ticks = 0
        Executors.asyncTimer(0, 1) { task ->
            if (++ticks > VOID_TICKS) {
                task.cancel()
                return@asyncTimer
            }

            Overcast.puff(cloud, 20, true)
        }

        cloud.world.playSound(cloud, Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 0.6f, 1.6f)
    }

    override fun onItemMerge(player: Player, event: ItemMergeEvent) {
        val expiry = expiryOf(event.target) ?: return

        if (expiry > System.currentTimeMillis()) {
            event.isCancelled = true
        }
    }

    private fun naturalLoot(victim: LivingEntity): Map<Material, Int> {
        val lootable = victim as? Lootable ?: return emptyMap()
        val table = lootable.lootTable ?: return emptyMap()
        val context = LootContext.Builder(victim.location)
            .lootedEntity(victim)
            .killer(victim.killer)
            .build()

        val random = LOOT_RANDOM.get().apply { setSeed(lootable.seed) }

        return runCatching { table.populateLoot(random, context) }
            .getOrNull()
            ?.groupingBy { it.type }
            ?.fold(0) { total, stack -> total + stack.amount }
            ?: emptyMap()
    }


    private fun singles(drops: List<ItemStack>, natural: Map<Material, Int>, multiplier: Double): List<ItemStack> {
        val budget = natural.toMutableMap()

        return drops.flatMap { stack ->
            val allowance = (budget[stack.type] ?: 0).coerceAtMost(stack.amount)
            budget[stack.type] = (budget[stack.type] ?: 0) - allowance

            val bonus = scale(allowance, multiplier) - allowance
            List(stack.amount + bonus) { stack.asOne() }
        }
    }

    private fun scale(amount: Int, multiplier: Double): Int {
        val exact = amount * multiplier
        val whole = floor(exact).toInt()

        return whole + if (Random.nextDouble() < exact - whole) 1 else 0
    }

    private fun rainLoot(location: Location, cloudY: Double, items: List<ItemStack>, thrower: UUID) {
        val queue = ArrayDeque(items)

        location.syncTimer(0, DROP_STAGGER) { task ->
            val stack = queue.removeFirstOrNull() ?: run {
                task.cancel()
                return@syncTimer
            }

            val angle = Random.nextDouble(0.0, 2.0 * Math.PI)
            val dist = DROP_RADIUS * sqrt(Random.nextDouble())
            val spawn = Location(
                location.world,
                location.x + cos(angle) * dist,
                cloudY,
                location.z + sin(angle) * dist
            )

            val item = location.world.dropItem(spawn, stack)
            item.velocity = Vector(0.0, 0.0, 0.0)
            item.thrower = thrower

            item.setPersistentKey(KEY, PersistentDataType.LONG, System.currentTimeMillis() + NO_MERGE_TICKS * 50L)
            item.syncDelayed(NO_MERGE_TICKS) { item.removePersistentKey(KEY) }
        }
    }


    private fun loomOver(target: LivingEntity) {
        target.setPersistentKey(KEY, PersistentDataType.LONG, System.currentTimeMillis() + LOOM_TICKS * 50L)

        var ticks = 0

        target.syncTimer(0, 1) { task ->
            if (target.isDead || !target.isValid) {
                task.cancel()
                Overcast.drop(target.uniqueId)
                return@syncTimer
            }

            if (++ticks > LOOM_TICKS) {
                task.cancel()
                Overcast.drop(target.uniqueId)
                Util.removePersistentKey(target, KEY)
                return@syncTimer
            }

            val loc = target.location
            Overcast.publish(
                target.uniqueId,
                Node(target.uniqueId, loc.clone(), target.eyeLocation.y, System.currentTimeMillis())
            )
        }
    }

    private fun lightningStrike(target: LivingEntity, color: Color) {
        val groundLoc = target.location
        val cloudY = target.eyeLocation.y + CLOUD_HEIGHT
        val heightDifference = (cloudY - groundLoc.y).coerceAtLeast(0.0)


        Executors.async {
            val lightningLength = heightDifference * 0.042

            Particles.lightning(
                groundLoc,
                BukkitVectors.UP,
                20,
                200,
                0.5,
                2.0,
                1.0,
                lightningLength,
                1.0,
                0.1,
                0.8,
                DUST.clone().withColor(color)
            )
        }

        groundLoc.world.playSound(groundLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, THUNDER_VOLUME, THUNDER_PITCH)
    }

    private data class Node(
        val id: UUID,
        val location: Location,
        val eyeY: Double,
        val stamp: Long
    )

    private object Overcast {

        private const val MERGE_DISTANCE = 7.0
        private const val MERGE_DISTANCE_SQ = MERGE_DISTANCE * MERGE_DISTANCE
        private const val MERGE_HEIGHT = 7.0
        private const val STALE_MS = 250L

        private val nodes = ConcurrentHashMap<UUID, Node>()
        private val soured = ConcurrentHashMap<UUID, Long>()
        private val lock = Any()
        private var task: ScheduledTask? = null
        private var ticks = 0

        fun publish(id: UUID, node: Node) {
            nodes[id] = node
            ensureRunning()
        }

        fun drop(id: UUID) {
            nodes.remove(id)
            soured.remove(id)
        }

        fun sour(origin: Location) {
            val cutoff = System.currentTimeMillis() - STALE_MS
            val expiry = System.currentTimeMillis() + VOID_TICKS * 50L
            val pool = nodes.values
                .filter { it.stamp >= cutoff && it.location.world == origin.world }
                .toMutableList()

            val frontier = ArrayDeque(listOf(origin))
            while (frontier.isNotEmpty()) {
                val at = frontier.removeFirst()
                val reached = pool.filter { linked(at, it.location) }

                pool.removeAll(reached)
                reached.forEach {
                    soured[it.id] = expiry
                    frontier.add(it.location)
                }
            }
        }

        private fun linked(a: Location, b: Location): Boolean {
            val dx = a.x - b.x
            val dz = a.z - b.z

            return dx * dx + dz * dz <= MERGE_DISTANCE_SQ && abs(a.y - b.y) <= MERGE_HEIGHT
        }

        private fun ensureRunning() {
            synchronized(lock) {
                if (task != null) return
                task = Executors.asyncTimer(0, 1) { t ->
                    if (nodes.isEmpty()) {
                        synchronized(lock) { task = null }
                        t.cancel()
                        return@asyncTimer
                    }
                    draw()
                }
            }
        }

        private fun draw() {
            val cutoff = System.currentTimeMillis() - STALE_MS
            val raining = ++ticks % DROP_INTERVAL == 0
            val windy = ticks % WIND_SOUND_INTERVAL == 0

            nodes.values
                .filter { it.stamp >= cutoff }
                .groupBy { it.location.world }
                .forEach { (_, worldNodes) -> drawWorld(worldNodes, raining, windy) }
        }

        private fun drawWorld(worldNodes: List<Node>, raining: Boolean, windy: Boolean) {
            val n = worldNodes.size
            val parent = IntArray(n) { it }

            fun find(i: Int): Int {
                var root = i
                while (parent[root] != root) root = parent[root]
                var walk = i
                while (parent[walk] != root) {
                    val next = parent[walk]
                    parent[walk] = root
                    walk = next
                }
                return root
            }

            val bridges = mutableListOf<Pair<Int, Int>>()
            for (i in 0 until n) {
                for (j in i + 1 until n) {
                    if (!linked(worldNodes[i].location, worldNodes[j].location)) continue

                    bridges.add(i to j)
                    val rootA = find(i)
                    val rootB = find(j)
                    if (rootA != rootB) parent[rootA] = rootB
                }
            }

            val clusters = worldNodes.indices.groupBy { find(it) }
            val cloudY = clusters.mapValues { (_, members) ->
                members.maxOf { worldNodes[it].eyeY } + CLOUD_HEIGHT
            }

            val now = System.currentTimeMillis()
            val smoking = clusters.mapValues { (_, members) ->
                members.any { (soured[worldNodes[it].id] ?: 0L) > now }
            }

            clusters.forEach { (root, members) ->
                val ceilingY = cloudY.getValue(root)
                val smoke = smoking.getValue(root)

                members.forEach { i ->
                    val node = worldNodes[i]
                    val ceiling = node.location.clone().apply { y = ceilingY }

                    puff(ceiling, 20, smoke)
                    if (raining && !smoke) fallDrop(ceiling, node.location.y)
                }

                // Once per cloud rather than per member, so a merged front is not N times as loud.
                // Wind still carries over a soured cloud, so this one ignores the smoke.
                if (windy) {
                    val center = centerOf(worldNodes, members, ceilingY)
                    center.sync {
                        center.world.playSound(
                            center,
                            Sound.BLOCK_DRY_GRASS_AMBIENT,
                            WIND_SOUND_VOLUME,
                            Random.nextDouble(0.6, 0.9).toFloat()
                        )
                    }
                }
            }

            bridges.forEach { (i, j) ->
                val a = worldNodes[i].location
                val b = worldNodes[j].location
                val root = find(i)
                val mid = Location(a.world, (a.x + b.x) / 2.0, cloudY.getValue(root), (a.z + b.z) / 2.0)

                puff(mid, 10, smoking.getValue(root))
            }
        }


        /** Middle of a cluster's footprint, at its shared ceiling. */
        private fun centerOf(worldNodes: List<Node>, members: List<Int>, ceilingY: Double): Location {
            val locations = members.map { worldNodes[it].location }

            return Location(
                locations.first().world,
                locations.sumOf { it.x } / locations.size,
                ceilingY,
                locations.sumOf { it.z } / locations.size
            )
        }

        fun puff(loc: Location, count: Int, smoke: Boolean) {
            val particle = if (smoke) Particle.LARGE_SMOKE else Particle.CLOUD
            loc.world.spawnParticle(particle, loc, count, 0.6, 0.1, 0.6, 0.0)
        }

        private fun fallDrop(cloud: Location, groundY: Double) {
            val angle = Random.nextDouble(0.0, 2.0 * Math.PI)
            val dist = DROP_RADIUS * sqrt(Random.nextDouble())
            val pos = cloud.clone().add(cos(angle) * dist, 1.0, sin(angle) * dist)

            Executors.asyncTimer(0, 1) { task ->
                pos.y -= FALL_SPEED

                if (pos.y <= groundY) {
                    task.cancel()

                    if (Random.nextDouble() < DRIP_SOUND_CHANCE) {
                        val landing = pos.clone()
                        landing.sync {
                            landing.world.playSound(
                                landing,
                                Sound.BLOCK_POINTED_DRIPSTONE_DRIP_WATER,
                                DRIP_SOUND_VOLUME,
                                Random.nextDouble(0.8, 1.3).toFloat()
                            )
                        }
                    }

                    return@asyncTimer
                }

                RAIN.spawn(pos)
            }
        }
    }
}
