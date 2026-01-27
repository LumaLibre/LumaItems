package dev.lumas.lumaitems.enums;

import org.bukkit.entity.Entity;

public enum CardinalDirection {
    NORTH, EAST, SOUTH, WEST;

    public static CardinalDirection fromEntityYaw(Entity entity) {
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
}
