package dev.lumas.lumaitems.manager

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedDataValue
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import com.comphenix.protocol.wrappers.WrappedTeamParameters
import com.comphenix.protocol.wrappers.WrappedWatchableObject
import com.google.common.collect.Lists
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.util.Executors
import java.lang.reflect.Type
import java.util.Optional
import java.util.UUID
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Team


object GlowManager {

    val COLORS = listOf(
        NamedTextColor.AQUA,
        NamedTextColor.BLACK,
        NamedTextColor.BLUE,
        NamedTextColor.DARK_AQUA,
        NamedTextColor.DARK_BLUE,
        NamedTextColor.DARK_GRAY,
        NamedTextColor.DARK_GREEN,
        NamedTextColor.DARK_PURPLE,
        NamedTextColor.DARK_RED,
        NamedTextColor.GOLD,
        NamedTextColor.GRAY,
        NamedTextColor.GREEN,
        NamedTextColor.LIGHT_PURPLE,
        NamedTextColor.RED,
        NamedTextColor.YELLOW,
        NamedTextColor.WHITE
    )

    val PACKET_COLORS by lazy {
        listOf(
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
    }


    private val board = Bukkit.getScoreboardManager().mainScoreboard
    private val teams: MutableList<Team> = mutableListOf()

    private val playerTeamsTasks: MutableMap<UUID, Int> = mutableMapOf()

    @JvmStatic
    fun initGlowTeams() {
        for (glowColor in COLORS) {
            registerGlowColorTeam(glowColor)
        }
    }

    fun registerGlowColorTeam(glowColor: NamedTextColor): Boolean {
        val glowColorName = glowColor.toString().lowercase()
        val team = board.getTeam(glowColorName)
        if (team == null) {
            board.registerNewTeam(glowColorName).color(glowColor)
            teams.add(board.getTeam(glowColorName) ?: return false)
        }
        return true
    }

    fun setGlowColor(entity: Entity, glowColor: NamedTextColor) {
        val team = board.getTeam(glowColor.toString().lowercase())
        if (team == null) {
            LumaItems.log("Could not find team for color $glowColor")
        } else {
            team.addEntry(entity.uniqueId.toString())
        }
    }

    @Suppress("DEPRECATION")
    fun setGlowColor(entity: Entity, glowColor: ChatColor) {
        val team = board.getTeam(glowColor.name.lowercase())
        if (team == null) {
            LumaItems.log("Could not find team for color ${glowColor.name.lowercase()}")
        } else {
            team.addEntry(entity.uniqueId.toString())
        }
    }

    fun removeGlowColor(entity: Entity) {
        val team = board.getEntityTeam(entity) ?: return
        team.removeEntry(entity.uniqueId.toString())
    }


    fun addToTeamForTicks(entity: Entity, glowColor: NamedTextColor, ticks: Long) {
        setGlowColor(entity, glowColor)
        Executors.syncDelayed(ticks) {
            removeGlowColor(entity)
            board.getEntityTeam(entity)?.addEntry(entity.uniqueId.toString())
        }
    }

    fun addToTeamForTicks(player: Player, glowColor: NamedTextColor, ticks: Long) {
        val uuid = player.uniqueId
        if (playerTeamsTasks.contains(uuid)) {
            Bukkit.getScheduler().cancelTask(playerTeamsTasks[uuid]!!)
        }

        setGlowColor(player, glowColor)

        playerTeamsTasks[uuid] = Executors.syncDelayed(ticks) {
            removeGlowColor(player)
        }.taskId
    }

    @Suppress("DEPRECATION")
    fun addToTeamForTicks(entity: Entity, glowColor: ChatColor, ticks: Long) {
        setGlowColor(entity, glowColor)
        Executors.syncDelayed(ticks) {
            removeGlowColor(entity)
            board.getEntityTeam(entity)?.addEntry(entity.uniqueId.toString())
        }
    }

    @Suppress("DEPRECATION")
    fun addToTeamForTicks(player: Player, glowColor: ChatColor, ticks: Long) {
        val uuid = player.uniqueId
        if (playerTeamsTasks.contains(uuid)) {
            Bukkit.getScheduler().cancelTask(playerTeamsTasks[uuid]!!)
        }

        setGlowColor(player, glowColor)

        playerTeamsTasks[uuid] = Executors.syncDelayed(ticks) {
            removeGlowColor(player)
        }.taskId
    }

    @Suppress("DEPRECATION")
    fun getGlowColorLegacy(entity: Entity): ChatColor? {
        return board.getEntityTeam(entity)?.color
    }

    fun getGlowColor(player: Entity): TextColor? {
        return board.getEntityTeam(player)?.color()
    }

    fun removeProtocolTeam(player: Player, entity: Entity) {
        val protocolManager = LumaItems.getProtocolManager() ?: return
        val teamName = "glow_${entity.entityId}"

        val removePacket = protocolManager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM)
        removePacket.strings.write(0, teamName)
        removePacket.integers.write(0, 1) // REMOVE = 1

        protocolManager.sendServerPacket(player, removePacket)
    }

    fun setProtocolTeamColor(player: Player, entity: Entity, color: EnumWrappers.ChatFormatting) {
        val protocolManager = LumaItems.getProtocolManager() ?: return
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

    // liberally borrowed from: https://www.spigotmc.org/threads/i-want-to-use-protocollib-to-make-fake-entity-glow.589919/
    fun setProtocolGlowPacket(player: Player, entity: Entity, glow: Boolean) {
        val protocolManager = LumaItems.getProtocolManager() ?: return

        val packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA) // metadata packet
        packet.integers.write(0, entity.entityId) //Set entity id from packet above
        val watcher = WrappedDataWatcher() //Create data watcher, the Entity Metadata packet requires this
        val type: Type = java.lang.Byte::class.java
        val serializer = WrappedDataWatcher.Registry.get(type) // java.lang.Byte::class.java
        watcher.entity = player //Set the new data watcher's target
        watcher.setObject(0, serializer, (if (glow) 0x40 else 0).toByte()) //Set status to glowing, found on protocol page

        val wrappedDataValueList: MutableList<WrappedDataValue> = Lists.newArrayList()
        watcher.watchableObjects.filterNotNull().forEach { entry: WrappedWatchableObject ->
            val dataWatcherObject = entry.watcherObject
            wrappedDataValueList.add(WrappedDataValue(dataWatcherObject.index, dataWatcherObject.serializer, entry.rawValue))
        }
        packet.dataValueCollectionModifier.write(0, wrappedDataValueList)
        protocolManager.sendServerPacket(player, packet)
    }

}