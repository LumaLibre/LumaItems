package dev.jsinco.luma.lumaitems.util.disabling;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public enum WorldName {

    ALL("all"),
    EVENT("event"),
    EVENT_NEW("event_new"),
    EVENT_THE_END("event_the_end"),
    SPAWN("spawn"),
    MAIN("main"),
    MAIN_NETHER("main_nether"),
    MAIN_THE_END("main_the_end"),
    MAIN_SEASONS("main_seasons"),
    RESOURCE("resource"),
    RESOURCE_NETHER("resource_nether"),
    STAFF("staff"),
    INTRODUCTION("introduction"),
    ;


    // Worlds that are normally accessible to players on LumaMC
    public static WorldName[] NORMALLY_ACCESSIBLE = {
            MAIN,
            MAIN_NETHER,
            MAIN_THE_END,
            MAIN_SEASONS,
            RESOURCE,
            RESOURCE_NETHER,
            SPAWN,
            EVENT,
            EVENT_NEW,
            EVENT_THE_END
    };

    private final String stringName;
    private final String uuid;

    WorldName(String stringName) {
        this.stringName = stringName;
        this.uuid = null;
    }

    WorldName(String stringName, String uuid) {
        this.stringName = stringName;
        this.uuid = uuid;
    }

    public String getStringName() {
        return stringName;
    }

    public String getUuid() {
        return uuid;
    }

    public boolean isInWorld(Entity entity) {
        return isInWorld(entity.getLocation());
    }

    public boolean isInWorld(Location location) {
        if (this == ALL) {
            return true;
        }

        World world = location.getWorld();
        if (world == null) {
            return false;
        }

        if (uuid != null) {
            return world.getName().equalsIgnoreCase(stringName) || world.getUID().toString().equals(uuid);
        }
        return world.getName().equalsIgnoreCase(stringName);
    }
}
