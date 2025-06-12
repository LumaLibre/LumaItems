package dev.jsinco.luma.lumaitems.util

import dev.jsinco.luma.lumaitems.LumaItems
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

object Executors {

    private fun ticksToMillis(ticks: Long): Long {
        return ticks * 50 // 1 tick = 50 milliseconds
    }

    fun sync(runnable: Consumer<BukkitRunnable>): BukkitTask {
        return object : BukkitRunnable() {
            override fun run() {
                runnable.accept(this)
            }
        }.runTask(LumaItems.getInstance())
    }

    fun syncTimer(delay: Long, period: Long, runnable: Consumer<BukkitRunnable>): BukkitTask {
        return object : BukkitRunnable() {
            override fun run() {
                runnable.accept(this)
            }
        }.runTaskTimer(LumaItems.getInstance(), delay, period)
    }

    fun syncDelayed(delay: Long, runnable: Consumer<BukkitRunnable>): BukkitTask {
        return object : BukkitRunnable() {
            override fun run() {
                runnable.accept(this)
            }
        }.runTaskLater(LumaItems.getInstance(), delay)
    }

    fun async(runnable: Consumer<ScheduledTask>): ScheduledTask {
        return Bukkit.getAsyncScheduler().runNow(LumaItems.getInstance()) { task ->
            runnable.accept(task)
        }
    }

    fun asyncTimer(delay: Long, period: Long, runnable: Consumer<ScheduledTask>): ScheduledTask {
        return Bukkit.getAsyncScheduler().runAtFixedRate(LumaItems.getInstance(), Consumer { task ->
            runnable.accept(task)
        }, ticksToMillis(delay), ticksToMillis(period), TimeUnit.MILLISECONDS)
    }
}