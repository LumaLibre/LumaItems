package dev.jsinco.luma.lumaitems.util

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.obj.CustomItemCooldown
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object QuickTasks {

    val activeCooldowns: MutableMap<Class<out CustomItem>, CustomItemCooldown> = ConcurrentHashMap()
    val flagged: MutableMap<UUID, Any> = ConcurrentHashMap()

    @JvmStatic
    fun isOnCooldown(customItem: CustomItem, player: UUID): Boolean {
        return activeCooldowns[customItem::class.java]?.isOnCooldown(player) ?: false
    }

    @JvmStatic
    fun isOnCooldown(customItem: CustomItem, player: Player): Boolean {
        return activeCooldowns[customItem::class.java]?.isOnCooldown(player.uniqueId) ?: false
    }

    @JvmStatic
    fun addCooldown(customItem: CustomItem, player: Player, ticks: Long) {
        addCooldown(customItem, player.uniqueId, ticks)
    }

    @JvmStatic
    fun addCooldown(customItem: CustomItem, player: Player, ticks: Long, callback: () -> Unit) {
        addCooldown(customItem, player.uniqueId, ticks, callback)
    }

    @JvmStatic
    fun addCooldown(customItem: CustomItem, player: UUID, ticks: Long) {
        val customItemCooldown = activeCooldowns[customItem::class.java]
            ?:
        CustomItemCooldown(customItem::class.java, mutableListOf()).also { activeCooldowns[customItem::class.java] = it }
        customItemCooldown.addCooldown(player)
        Bukkit.getScheduler().runTaskLaterAsynchronously(LumaItems.getInstance(), Runnable {
            customItemCooldown.removeCooldown(player)
        }, ticks)
    }

    @JvmStatic
    fun addCooldown(customItem: CustomItem, player: UUID, ticks: Long, callback: () -> Unit) {
        val customItemCooldown = activeCooldowns[customItem::class.java]
            ?:
        CustomItemCooldown(customItem::class.java, mutableListOf()).also { activeCooldowns[customItem::class.java] = it }
        customItemCooldown.addCooldown(player)
        Bukkit.getScheduler().runTaskLaterAsynchronously(LumaItems.getInstance(), Runnable {
            customItemCooldown.removeCooldown(player)
            callback()
        }, ticks)
    }

    @JvmStatic
    fun getActiveCooldowns(customItem: CustomItem): Int {
        return activeCooldowns[customItem::class.java]?.players?.size ?: 0
    }

    fun addIndefinitely(customItem: CustomItem, player: UUID) {
        val customItemCooldown = activeCooldowns[customItem::class.java] ?:
        CustomItemCooldown(customItem::class.java, mutableListOf()).also { activeCooldowns[customItem::class.java] = it }
        customItemCooldown.addCooldown(player)
    }

    fun removeNow(player: UUID) {
        activeCooldowns.values.forEach { it.removeCooldown(player) }
    }

    fun removeNow(customItem: CustomItem, player: UUID) {
        activeCooldowns[customItem::class.java]?.removeCooldown(player)
    }

    fun removeWhen(customItem: CustomItem, player: UUID, ticks: Long) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(LumaItems.getInstance(), Runnable {
            removeNow(customItem, player)
        }, ticks)
    }


    fun flag(player: UUID, anything: Any) {
        flagged[player] = anything
    }

    fun flag(player: UUID) {
        flagged[player] = true
    }

    fun isFlagged(player: UUID): Boolean {
        return flagged.containsKey(player)
    }

    fun getFlag(player: UUID): Any? {
        return flagged[player]
    }

    fun <T> getFlag(player: UUID, anticipated: Class<T>): T? {
        return flagged[player] as? T
    }

    fun removeFlag(player: UUID) {
        flagged.remove(player)
    }

}