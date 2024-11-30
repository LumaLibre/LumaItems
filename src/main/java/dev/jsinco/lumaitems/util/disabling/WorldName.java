package dev.jsinco.lumaitems.util.disabling;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public enum WorldName {
    EVENT("event"),
    EVENT_NEW("event_new");


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
