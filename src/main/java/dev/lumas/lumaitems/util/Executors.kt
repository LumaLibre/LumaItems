package dev.lumas.lumaitems.util

import dev.lumas.lumaitems.LumaItems
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

    fun sync(runnable: Runnable): BukkitTask? {
        if (Bukkit.isPrimaryThread()) {
            runnable.run()
            return null
        } else {
            return Bukkit.getScheduler().runTask(LumaItems.getInstance(), runnable)
        }
    }

//    fun sync(runnable: Consumer<BukkitRunnable>): BukkitTask {
//        return object : BukkitRunnable() {
//            override fun run() {
//                try {
//                    runnable.accept(this)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//        }.runTask(LumaItems.getInstance())
//    }

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


    fun <T> (() -> T).sync(): BukkitTask? {
        return sync { this() }
    }

    fun <T> (() -> T).async(): ScheduledTask {
        return async { this() }
    }
}