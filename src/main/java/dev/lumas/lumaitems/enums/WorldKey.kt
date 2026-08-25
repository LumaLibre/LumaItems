package dev.lumas.lumaitems.enums

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity

enum class WorldKey(vararg worldKeys: String) {

    MAIN_OVERWORLD("minecraft:overworld"),
    MAIN_NETHER("minecraft:the_nether"),
    MAIN_END("minecraft:the_end"),
    MAIN_SEASONS("worlds:main_seasons"),
    MAIN_TERRALITH("terralith_dim:overworld"),

    RESOURCE_OVERWORLD("worlds:resource_overworld", "resource:overworld"),
    RESOURCE_THE_NETHER("worlds:resource_the_nether", "resource:the_nether"),
    RESOURCE_THE_END("worlds:resource_the_end", "resource:the_end"),
    RESOURCE_DESERT("worlds:resource_desert", "resource:desert"),

    EVENT("worlds:event"),
    EVENT_NEW("worlds:event_new"),
    SPECIAL("worlds:special"),
    PINATA("incu:pinata"),

    SPAWN("worlds:spawn"),

    STAFF("worlds:staff"),
    TESTING("worlds:testing"),

    BUILD_FLAT("worlds:build_flat"),
    BUILD_VOID("worlds:build_void"),
    ;

    val keys: Set<String> = worldKeys.map { it.lowercase() }.toSet()

    fun matches(world: World?): Boolean {
        return world != null && world.key.toString() in keys
    }

    fun isInWorld(location: Location): Boolean {
        return matches(location.world)
    }

    fun isInWorld(entity: Entity): Boolean {
        return matches(entity.world)
    }

    companion object {
        private val BY_KEY: Map<String, WorldKey> = entries
            .flatMap { world -> world.keys.map { it to world } }
            .toMap()

        fun of(world: World?): WorldKey? {
            return world?.let { BY_KEY[it.key.toString()] }
        }

        fun of(location: Location): WorldKey? {
            return of(location.world)
        }

        fun of(entity: Entity): WorldKey? {
            return of(entity.world)
        }
    }
}
