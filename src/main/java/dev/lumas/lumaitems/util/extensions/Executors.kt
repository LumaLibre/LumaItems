@file:JvmName("SynchronizedExecutors")

package dev.lumas.lumaitems.util.extensions

import dev.lumas.lumaitems.LumaItems
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Entity

fun Entity.sync(runnable: Runnable): Boolean {
    return this.scheduler.execute(LumaItems.getInstance(), runnable, null, 1)
}

fun Location.sync(runnable: Runnable) {
    Bukkit.getRegionScheduler().execute(LumaItems.getInstance(), this, runnable)
}

fun Block.sync(runnable: Runnable) {
    Bukkit.getRegionScheduler().execute(LumaItems.getInstance(), this.location, runnable)
}

fun Entity.syncTimer(delay: Long, period: Long, runnable: Consumer<ScheduledTask>): ScheduledTask? {
    return this.scheduler.runAtFixedRate(LumaItems.getInstance(), runnable, null, delay.coerceAtLeast(1), period)
}

fun Collection<Entity>.syncTimer(delay: Long, period: Long, runnable: Consumer<ScheduledTask>): ScheduledTask? {
    return if (this.isEmpty()) {
        null
    } else {
        this.first().scheduler.runAtFixedRate(LumaItems.getInstance(), runnable, null, delay.coerceAtLeast(1), period)
    }
}

fun Location.syncTimer(delay: Long, period: Long, runnable: Consumer<ScheduledTask>): ScheduledTask {
    return Bukkit.getRegionScheduler().runAtFixedRate(LumaItems.getInstance(), this, runnable, delay.coerceAtLeast(1), period)
}

fun Entity.syncDelayed(delay: Long, runnable: Consumer<ScheduledTask>): ScheduledTask? {
    return this.scheduler.runDelayed(LumaItems.getInstance(), runnable, null, delay)
}

fun Location.syncDelayed(delay: Long, runnable: Consumer<ScheduledTask>): ScheduledTask {
    return Bukkit.getRegionScheduler().runDelayed(LumaItems.getInstance(), this, runnable, delay)
}


object Executors {

    private fun ticksToMillis(ticks: Long): Long {
        return ticks * 50
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

    @JvmStatic
    fun asyncTimer(delay: Long, period: Long, runnable: Consumer<ScheduledTask>): ScheduledTask {
        return Bukkit.getAsyncScheduler().runAtFixedRate(LumaItems.getInstance(), Consumer { task ->
            try {
                runnable.accept(task)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ticksToMillis(delay), ticksToMillis(period), TimeUnit.MILLISECONDS)
    }

    @JvmStatic
    fun asyncDelayed(delay: Long, runnable: Consumer<ScheduledTask>): ScheduledTask {
        return Bukkit.getAsyncScheduler().runDelayed(LumaItems.getInstance(), Consumer { task ->
            try {
                runnable.accept(task)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ticksToMillis(delay), TimeUnit.MILLISECONDS)
    }

    fun <T> (() -> T).async(): ScheduledTask {
        return async { this() }
    }
}