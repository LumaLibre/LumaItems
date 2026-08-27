package dev.lumas.lumaitems.items.armor.helmet

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.AbilityUtil
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.SharedContainers
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.addCooldown
import dev.lumas.lumaitems.util.extensions.isBoundingBoxOnGround
import dev.lumas.lumaitems.util.extensions.isItemInSlot
import dev.lumas.lumaitems.util.extensions.isOnCooldown
import dev.lumas.lumaitems.util.extensions.isTagged
import dev.lumas.lumaitems.util.extensions.mix
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.syncDelayed
import dev.lumas.lumaitems.util.extensions.syncTimer
import dev.lumas.lumaitems.util.extensions.toBukkitColor
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.Tag
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

class SunbrellaHatNouveauItem : CustomItemFunctions() {

    private companion object {
        private const val KEY = "sunbrella-nouveau"
        private val KEY_NS = KEY.namespacedKey()
        
        private const val CRUISE_SPEED = 0.55
        private const val MIN_SPEED = 0.12
        private const val MAX_SPEED = 2.10
        private const val DIVE_ACCEL = 0.115
        private const val DRAG = 0.07
        private const val CLIMB_BLEED = 0.035
        private const val SINK_RATE = 0.055
        private const val DIVE_SINK = 0.72
        private const val FLARE_LIFT = 1.15
        private const val MAX_CLIMB = 0.38
        private const val TURN_RESPONSE = 0.17
        private const val VERTICAL_RESPONSE = 0.34
        private const val OPEN_CLEARANCE = 0.6
        private const val ENTRY_CARRY = 0.55


        private const val CANOPY_POINTS = 12
        private const val CANOPY_HEIGHT = 2.75
        private const val CANOPY_RADIUS = 0.9
        private const val CANOPY_DEPTH = 0.52
        private const val HARNESS_HEIGHT = 1.45
        private const val CORD_POINTS = 3

        private const val SLIPSTREAM_BPS = 15.5
        private const val SLIPSTREAM_FULL_BPS = 18.0
        private const val WHIRL_PERIOD = 5

        private const val SLASH_COOLDOWN = 10L
        private const val SLASH_STEP = 0.55
        private const val SLASH_SUBSTEPS = 2
        private const val SLASH_RANGE = 17.0
        private const val SLASH_RADIUS = 1.7
        private const val SLASH_DAMAGE_RATIO = 0.34
        private const val SLASH_MIN_DAMAGE = 3.0
        private const val SLASH_SEGMENTS = 9
        private const val SLASH_ARC = 1.25

        private val VOLLEY = arrayOf(
            Triple(-14.0, 38.0, 0L),
            Triple(0.0, -6.0, 2L),
            Triple(14.0, -42.0, 4L)
        )

        private val LEAF = "#A0F562".toBukkitColor()
        private val BLOSSOM = "#F893AC".toBukkitColor()
        private val SKY = "#AACDF5".toBukkitColor()
        private val VIOLET = "#AC9EEE".toBukkitColor()
        private val PALETTE = arrayOf(LEAF, BLOSSOM, SKY, VIOLET)
        private val GRADIENT: Array<Color> = Array(24) { index ->
            val point = index / 23.0 * (PALETTE.size - 1)
            val low = floor(point).toInt().coerceAtMost(PALETTE.size - 2)
            PALETTE[low].mix(PALETTE[low + 1], (point - low).toFloat())
        }

        private fun gradient(ratio: Double): Color {
            return GRADIENT[(ratio.coerceIn(0.0, 1.0) * (GRADIENT.size - 1)).roundToInt()]
        }
    }
    
    private class Flight {
        var airspeed: Double = CRUISE_SPEED
        var ticks: Int = 0
        var task: ScheduledTask? = null
    }

    private val flights = ConcurrentHashMap<UUID, Flight>()
    private val lastSlipstream = ConcurrentHashMap<UUID, Int>()
    private val dealingCrescentDamage = ConcurrentHashMap.newKeySet<UUID>()


    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#A0F562:#F893AC:#AACDF5:#AC9EEE>Sunbrella Nouveau</gradient></b>")
            .customEnchants("<#AC9EEE>Crescent Gale", "<#F893AC>Slipstream", "<#AACDF5>Drift")
            .material(Material.NETHERITE_HELMET)
            .persistentData(KEY)
            .tier(Tier.LUMARINE_2026)
            .lore(
                "<#AC9EEE>While worn</#AC9EEE>, full sword",
                "attacks will throw <#AC9EEE>3</#AC9EEE> wind",
                "crescents that can cut",
                "any entity in their way.",
                "",
                "<#F893AC>While worn</#F893AC>, this hat will",
                "reduce the wearer's air",
                "drag, allowing them to",
                "move faster in the air.",
                "",
                "<#AACDF5>Sneak</#AACDF5> while falling unzips",
                "the canopy. Dive to build",
                "speed, flare to spend it",
                "on lift. Fall damage is",
                "negated in flight.",
            )
            .vanillaEnchants(
                Enchantment.PROTECTION to 5,
                Enchantment.FEATHER_FALLING to 6,
                Enchantment.PROJECTILE_PROTECTION to 4,
                Enchantment.UNBREAKING to 4,
                Enchantment.MENDING to 1
            )
            .attributeModifiers(
                SharedContainers.AIR_DRAG_MODIFIER
                    .setOperation(AttributeModifier.Operation.ADD_NUMBER)
                    .setAmount(-0.5)
                    .setSlot(EquipmentSlotGroup.HEAD)
                    .build(),
            )
            .buildPair()
    }


    override fun onMove(player: Player, event: PlayerMoveEvent) {
        if (player.isItemInSlot(KEY_NS, EquipmentSlot.HEAD)) {
            trySlipstream(player, event.to.toVector().subtract(event.from.toVector()))
        }

        if (player.isBoundingBoxOnGround()) {
            flights.remove(player.uniqueId)?.task?.cancel()
            return
        }

        if (flights[player.uniqueId]?.task != null || !canOpenCanopy(player)) {
            return
        }
        openCanopy(player)
    }

    override fun onPlayerQuit(player: Player, event: PlayerQuitEvent) {
        flights.remove(player.uniqueId)?.task?.cancel()
        lastSlipstream.remove(player.uniqueId)
    }

    private fun canOpenCanopy(player: Player): Boolean {
        return player.isSneaking
                && player.velocity.y < -0.08
                && !player.isFlying
                && !player.isGliding
                && !player.isInsideVehicle
                && !player.isInWater
                && !player.isBoundingBoxOnGround(OPEN_CLEARANCE)
                && player.isItemInSlot(KEY_NS, EquipmentSlot.HEAD)
    }

    private fun stillFlying(player: Player): Boolean {
        return player.isValid
                && player.isSneaking
                && !player.isFlying
                && !player.isGliding
                && !player.isInsideVehicle
                && !player.isInWater
                && !player.isBoundingBoxOnGround()
                && player.isItemInSlot(KEY_NS, EquipmentSlot.HEAD)
    }

    private fun openCanopy(player: Player) {
        val flight = flights.computeIfAbsent(player.uniqueId) { Flight() }
        val entry = player.velocity
        val carried = Vector(entry.x, 0.0, entry.z).length() + (-entry.y).coerceAtLeast(0.0) * ENTRY_CARRY
        flight.airspeed = carried.coerceIn(MIN_SPEED, MAX_SPEED)
        flight.ticks = 0

        val location = player.location
        player.world.playSound(location, Sound.ENTITY_WIND_CHARGE_THROW, 0.5f, 1.5f)
        player.playSound(location, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.35f, 1.7f)
        player.world.spawnParticle(
            Particle.DUST_COLOR_TRANSITION, location.clone().add(0.0, CANOPY_HEIGHT - CANOPY_DEPTH * 0.35, 0.0), 24, 0.55, 0.15, 0.55, 0.0,
            Particle.DustTransition(LEAF, SKY, 1.2f)
        )

        flight.task = player.syncTimer(1, 1) { task ->
            if (!stillFlying(player)) {
                closeCanopy(player, flight)
                task.cancel()
                return@syncTimer
            }
            flight.ticks++
            fly(player, flight)
            drawCanopy(player, flight)
        }
    }

    private fun closeCanopy(player: Player, flight: Flight) {
        flight.task = null
        if (!player.isValid) return

        player.playSound(player.location, Sound.ENTITY_BREEZE_DEFLECT, 0.35f, 1.6f)
        player.world.spawnParticle(
            Particle.DUST_COLOR_TRANSITION, player.location.clone().add(0.0, CANOPY_HEIGHT - CANOPY_DEPTH * 0.35, 0.0), 10, 0.3, 0.2, 0.3, 0.0,
            Particle.DustTransition(SKY, VIOLET, 0.9f)
        )
    }

    private fun fly(player: Player, flight: Flight) {
        val look = player.location.direction
        val dive = -look.y

        flight.airspeed += dive * DIVE_ACCEL
        flight.airspeed -= (flight.airspeed - CRUISE_SPEED) * DRAG
        if (dive < 0) flight.airspeed += dive * CLIMB_BLEED
        flight.airspeed = flight.airspeed.coerceIn(MIN_SPEED, MAX_SPEED)

        val heading = Vector(look.x, 0.0, look.z)
        if (heading.lengthSquared() < 1.0E-4) {
            val current = player.velocity.clone().setY(0.0)
            heading.copy(if (current.lengthSquared() < 1.0E-4) BukkitVectors.EAST else current)
        }
        heading.normalize()

        val usable = flight.airspeed - MIN_SPEED
        val targetY = if (dive >= 0) {
            -(SINK_RATE + dive * flight.airspeed * DIVE_SINK)
        } else {
            (-SINK_RATE + -dive * usable * FLARE_LIFT).coerceAtMost(MAX_CLIMB)
        }

        val velocity = player.velocity
        velocity.x += (heading.x * flight.airspeed - velocity.x) * TURN_RESPONSE
        velocity.z += (heading.z * flight.airspeed - velocity.z) * TURN_RESPONSE
        velocity.y += (targetY - velocity.y) * VERTICAL_RESPONSE
        player.velocity = velocity
        player.fallDistance = 0f
    }

    private fun drawCanopy(player: Player, flight: Flight) {
        val world = player.world
        val apex = player.location.clone().add(0.0, CANOPY_HEIGHT, 0.0)
        val speedRatio = ((flight.airspeed - MIN_SPEED) / (MAX_SPEED - MIN_SPEED)).coerceIn(0.0, 1.0)
        val radius = CANOPY_RADIUS + speedRatio * 0.14
        val sway = sin(flight.ticks * 0.09) * 0.035

        world.spawnParticle(
            Particle.DUST_COLOR_TRANSITION, apex, 1, 0.0, 0.0, 0.0, 0.0,
            Particle.DustTransition(LEAF, SKY, 0.95f)
        )
        for (index in 0 until CANOPY_POINTS) {
            val angle = index / CANOPY_POINTS.toDouble() * Math.PI * 2
            val color = gradient(index / CANOPY_POINTS.toDouble())
            val rim = apex.clone().add(
                cos(angle) * radius,
                -CANOPY_DEPTH + sin(angle * 4 + flight.ticks * 0.08) * 0.045 + sway,
                sin(angle) * radius
            )
            val dome = apex.clone().add(
                cos(angle) * radius * 0.58,
                -CANOPY_DEPTH * 0.24 + sway,
                sin(angle) * radius * 0.58
            )

            world.spawnParticle(
                Particle.DUST_COLOR_TRANSITION, dome, 1, 0.0, 0.0, 0.0, 0.0,
                Particle.DustTransition(color, SKY, 0.8f)
            )
            world.spawnParticle(
                Particle.DUST_COLOR_TRANSITION, rim, 1, 0.0, 0.0, 0.0, 0.0,
                Particle.DustTransition(color, VIOLET, 0.9f)
            )

            if (index % (CANOPY_POINTS / 4) == 0) {
                val harness = player.location.clone().add(
                    cos(angle) * 0.18,
                    HARNESS_HEIGHT,
                    sin(angle) * 0.18
                )
                for (cordPoint in 1..CORD_POINTS) {
                    val progress = cordPoint / (CORD_POINTS + 1.0)
                    val cord = rim.clone().add(
                        (harness.x - rim.x) * progress,
                        (harness.y - rim.y) * progress,
                        (harness.z - rim.z) * progress
                    )
                    world.spawnParticle(
                        Particle.DUST_COLOR_TRANSITION, cord, 1, 0.0, 0.0, 0.0, 0.0,
                        Particle.DustTransition(SKY, Color.WHITE, 0.55f)
                    )
                }
            }
        }

        val velocity = player.velocity
        val heading = velocity.clone().setY(0.0)
        if (heading.lengthSquared() > 1.0E-4) {
            heading.normalize()
            val side = heading.getCrossProduct(BukkitVectors.UP).normalize().multiply(radius)
            val trail = heading.clone().multiply(-0.45)
            for (sign in intArrayOf(-1, 1)) {
                val tip = apex.clone()
                    .add(side.clone().multiply(sign.toDouble()))
                    .add(trail)
                    .add(0.0, -CANOPY_DEPTH + sin(flight.ticks * 0.15 + sign) * 0.1, 0.0)
                world.spawnParticle(
                    Particle.DUST_COLOR_TRANSITION, tip, 1, 0.05, 0.05, 0.05, 0.0,
                    Particle.DustTransition(SKY, LEAF, 0.7f)
                )
            }
        }

        trySlipstream(player, velocity)

        if (flight.ticks % 6 == 0) {
            world.spawnParticle(Particle.SMALL_GUST, player.location.clone().subtract(0.0, 0.4, 0.0), 1, 0.2, 0.0, 0.2, 0.0)
        }
    }

    private fun trySlipstream(player: Player, travelled: Vector) {
        val velocity = player.velocity
        // Player velocity reads stale under creative flight, and the move delta is short when
        // several packets land in one tick - whichever reads faster is the honest one.
        val motion = if (velocity.lengthSquared() >= travelled.lengthSquared()) velocity else travelled
        val blocksPerSecond = motion.length() * 20.0
        if (blocksPerSecond < SLIPSTREAM_BPS) return

        val tick = player.ticksLived
        if (lastSlipstream.put(player.uniqueId, tick) == tick) return
        drawSlipstream(player, motion, blocksPerSecond, tick)
    }

    private fun drawSlipstream(player: Player, motion: Vector, blocksPerSecond: Double, tick: Int) {
        val intensity = ((blocksPerSecond - SLIPSTREAM_BPS) / (SLIPSTREAM_FULL_BPS - SLIPSTREAM_BPS)).coerceIn(0.0, 1.0)
        val world = player.world
        val wake = motion.clone().normalize().multiply(-0.65)
        val crown = player.eyeLocation.add(0.0, 0.3, 0.0)
        val spread = 0.26 + intensity * 0.24

        for (index in 0 until 2 + (intensity * 4).toInt()) {
            val streak = crown.clone()
                .add(wake)
                .add(
                    random.nextDouble(-spread, spread),
                    random.nextDouble(-spread * 1.7, spread * 0.5),
                    random.nextDouble(-spread, spread)
                )
            world.spawnParticle(
                Particle.DUST_COLOR_TRANSITION, streak, 1, 0.0, 0.0, 0.0, 0.0,
                Particle.DustTransition(gradient(random.nextDouble()), SKY, (0.55 + intensity * 0.5).toFloat())
            )
        }

        world.spawnParticle(Particle.CLOUD, crown.clone().add(wake), 1, 0.12, 0.12, 0.12, 0.0)
        if (intensity > 0.45) {
            world.spawnParticle(Particle.SMALL_GUST, crown.clone().add(wake.clone().multiply(1.6)), 1, 0.1, 0.1, 0.1, 0.0)
        }

        if (tick % WHIRL_PERIOD == 0) {
            world.playSound(
                player.location, Sound.ENTITY_BREEZE_WHIRL,
                (0.14 + intensity * 0.22).toFloat(),
                (0.85 + intensity * 0.55).toFloat()
            )
        }
    }

    override fun onLeftClick(player: Player, event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        tryLaunchCrescentVolley(player)
    }

    override fun onEntityDamage(player: Player, event: EntityDamageByEntityEvent) {
        // Projectile hits also arrive here with their shooter as player. Only a direct melee
        // attack may launch a volley, and damage dealt by a crescent must not recurse.
        if (event.damager != player || player.uniqueId in dealingCrescentDamage) return
        tryLaunchCrescentVolley(player)
    }

    private fun tryLaunchCrescentVolley(player: Player) {
        val sword = player.inventory.itemInMainHand
        if (!sword.type.isTagged(Tag.ITEMS_SWORDS)) return
        if (player.attackCooldown < 0.9f) return
        if (player.isOnCooldown(this) || !player.isItemInSlot(KEY_NS, EquipmentSlot.HEAD)) return

        player.addCooldown(this, SLASH_COOLDOWN)

        val attack = player.getAttribute(Attribute.ATTACK_DAMAGE)?.value ?: 1.0
        val sharpness = sword.getEnchantmentLevel(Enchantment.SHARPNESS).let { if (it > 0) 1.0 + 0.5 * (it - 1) else 0.0 }
        val damage = ((attack + sharpness) * SLASH_DAMAGE_RATIO).coerceAtLeast(SLASH_MIN_DAMAGE)

        for ((yaw, roll, delay) in VOLLEY) {
            if (delay <= 0) {
                launchCrescent(player, yaw, roll, damage)
            } else {
                player.syncDelayed(delay) { if (player.isValid) launchCrescent(player, yaw, roll, damage) }
            }
        }

        sword.damage(1, player)
        player.world.playSound(player.location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.9f, 1.4f)
        player.world.playSound(player.location, Sound.ENTITY_BREEZE_SHOOT, 0.7f, 1.2f)
    }

    private fun launchCrescent(player: Player, yawOffset: Double, rollDegrees: Double, damage: Double) {
        val world = player.world
        val forward = player.eyeLocation.direction.rotateAroundY(Math.toRadians(yawOffset)).normalize()
        val position = player.eyeLocation.add(forward.clone().multiply(0.8))

        val right = forward.getCrossProduct(BukkitVectors.UP).let {
            if (it.lengthSquared() < 1.0E-6) BukkitVectors.EAST else it.normalize()
        }
        val up = right.getCrossProduct(forward).normalize()

        val roll = Math.toRadians(rollDegrees)
        val bladeRight = right.clone().multiply(cos(roll)).add(up.clone().multiply(sin(roll)))
        val bladeUp = up.clone().multiply(cos(roll)).subtract(right.clone().multiply(sin(roll)))

        val struck = HashSet<UUID>()
        var travelled = 0.0

        player.syncTimer(0, 1) { task ->
            if (!player.isValid || travelled >= SLASH_RANGE) {
                task.cancel()
                return@syncTimer
            }

            var blocked = false
            for (substep in 0 until SLASH_SUBSTEPS) {
                position.add(forward.x * SLASH_STEP, forward.y * SLASH_STEP, forward.z * SLASH_STEP)
                travelled += SLASH_STEP

                if (!position.block.isPassable) {
                    blocked = true
                    break
                }

                drawCrescent(position, forward, bladeRight, bladeUp, travelled / SLASH_RANGE)
                cut(player, position, struck, damage)
                if (travelled >= SLASH_RANGE) break
            }

            if (blocked) {
                world.spawnParticle(Particle.GUST_EMITTER_SMALL, position, 1, 0.0, 0.0, 0.0, 0.0)
                world.playSound(position, Sound.ENTITY_BREEZE_DEFLECT, 0.5f, 1.5f)
                task.cancel()
            }
        }
    }

    private fun drawCrescent(center: Location, forward: Vector, bladeRight: Vector, bladeUp: Vector, life: Double) {
        val world = center.world
        val radius = SLASH_RADIUS * (0.75 + life * 0.45)
        val size = (1.05 - life * 0.45).toFloat()
        val edge = cos(SLASH_ARC)

        for (index in 0..SLASH_SEGMENTS) {
            val ratio = index / SLASH_SEGMENTS.toDouble()
            val angle = (-1.0 + 2.0 * ratio) * SLASH_ARC
            val bulge = cos(angle) - edge

            val point = center.clone()
                .add(bladeRight.x * sin(angle) * radius, bladeRight.y * sin(angle) * radius, bladeRight.z * sin(angle) * radius)
                .add(bladeUp.x * bulge * radius * 0.35, bladeUp.y * bulge * radius * 0.35, bladeUp.z * bulge * radius * 0.35)
                .add(forward.x * bulge * radius * 0.5, forward.y * bulge * radius * 0.5, forward.z * bulge * radius * 0.5)

            world.spawnParticle(
                Particle.DUST_COLOR_TRANSITION, point, 1, 0.0, 0.0, 0.0, 0.0,
                Particle.DustTransition(gradient(ratio), gradient(1.0 - ratio), size)
            )
        }

        world.spawnParticle(Particle.TRAIL, center, 1, 0.0, 0.0, 0.0, 0.0,
            Particle.Trail(center.clone().add(forward.clone().multiply(1.6)), SKY, 6))
    }

    private fun cut(player: Player, center: Location, struck: MutableSet<UUID>, damage: Double) {
        val victims = center.getNearbyLivingEntities(SLASH_RADIUS, SLASH_RADIUS * 0.85, SLASH_RADIUS)
        for (victim in victims) {
            if (victim.uniqueId == player.uniqueId || victim.isDead) continue
            if (!struck.add(victim.uniqueId)) continue
            if (AbilityUtil.noDamagePermission(player, victim)) continue

            victim.noDamageTicks = 0
            dealingCrescentDamage.add(player.uniqueId)
            try {
                victim.damage(damage, player)
            } finally {
                dealingCrescentDamage.remove(player.uniqueId)
            }

            val impact = victim.boundingBox.center.toLocation(victim.world)
            victim.world.spawnParticle(Particle.SWEEP_ATTACK, impact, 1, 0.3, 0.3, 0.3, 0.0)
            victim.world.spawnParticle(
                Particle.DUST_COLOR_TRANSITION, impact, 12, 0.35, 0.45, 0.35, 0.0,
                Particle.DustTransition(BLOSSOM, SKY, 1.1f)
            )
            victim.world.playSound(impact, Sound.ENTITY_PLAYER_ATTACK_STRONG, 0.7f, 1.35f)
        }
    }
}
