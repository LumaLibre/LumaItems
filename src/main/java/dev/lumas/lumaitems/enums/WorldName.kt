package dev.lumas.lumaitems.enums

import org.bukkit.Location
import org.bukkit.entity.Entity

enum class WorldName {
    ALL("all"),
    EVENT("event"),
    EVENT_NEW("event_new"),
    EVENT_THE_END("event_the_end"),
    SPAWN("spawn"),
    MAIN("main"),
    MAIN_NETHER("main_nether"),
    MAIN_THE_END("main_the_end"),
    MAIN_SEASONS("main_seasons"),
    MAIN_TERRALITH("main_terralith_dim_overworld"),
    RESOURCE("resource"),
    RESOURCE_NETHER("resource_nether"),
    RESOURCE_THE_END("resource_the_end"),
    RESOURCE_DESERT("resource_desert"),
    STAFF("staff"),
    INTRODUCTION("introduction"),
    PINATA("pinata"),
    SPECIAL("special"),

    // Mainly for testing/debugging
    WORLD("world"),
    WORLD_NETHER("world_nether"),
    WORLD_THE_END("world_the_end"),
    ;


    val stringName: String
    val uuid: String?

    constructor(stringName: String) {
        this.stringName = stringName
        this.uuid = null
    }

    constructor(stringName: String, uuid: String) {
        this.stringName = stringName
        this.uuid = uuid
    }

    fun isInWorld(entity: Entity): Boolean {
        return isInWorld(entity.location)
    }

    fun isInWorld(location: Location): Boolean {
        if (this == ALL) {
            return true
        }

        val world = location.getWorld() ?: return false

        if (uuid != null) {
            return world.name.equals(stringName, ignoreCase = true) || world.uid.toString() == uuid
        }
        return world.name.equals(stringName, ignoreCase = true)
    }

    companion object {
        val STANDARD_WORLDS: Array<WorldName> = arrayOf<WorldName>(
            MAIN,
            MAIN_NETHER,
            MAIN_THE_END,
            MAIN_SEASONS,
            MAIN_TERRALITH,
            RESOURCE,
            RESOURCE_NETHER,
            RESOURCE_THE_END,
            RESOURCE_DESERT
        )

        val TEST_WORLDS: Array<WorldName> = arrayOf<WorldName>(
            WORLD,
            WORLD_NETHER,
            WORLD_THE_END,
        )
    }
}
