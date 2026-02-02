package dev.lumas.lumaitems.util

import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.util.Vector

/**
 * Utility class for vector/location operations in Bukkit that are
 * shared across multiple items.
 */
object BukkitVectors {

    val UP = Vector(0.0, 1.0, 0.0)
        get() = field.clone()
    val DOWN = Vector(0.0, -1.0, 0.0)
        get() = field.clone()
    val NORTH = Vector(0.0, 0.0, -1.0)
        get() = field.clone()
    val SOUTH = Vector(0.0, 0.0, 1.0)
        get() = field.clone()
    val EAST = Vector(1.0, 0.0, 0.0)
        get() = field.clone()
    val WEST = Vector(-1.0, 0.0, 0.0)
        get() = field.clone()
    val ZERO = Vector(0.0, 0.0, 0.0)
        get() = field.clone()
    val ONE = Vector(1.0, 1.0, 1.0)
        get() = field.clone()

    fun rotateVectorY(vector: Vector, angleRadians: Double): Vector {
        val x = vector.x
        val z = vector.z
        val cos = cos(angleRadians)
        val sin = sin(angleRadians)
        return Vector(x * cos - z * sin, vector.y, x * sin + z * cos)
    }

    fun rotateVectorX(vector: Vector, angleRadians: Double): Vector {
        val y = vector.y
        val z = vector.z
        val cos = cos(angleRadians)
        val sin = sin(angleRadians)
        return Vector(vector.x, y * cos - z * sin, y * sin + z * cos)
    }

    fun rotateVectorZ(vector: Vector, angleRadians: Double): Vector {
        val x = vector.x
        val y = vector.y
        val cos = cos(angleRadians)
        val sin = sin(angleRadians)
        return Vector(x * cos - y * sin, x * sin + y * cos, vector.z)
    }


    fun direction(from: Location, to: Location): Vector {
        return to.toVector().subtract(from.toVector()).normalize()
    }


    fun bounceWithBlockFace(entity: Entity, surface: BlockFace, magnitude: Double = 2.0): Vector {
        val velocity = entity.velocity
        val normal = Vector(surface.modX.toDouble(), surface.modY.toDouble(), surface.modZ.toDouble()).normalize()
        val dot = velocity.dot(normal)
        val reflected = velocity.clone().subtract(normal.clone().multiply(2 * dot))

        return reflected.normalize().multiply(velocity.length() * magnitude)
    }

    fun bounceWithEntity(entity: Entity, hitPoint: Location, hitEntity: LivingEntity, magnitude: Double = 2.0): Vector {
        val centerAsBlock = hitEntity.eyeLocation.block
        // Get the closest BlockFace of 'centerAsBlock' to the hitPoint
        val closestFace: BlockFace = listOf(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)
            .minByOrNull { face ->
                val faceCenter = centerAsBlock.location
                    .add(face.modX * 0.5, face.modY * 0.5, face.modZ * 0.5) // middle of that face
                faceCenter.distanceSquared(hitPoint)
            } ?: BlockFace.SELF


        return bounceWithBlockFace(entity, closestFace, magnitude).multiply(7)
    }


    // ripped mostly from NMS' trident loyalty implementation
    fun flyToLivingEntity(living: LivingEntity, otherEntity: Entity, speedFactor: Double, stopDistance: Double = 2.0, currentVelMultiplicity: Double = 0.8): Vector {
        val otherPos = otherEntity.location.toVector()
        val eyePos = living.eyeLocation.toVector()


        if (eyePos.distanceSquared(otherPos) <= stopDistance * stopDistance) {
            return BukkitVectors.ZERO
        }

        val vecToEntity = eyePos.subtract(otherPos) // direction from otherEntity -> entity

        val yNudge = vecToEntity.y * 0.019
        val speed = 0.05 * speedFactor

        // smooth/dampen current velocity and add homing component
        val currentVel = otherEntity.velocity
        val newVel = currentVel.multiply(currentVelMultiplicity).add(vecToEntity.normalize().multiply(speed))
        newVel.y += yNudge

        return newVel
    }


    fun seizeToAnchor(
        entity: Entity,
        anchor: Location,
        maxDistance: Double = 7.0,
        stiffness: Double = 0.4,
        damping: Double = 0.7,
        slack: Double = 0.5
    ): Vector? {
        val entityPos = entity.location.toVector()
        val anchorPos = anchor.toVector()
        val delta = entityPos.clone().subtract(anchorPos)
        val distance = delta.length()

        // Only pull back if stretched past the leash length + slack
        if (distance <= maxDistance + slack) return null

        val direction = delta.normalize()
        val excess = distance - maxDistance


        val force = direction.multiply(excess * stiffness)
        val newVelocity = entity.velocity.subtract(force)
        return newVelocity.multiply(damping)
    }


    fun propelAway(start: Location, goal: Location, speed: Double = 0.3, yBias: Double = 1.5): Vector {
        val playerVec = start.toVector()
        val pointVec = goal.toVector()

        val direction = pointVec.subtract(playerVec).normalize()

        direction.setY(yBias)

        return direction.multiply(speed)
    }

    fun randomGoalLocation(
        center: Location,
        minDistance: Double = 0.35,
        maxDistance: Double = 1.0,
        yRange: Double = 0.0
    ): Location {
        val angle = Random.nextDouble(0.0, 2 * Math.PI)
        val distance = Random.nextDouble(minDistance, maxDistance)

        val xAdd = distance * cos(angle)
        val zAdd = distance * sin(angle)
        val yAdd = if (yRange > 0.0) Random.nextDouble(-yRange, yRange) else 0.0

        return center.clone().add(xAdd, yAdd, zAdd)
    }
}