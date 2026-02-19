package dev.lumas.lumaitems.enums;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

public enum CardinalDirection {
    NORTH(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST),
    EAST(BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH),
    SOUTH(BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST),
    WEST(BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH);

    private final BlockFace forwardFace;
    private final BlockFace rightFace;
    private final BlockFace backwardFace;
    private final BlockFace leftFace;

    CardinalDirection(BlockFace forwardFace, BlockFace rightFace, BlockFace backwardFace, BlockFace leftFace) {
        this.forwardFace = forwardFace;
        this.rightFace = rightFace;
        this.backwardFace = backwardFace;
        this.leftFace = leftFace;
    }

    public static CardinalDirection fromEntity(Entity entity) {
        double yaw = (entity.getLocation().getYaw() - 90.0) % 360.0;
        if (yaw < 0) yaw += 360.0;

        if (yaw < 45.0 || yaw >= 315.0) return WEST;
        if (yaw < 135.0) return NORTH;
        if (yaw < 225.0) return EAST;
        return SOUTH;
    }

    @Override
    public String toString() {
        return name().substring(0, 1); // "N", "E", "S", "W"
    }

    public BlockFace getForwardFace() {
        return forwardFace;
    }

    public BlockFace getRightFace() {
        return rightFace;
    }

    public BlockFace getBackwardFace() {
        return backwardFace;
    }

    public BlockFace getLeftFace() {
        return leftFace;
    }
}
