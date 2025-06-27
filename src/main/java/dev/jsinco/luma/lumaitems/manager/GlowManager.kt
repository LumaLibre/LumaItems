@file:Suppress("deprecation")
package dev.jsinco.luma.lumaitems.manager

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.wrappers.WrappedDataValue
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import com.comphenix.protocol.wrappers.WrappedWatchableObject
import com.google.common.collect.Lists
import dev.jsinco.luma.lumaitems.LumaItems
import java.util.Objects
import java.util.UUID
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Team


object GlowManager {

    private val plugin = LumaItems.getInstance()
    val glowColors = listOf(
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

    private val board = Bukkit.getScoreboardManager().mainScoreboard
    private val teams: MutableList<Team> = mutableListOf()

    private val playerTeamsTasks: MutableMap<UUID, Int> = mutableMapOf()

    @JvmStatic
    fun initGlowTeams() {
        for (glowColor in glowColors) {
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
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, {
            removeGlowColor(entity)
            board.getEntityTeam(entity)?.addEntry(entity.uniqueId.toString())
        }, ticks)
    }

    fun addToTeamForTicks(player: Player, glowColor: NamedTextColor, ticks: Long) {
        val uuid = player.uniqueId
        if (playerTeamsTasks.contains(uuid)) {
            Bukkit.getScheduler().cancelTask(playerTeamsTasks[uuid]!!)
        }

        setGlowColor(player, glowColor)

        playerTeamsTasks[uuid] = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, {
            removeGlowColor(player)
        }, ticks)
    }

    fun addToTeamForTicks(entity: Entity, glowColor: ChatColor, ticks: Long) {
        setGlowColor(entity, glowColor)
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, {
            removeGlowColor(entity)
            board.getEntityTeam(entity)?.addEntry(entity.uniqueId.toString())
        }, ticks)
    }

    fun addToTeamForTicks(player: Player, glowColor: ChatColor, ticks: Long) {
        val uuid = player.uniqueId
        if (playerTeamsTasks.contains(uuid)) {
            Bukkit.getScheduler().cancelTask(playerTeamsTasks[uuid]!!)
        }

        setGlowColor(player, glowColor)

        playerTeamsTasks[uuid] = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, {
            removeGlowColor(player)
        }, ticks)
    }

    fun getGlowColorLegacy(entity: Entity): ChatColor? {
        return board.getEntityTeam(entity)?.color
    }

    fun getGlowColor(player: Entity): TextColor? {
        return board.getEntityTeam(player)?.color()
    }

    // liberally borrowed from: https://www.spigotmc.org/threads/i-want-to-use-protocollib-to-make-fake-entity-glow.589919/
    fun setProtocolGlowPacket(player: Player, entity: Entity, glow: Boolean) {
        val protocolManager = LumaItems.getProtocolManager() ?: return

        val packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA) // metadata packet
        packet.integers.write(0, entity.entityId) //Set entity id from packet above
        val watcher = WrappedDataWatcher() //Create data watcher, the Entity Metadata packet requires this
        val serializer = WrappedDataWatcher.Registry.get(java.lang.Byte::class.java)
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