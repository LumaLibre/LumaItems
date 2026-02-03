package dev.lumas.lumaitems.util

import dev.lumas.lumaitems.LumaItems
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

object Executors {

    private fun ticksToMillis(ticks: Long): Long {
        return ticks * 50 // 1 tick = 50 milliseconds
    }

    @Deprecated("Not ThreadedRegions safe")
    fun sync(runnable: Runnable): BukkitTask? {
        throw UnsupportedOperationException("Not ThreadedRegions safe")
    }

    fun Entity.syncEntity(runnable: Runnable): Boolean {
        return this.scheduler.execute(LumaItems.getInstance(), runnable, null, 1)
    }

    fun Location.syncLocation(runnable: Runnable) {
        Bukkit.getRegionScheduler().execute(LumaItems.getInstance(), this, runnable)
    }


    @Deprecated("Not ThreadedRegions safe")
    fun syncTimer(delay: Long, period: Long, runnable: Consumer<BukkitRunnable>): BukkitTask {
        return object : BukkitRunnable() {
            override fun run() {
                try {
                    runnable.accept(this)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.runTaskTimer(LumaItems.getInstance(), delay, period)
    }

    fun Entity.syncEntityTimer(delay: Long, period: Long, runnable: Consumer<ScheduledTask>): ScheduledTask? {
        return this.scheduler.runAtFixedRate(LumaItems.getInstance(), runnable, null, delay.coerceAtLeast(1), period)
    }

    fun Collection<Entity>.syncEntityTimer(delay: Long, period: Long, runnable: Consumer<ScheduledTask>): ScheduledTask? {
        return if (this.isEmpty()) {
            null
        } else {
            this.first().scheduler.runAtFixedRate(LumaItems.getInstance(), runnable, null, delay.coerceAtLeast(1), period)
        }
    }

    fun Location.syncLocationTimer(delay: Long, period: Long, runnable: Consumer<ScheduledTask>): ScheduledTask {
        return Bukkit.getRegionScheduler().runAtFixedRate(LumaItems.getInstance(), this, runnable, delay.coerceAtLeast(1), period)
    }

    @Deprecated("Not ThreadedRegions safe")
    fun syncDelayed(delay: Long, runnable: Consumer<BukkitRunnable>): BukkitTask {
        return object : BukkitRunnable() {
            override fun run() {
                try {
                    runnable.accept(this)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.runTaskLater(LumaItems.getInstance(), delay)
    }

    fun Entity.syncEntityDelayed(delay: Long, runnable: Consumer<ScheduledTask>): ScheduledTask? {
        return this.scheduler.runDelayed(LumaItems.getInstance(), runnable, null, delay)
    }

    fun Location.syncLocationDelayed(delay: Long, runnable: Consumer<ScheduledTask>): ScheduledTask {
        return Bukkit.getRegionScheduler().runDelayed(LumaItems.getInstance(), this, runnable, delay)
    }

    @JvmStatic
    fun async(runnable: Consumer<ScheduledTask>): ScheduledTask {
        return Bukkit.getAsyncScheduler().runNow(LumaItems.getInstance()) { task ->
            try {
                runnable.accept(task)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun asyncTimer(delay: Long, period: Long, runnable: Consumer<ScheduledTask>): ScheduledTask {
        return Bukkit.getAsyncScheduler().runAtFixedRate(LumaItems.getInstance(), Consumer { task ->
            try {
                runnable.accept(task)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ticksToMillis(delay), ticksToMillis(period), TimeUnit.MILLISECONDS)
    }

    fun asyncDelayed(delay: Long, runnable: Consumer<ScheduledTask>): ScheduledTask {
        return Bukkit.getAsyncScheduler().runDelayed(LumaItems.getInstance(), Consumer { task ->
            try {
                runnable.accept(task)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ticksToMillis(delay), TimeUnit.MILLISECONDS)
    }

    @Deprecated("Not ThreadedRegions safe")
    fun <T> (() -> T).sync(): BukkitTask? {
        return sync { this() }
    }

    fun <T> (() -> T).async(): ScheduledTask {
        return async { this() }
    }
}