package dev.lumas.lumaitems.enums

import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity

enum class CardinalDirection(val forwardFace: BlockFace, val rightFace: BlockFace, val backwardFace: BlockFace, val leftFace: BlockFace) {
    NORTH(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST),
    EAST(BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH),
    SOUTH(BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST),
    WEST(BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH);


    override fun toString(): String {
        return name[0].toString() // "N", "E", "S", "W"
    }

    companion object {
        fun fromEntity(entity: Entity): CardinalDirection {
            var yaw = (entity.location.yaw - 90.0) % 360.0
            if (yaw < 0) yaw += 360.0

            if (yaw !in 45.0..<315.0) return WEST
            if (yaw < 135.0) return NORTH
            if (yaw < 225.0) return EAST
            return SOUTH
        }
    }
}
