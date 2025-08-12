package dev.jsinco.luma.lumaitems.util

import kotlin.math.cos
import kotlin.math.sin
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.util.Vector

/**
 * Utility class for vector operations in Bukkit that are
 * shared across multiple items.
 */
object BukkitVectors {

    val UP = Vector(0.0, 1.0, 0.0)
    val DOWN = Vector(0.0, -1.0, 0.0)
    val NORTH = Vector(0.0, 0.0, -1.0)
    val SOUTH = Vector(0.0, 0.0, 1.0)
    val EAST = Vector(1.0, 0.0, 0.0)
    val WEST = Vector(-1.0, 0.0, 0.0)
    val ZERO = Vector(0.0, 0.0, 0.0)
    val ONE = Vector(1.0, 1.0, 1.0)

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

}