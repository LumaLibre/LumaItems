@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
package dev.lumas.lumaitems.util

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedDataValue
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import com.comphenix.protocol.wrappers.WrappedTeamParameters
import com.comphenix.protocol.wrappers.WrappedWatchableObject
import com.google.common.collect.Lists
import dev.lumas.lumaitems.hooks.ProtocolLibHook
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.util.extensions.lazyListOf
import dev.lumas.lumaitems.util.extensions.sync
import java.lang.Byte
import java.lang.reflect.Type
import java.util.Optional
import kotlin.Boolean
import kotlin.getValue
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

object PacketGlowColors {

    val PACKET_COLORS by lazyListOf(
        EnumWrappers.ChatFormatting.AQUA,
        EnumWrappers.ChatFormatting.BLACK,
        EnumWrappers.ChatFormatting.BLUE,
        EnumWrappers.ChatFormatting.DARK_AQUA,
        EnumWrappers.ChatFormatting.DARK_BLUE,
        EnumWrappers.ChatFormatting.DARK_GRAY,
        EnumWrappers.ChatFormatting.DARK_GREEN,
        EnumWrappers.ChatFormatting.DARK_PURPLE,
        EnumWrappers.ChatFormatting.DARK_RED,
        EnumWrappers.ChatFormatting.GOLD,
        EnumWrappers.ChatFormatting.GRAY,
        EnumWrappers.ChatFormatting.GREEN,
        EnumWrappers.ChatFormatting.LIGHT_PURPLE,
        EnumWrappers.ChatFormatting.RED,
        EnumWrappers.ChatFormatting.YELLOW,
        EnumWrappers.ChatFormatting.WHITE
    )


    fun removeProtocolTeam(player: Player, entity: Entity) {
        val protocolManager = Registry.HOOKS.getOrThrow(ProtocolLibHook::class).getProtocolManager() ?: return
        val teamName = "lumaitems_${entity.entityId}"

        val removePacket = protocolManager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM)
        removePacket.strings.write(0, teamName)
        removePacket.integers.write(0, 1) // REMOVE = 1

        protocolManager.sendServerPacket(player, removePacket)
    }

    fun setProtocolTeamColor(player: Player, entity: Entity, color: EnumWrappers.ChatFormatting) {
        entity.sync {
            val protocolManager = Registry.HOOKS.getOrThrow(ProtocolLibHook::class).getProtocolManager() ?: return@sync
            val teamName = "glow_${entity.entityId}"
            val entry = if (entity is Player) entity.name else entity.uniqueId.toString()
            val teamParams = WrappedTeamParameters.newBuilder()
                .displayName(WrappedChatComponent.fromText(teamName))
                .prefix(WrappedChatComponent.fromText(""))
                .suffix(WrappedChatComponent.fromText(""))
                .color(color)
                .nametagVisibility(EnumWrappers.TeamVisibility.ALWAYS)
                .collisionRule(EnumWrappers.TeamCollisionRule.ALWAYS)
                .build()


            // Create team
            val createPacket = protocolManager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM)
            createPacket.strings.write(0, teamName)
            createPacket.integers.write(0, 0) // CREATE/UPDATE = 0, REMOVE = 1
            createPacket.optionalTeamParameters.write(0, Optional.of(teamParams))
            protocolManager.sendServerPacket(player, createPacket)

            // Add entity to the team
            val addPacket = protocolManager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM)
            addPacket.strings.write(0, teamName)
            addPacket.integers.write(0, 3) // ADD_PLAYERS
            addPacket.getSpecificModifier(Collection::class.java).write(0, listOf(entry))
            protocolManager.sendServerPacket(player, addPacket)
        }
    }

    // liberally borrowed from: https://www.spigotmc.org/threads/i-want-to-use-protocollib-to-make-fake-entity-glow.589919/
    fun setProtocolGlowPacket(player: Player, entity: Entity, glow: Boolean) {
        entity.sync {
            val protocolManager = Registry.HOOKS.getOrThrow(ProtocolLibHook::class).getProtocolManager() ?: return@sync

            val packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA) // metadata packet
            packet.integers.write(0, entity.entityId) //Set entity id from packet above
            val watcher = WrappedDataWatcher() //Create data watcher, the Entity Metadata packet requires this
            val type: Type = Byte::class.java
            val serializer = WrappedDataWatcher.Registry.get(type) // java.lang.Byte::class.java
            watcher.entity = player //Set the new data watcher's target
            watcher.setObject(0, serializer, (if (glow) 0x40 else 0).toByte()) //Set status to glowing, found on protocol page

            val wrappedDataValueList: MutableList<WrappedDataValue> = Lists.newArrayList()
            watcher.watchableObjects.filterNotNull().forEach { entry: WrappedWatchableObject ->
                val dataWatcherObject = entry.watcherObject
                wrappedDataValueList.add(
                    WrappedDataValue(
                        dataWatcherObject.index,
                        dataWatcherObject.serializer,
                        entry.rawValue
                    )
                )
            }
            packet.dataValueCollectionModifier.write(0, wrappedDataValueList)
            protocolManager.sendServerPacket(player, packet)
        }
    }

}