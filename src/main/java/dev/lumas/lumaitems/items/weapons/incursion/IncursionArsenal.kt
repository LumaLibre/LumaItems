package dev.lumas.lumaitems.items.weapons.incursion

import dev.lumas.core.util.Text
import dev.lumas.lumaitems.items.weapons.incursion.IncursionArsenal.BEAM_STEP
import dev.lumas.lumaitems.util.extensions.canDamage
import dev.lumas.lumaitems.util.extensions.sync
import java.time.Duration

import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

import net.kyori.adventure.title.Title
import net.minecraft.world.entity.TamableAnimal
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.attribute.Attribute
import org.bukkit.block.Block
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Tameable
import org.bukkit.inventory.MainHand
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import java.text.DecimalFormat

internal object IncursionArsenal {

    const val BEAM_STEP = 0.5

    private const val HEAD_HALF_OF_HEIGHT = 0.18
    private const val HEAD_HALF_OF_WIDTH = 0.35
    private const val MIN_HEAD_HALF = 0.15
    private const val MAX_HEAD_HALF = 0.5

    private const val HAND_SIDE_OFFSET = 0.4
    private const val HAND_FORWARD_OFFSET = 0.4
    private const val HAND_DROP_OFFSET = 1.0

    private val BODY_SAMPLE_FRACTIONS = doubleArrayOf(0.2, 0.5, 0.8)

    private val HEADSHOT_TITLE_TIMES: Title.Times =
        Title.Times.times(Duration.ZERO, Duration.ofMillis(600), Duration.ofMillis(200))

    class Target(
        val entity: LivingEntity,
        val hitbox: BoundingBox,
        val eyeHeight: Double,
        val facing: Vector
    ) {

        fun expandedHitbox(radius: Double): BoundingBox = hitbox.clone().expand(radius)

        fun headHitbox(radius: Double): BoundingBox {
            val half = min(
                hitbox.height * HEAD_HALF_OF_HEIGHT,
                min(hitbox.widthX, hitbox.widthZ) * HEAD_HALF_OF_WIDTH
            ).coerceIn(MIN_HEAD_HALF, MAX_HEAD_HALF)

            val reach = max(0.0, (max(hitbox.widthX, hitbox.widthZ) / 2.0) - half)
            val centreX = hitbox.centerX + (facing.x * reach)
            val centreZ = hitbox.centerZ + (facing.z * reach)
            val eyeY = hitbox.minY + eyeHeight

            return BoundingBox(
                centreX - half, eyeY - half, centreZ - half,
                centreX + half, eyeY + half, centreZ + half
            ).expand(radius)
        }

        fun containsWithin(radius: Double, x: Double, y: Double, z: Double): Boolean =
            x >= hitbox.minX - radius && x < hitbox.maxX + radius &&
                y >= hitbox.minY - radius && y < hitbox.maxY + radius &&
                z >= hitbox.minZ - radius && z < hitbox.maxZ + radius
    }

    // Must be called from the region owning [around], which is where the hitboxes are read
    fun targetsAround(shooter: Player, around: Location, radius: Double): List<Target> {
        if (!Bukkit.isOwnedByCurrentRegion(around)) return emptyList()

        return around.world.getNearbyLivingEntities(around, radius)
            .mapNotNull { snapshot(shooter, it) }
    }

    // For the beam, which is far too long to gather around a single point
    fun targetsInChunk(shooter: Player, world: World, chunkX: Int, chunkZ: Int): List<Target> {
        if (!Bukkit.isOwnedByCurrentRegion(world, chunkX, chunkZ)) return emptyList()

        return world.getChunkAt(chunkX, chunkZ).entities
            .mapNotNull { snapshot(shooter, it as? LivingEntity ?: return@mapNotNull null) }
    }

    private fun snapshot(shooter: Player, entity: LivingEntity): Target? {
        if (entity.uniqueId == shooter.uniqueId || !entity.isValid || entity.isDead) return null
        if (!shooter.canDamage(entity)) return null
        val yaw = Math.toRadians(entity.location.yaw.toDouble())
        val facing = Vector(-sin(yaw), 0.0, cos(yaw))
        return Target(entity, entity.boundingBox, entity.eyeHeight, facing)
    }

    // No true damage here, so protection plugins can do their thing
    fun hurt(target: LivingEntity, shooter: Player, damage: Double, beforeDamage: ((LivingEntity) -> Unit)? = null) {
        if (damage <= 0 && beforeDamage == null) return
        if (target is Tameable && target.isTamed) return
        if (target !is Player && target.customName() != null) return

        val scaledDamage = if (isBossMob(target)) damage * 0.5 else damage

        target.sync {
            if (!target.isValid || target.isDead) return@sync

            beforeDamage?.invoke(target)
            if (scaledDamage > 0) target.damage(scaledDamage, shooter)
        }
    }

    private fun isBossMob(entity: LivingEntity): Boolean =
        entity.type == EntityType.ELDER_GUARDIAN ||
        entity.type == EntityType.ENDER_DRAGON ||
        entity.type == EntityType.WITHER ||
        entity.type == EntityType.WARDEN

    fun forced(
        world: World,
        particle: Particle,
        at: Location,
        count: Int,
        spread: Double,
        extra: Double,
        data: Any? = null
    ) {
        world.spawnParticle(particle, at, count, spread, spread, spread, extra, data, true)
    }

    fun forced(
        world: World,
        particle: Particle,
        x: Double,
        y: Double,
        z: Double,
        count: Int,
        spread: Double,
        extra: Double,
        data: Any? = null
    ) {
        world.spawnParticle(particle, x, y, z, count, spread, spread, spread, extra, data, true)
    }

    fun hitFeedback(shooter: Player) {
        shooter.playSound(shooter.location, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1.4f)
    }

    fun headshotFeedback(shooter: Player, headshots: List<Double>) {
        shooter.playSound(shooter.location, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1.4f)
        shooter.playSound(shooter.location, Sound.ITEM_TRIDENT_RETURN, 0.6f, 1.8f)

        val format = DecimalFormat("0.#")
        val subtitle = headshots.sorted().joinToString(", ") { distance ->
            format.format(distance) + "m"
        }
        shooter.showTitle(
            Title.title(Text.mm("<red>HEADSHOT"), Text.mm("<gray>$subtitle"), HEADSHOT_TITLE_TIMES)
        )
    }

    fun mainHandLocation(player: Player): Location {
        val eye = player.eyeLocation
        val yaw = Math.toRadians(eye.yaw.toDouble())
        val right = Vector(-Math.cos(yaw), 0.0, -Math.sin(yaw))
        if (player.mainHand == MainHand.LEFT) right.multiply(-1)

        val scale = player.getAttribute(Attribute.SCALE)?.value ?: 1.0

        return eye.clone()
            .add(right.multiply(HAND_SIDE_OFFSET * scale))
            .add(eye.direction.multiply(HAND_FORWARD_OFFSET * scale))
            .subtract(Vector(0.0, HAND_DROP_OFFSET * scale, 0.0))
    }

    fun beamTouches(box: BoundingBox, eye: Location, direction: Vector, maxDistance: Double): Boolean {
        val origin = eye.toVector()
        return box.contains(origin) || box.rayTrace(origin, direction, maxDistance) != null
    }

    fun coneHitDistance(
        apex: Vector,
        axis: Vector,
        range: Double,
        tanHalfAngle: Double,
        cosHalfAngle: Double,
        hitbox: BoundingBox
    ): Double {
        val height = hitbox.height
        val radius = max(max(hitbox.widthX, hitbox.widthZ) / 2.0, height / 6.0)

        val maxReach = range + (height / 2.0) + radius
        if (apex.distanceSquared(hitbox.center) > maxReach * maxReach) return -1.0

        var best = -1.0
        for (fraction in BODY_SAMPLE_FRACTIONS) {
            val dx = hitbox.centerX - apex.x
            val dy = (hitbox.minY + (height * fraction)) - apex.y
            val dz = hitbox.centerZ - apex.z

            val distance = Math.sqrt((dx * dx) + (dy * dy) + (dz * dz))
            if (distance <= radius) return 0.0

            val along = (dx * axis.x) + (dy * axis.y) + (dz * axis.z)
            if (along < 0 || along - radius > range) continue

            val offX = dx - (axis.x * along)
            val offY = dy - (axis.y * along)
            val offZ = dz - (axis.z * along)

            val fromAxis = Math.sqrt((offX * offX) + (offY * offY) + (offZ * offZ))
            if (fromAxis > (along * tanHalfAngle) + (radius / cosHalfAngle)) continue

            val toSurface = max(0.0, distance - radius)
            if (toSurface <= range && (best < 0 || toSurface < best)) best = toSurface
        }
        return best
    }

    // How far along the ray it first meets something solid inside this block, or -1 if it doesn't
    fun solidHitDistance(block: Block, origin: Vector, direction: Vector, maxDistance: Double): Double {
        if (!couldStopShots(block)) return -1.0

        val bounds = block.boundingBox
        if (bounds.volume <= 0) return -1.0
        if (bounds.contains(origin)) return 0.0 // Fired from inside the block itself

        // <direction> is normalised, so this is the distance along the ray
        val hit = bounds.rayTrace(origin, direction, maxDistance) ?: return -1.0
        return hit.hitPosition.distance(origin)
    }

    fun couldStopShots(block: Block): Boolean =
        !block.isPassable && block.type != Material.BARRIER

    fun hasClearShot(from: Location, direction: Vector, distance: Double): Boolean {
        val world = from.world
        val origin = from.toVector()
        var lastX = Int.MIN_VALUE
        var lastY = Int.MIN_VALUE
        var lastZ = Int.MIN_VALUE

        var travelled = BEAM_STEP
        while (travelled < distance) {
            val blockX = floor(from.x + (direction.x * travelled)).toInt()
            val blockY = floor(from.y + (direction.y * travelled)).toInt()
            val blockZ = floor(from.z + (direction.z * travelled)).toInt()
            travelled += BEAM_STEP

            if (blockX == lastX && blockY == lastY && blockZ == lastZ) continue
            lastX = blockX
            lastY = blockY
            lastZ = blockZ

            if (!Bukkit.isOwnedByCurrentRegion(world, blockX shr 4, blockZ shr 4)) return true
            if (solidHitDistance(world.getBlockAt(blockX, blockY, blockZ), origin, direction, distance) >= 0) {
                return false
            }
        }
        return true
    }
}
