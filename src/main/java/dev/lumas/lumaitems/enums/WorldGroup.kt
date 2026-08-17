package dev.lumas.lumaitems.enums

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity

enum class WorldGroup(vararg members: WorldKey) {

    ALL_WORLDS, // Includes worlds that don't have a known WorldKey

    MAIN_WORLDS(
        WorldKey.MAIN_OVERWORLD,
        WorldKey.MAIN_NETHER,
        WorldKey.MAIN_END,
        WorldKey.MAIN_SEASONS,
        WorldKey.MAIN_TERRALITH,
    ),

    RESOURCE_WORLDS(
        WorldKey.RESOURCE_OVERWORLD,
        WorldKey.RESOURCE_THE_NETHER,
        WorldKey.RESOURCE_THE_END,
        WorldKey.RESOURCE_DESERT,
    ),

    EVENT_WORLDS(
        WorldKey.EVENT,
        WorldKey.EVENT_NEW,
        WorldKey.SPECIAL,
    ),

    STAFF_WORLDS(
        WorldKey.STAFF,
        WorldKey.TESTING,
        WorldKey.BUILD_FLAT,
        WorldKey.BUILD_VOID,
    ),

    BUILD_WORLDS(
        WorldKey.BUILD_FLAT,
        WorldKey.BUILD_VOID,
    ),

    STANDARD_WORLDS(
        WorldKey.MAIN_OVERWORLD,
        WorldKey.MAIN_NETHER,
        WorldKey.MAIN_END,
        WorldKey.MAIN_SEASONS,
        WorldKey.MAIN_TERRALITH,
        WorldKey.RESOURCE_OVERWORLD,
        WorldKey.RESOURCE_THE_NETHER,
        WorldKey.RESOURCE_THE_END,
        WorldKey.RESOURCE_DESERT,
    ),
    ;

    val worlds: Set<WorldKey> = members.toSet()

    fun matches(world: World?): Boolean {
        if (this == ALL_WORLDS) return true
        val key = WorldKey.of(world) ?: return false
        return key in worlds
    }

    fun isInWorld(location: Location): Boolean {
        return matches(location.world)
    }

    fun isInWorld(entity: Entity): Boolean {
        return matches(entity.world)
    }
}
