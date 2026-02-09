package dev.lumas.lumaitems.enums

import org.bukkit.Location
import org.bukkit.block.BlockFace

enum class SimpleDirection(val blockFace: BlockFace) {
    NORTH(BlockFace.NORTH),
    EAST(BlockFace.EAST),
    SOUTH(BlockFace.SOUTH),
    WEST(BlockFace.WEST),
    UP(BlockFace.UP),
    DOWN(BlockFace.DOWN);

    val vector = blockFace.direction

    companion object {
        fun fromLocation(location: Location, ignoreUpDown: Boolean = false): SimpleDirection {
            val pitch = location.pitch

            if (!ignoreUpDown) {
                // If looking steeply up or down, that wins
                if (pitch < -45) return UP
                if (pitch > 45) return DOWN
            }

            // Otherwise, use yaw to determine horizontal direction
            val yaw = ((location.yaw % 360) + 360) % 360
            return when (yaw) {
                !in 45.0..<315.0 -> SOUTH
                in 45.0..<135.0 -> WEST
                in 135.0..<225.0 -> NORTH
                else -> EAST
            }
        }
    }
}