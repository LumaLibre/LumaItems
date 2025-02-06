package dev.jsinco.luma.lumaitems.obj

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.manager.CustomItem
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object QuickTasks {

    val activeCooldowns: MutableMap<Class<out CustomItem>, CustomItemCooldown> = ConcurrentHashMap()

    fun isOnCooldown(customItem: CustomItem, player: UUID): Boolean {
        return activeCooldowns[customItem::class.java]?.isOnCooldown(player) ?: false
    }

    fun addCooldown(customItem: CustomItem, player: UUID, ticks: Long) {
        val customItemCooldown = activeCooldowns[customItem::class.java]
            ?:
        CustomItemCooldown(customItem::class.java, mutableListOf()).also { activeCooldowns[customItem::class.java] = it }
        customItemCooldown.addCooldown(player)
        Bukkit.getScheduler().runTaskLaterAsynchronously(LumaItems.getInstance(), Runnable {
            customItemCooldown.removeCooldown(player)
        }, ticks)
    }

    fun addIndefinitely(customItem: CustomItem, player: UUID) {
        val customItemCooldown = activeCooldowns[customItem::class.java] ?:
        CustomItemCooldown(customItem::class.java, mutableListOf()).also { activeCooldowns[customItem::class.java] = it }
        customItemCooldown.addCooldown(player)
    }

    fun removeNow(customItem: CustomItem, player: UUID) {
        activeCooldowns[customItem::class.java]?.removeCooldown(player)
    }

    fun removeWhen(customItem: CustomItem, player: UUID, ticks: Long) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(LumaItems.getInstance(), Runnable {
            removeNow(customItem, player)
        }, ticks)
    }


    fun runTaskAsyncFor(period: Long, forAmount: Long, runnable: Runnable) {
        object : BukkitRunnable() {
            var ticksRan = 0L
            override fun run() {
                ticksRan += period
                if (ticksRan >= forAmount) {
                    cancel()
                }
                runnable.run()
            }
        }.runTaskTimerAsynchronously(LumaItems.getInstance(), 0L, period)
    }
}