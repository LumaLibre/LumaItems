package dev.lumas.lumaitems.model

import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.util.extensions.coerce
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import java.util.function.Consumer
import org.bukkit.Bukkit

interface Synchronizable {

    fun sync(consumer: Consumer<ScheduledTask>): ScheduledTask
    fun syncTimer(delay: Long, period: Long, consumer: Consumer<ScheduledTask>): ScheduledTask
    fun syncDelayed(delay: Long, consumer: Consumer<ScheduledTask>): ScheduledTask

    interface Location : Synchronizable {
        val location: org.bukkit.Location

        override fun sync(consumer: Consumer<ScheduledTask>): ScheduledTask {
            return Bukkit.getRegionScheduler().run(LumaItems.getInstance(), location, consumer)
        }

        override fun syncTimer(delay: Long, period: Long, consumer: Consumer<ScheduledTask>): ScheduledTask {
            return Bukkit.getRegionScheduler().runAtFixedRate(LumaItems.getInstance(), location, consumer, delay.coerce(), period.coerce())
        }

        override fun syncDelayed(delay: Long, consumer: Consumer<ScheduledTask>): ScheduledTask {
            return Bukkit.getRegionScheduler().runDelayed(LumaItems.getInstance(), location, consumer, delay.coerce())
        }
    }


    interface Entity : Synchronizable {
        val entity: org.bukkit.entity.Entity

        override fun sync(consumer: Consumer<ScheduledTask>): ScheduledTask {
            return entity.scheduler.run(LumaItems.getInstance(), consumer, null) ?: NoOperativeScheduledTask.create()
        }

        override fun syncTimer(delay: Long, period: Long, consumer: Consumer<ScheduledTask>): ScheduledTask {
            return entity.scheduler.runAtFixedRate(LumaItems.getInstance(), consumer, null, delay.coerce(), period.coerce()) ?: NoOperativeScheduledTask.create()
        }

        override fun syncDelayed(delay: Long, consumer: Consumer<ScheduledTask>): ScheduledTask {
            return entity.scheduler.runDelayed(LumaItems.getInstance(), consumer, null, delay.coerce()) ?: NoOperativeScheduledTask.create()
        }
    }


    interface Chunk : Synchronizable {
        val chunk: org.bukkit.Chunk

        override fun sync(consumer: Consumer<ScheduledTask>): ScheduledTask {
            return Bukkit.getRegionScheduler().run(LumaItems.getInstance(), chunk.world, chunk.x, chunk.z, consumer)
        }

        override fun syncTimer(delay: Long, period: Long, consumer: Consumer<ScheduledTask>): ScheduledTask {
            return Bukkit.getRegionScheduler().runAtFixedRate(LumaItems.getInstance(), chunk.world, chunk.x, chunk.z, consumer, delay.coerce(), period.coerce())
        }

        override fun syncDelayed(delay: Long, consumer: Consumer<ScheduledTask>): ScheduledTask {
            return Bukkit.getRegionScheduler().runDelayed(LumaItems.getInstance(), chunk.world, chunk.x, chunk.z, consumer, delay.coerce())
        }
    }

    interface ChunkPos : Synchronizable {
        val world: org.bukkit.World
        val x: Int
        val z: Int


        override fun sync(consumer: Consumer<ScheduledTask>): ScheduledTask {
            return Bukkit.getRegionScheduler().run(LumaItems.getInstance(), world, x, z, consumer)
        }

        override fun syncTimer(delay: Long, period: Long, consumer: Consumer<ScheduledTask>): ScheduledTask {
            return Bukkit.getRegionScheduler().runAtFixedRate(LumaItems.getInstance(), world, x, z, consumer, delay.coerce(), period.coerce())
        }

        override fun syncDelayed(delay: Long, consumer: Consumer<ScheduledTask>): ScheduledTask {
            return Bukkit.getRegionScheduler().runDelayed(LumaItems.getInstance(), world, x, z, consumer, delay.coerce())
        }
    }


    interface Block : Synchronizable {
        val block: org.bukkit.block.Block

        override fun sync(consumer: Consumer<ScheduledTask>): ScheduledTask {
            return Bukkit.getRegionScheduler().run(LumaItems.getInstance(), block.location, consumer)
        }

        override fun syncTimer(delay: Long, period: Long, consumer: Consumer<ScheduledTask>): ScheduledTask {
            return Bukkit.getRegionScheduler().runAtFixedRate(LumaItems.getInstance(), block.location, consumer, delay.coerce(), period.coerce())
        }

        override fun syncDelayed(delay: Long, consumer: Consumer<ScheduledTask>): ScheduledTask {
            return Bukkit.getRegionScheduler().runDelayed(LumaItems.getInstance(), block.location, consumer, delay.coerce())
        }
    }

    interface BlockPos : Synchronizable {
        val world: org.bukkit.World
        val x: Int
        val z: Int

        private fun Int.shift(): Int {
            return this shr 4
        }

        override fun sync(consumer: Consumer<ScheduledTask>): ScheduledTask {
            return Bukkit.getRegionScheduler().run(LumaItems.getInstance(), world, x.shift(), z.shift(), consumer)
        }

        override fun syncTimer(delay: Long, period: Long, consumer: Consumer<ScheduledTask>): ScheduledTask {
            return Bukkit.getRegionScheduler().runAtFixedRate(LumaItems.getInstance(), world, x.shift(), z.shift(), consumer, delay.coerce(), period.coerce())
        }

        override fun syncDelayed(delay: Long, consumer: Consumer<ScheduledTask>): ScheduledTask {
            return Bukkit.getRegionScheduler().runDelayed(LumaItems.getInstance(), world, x.shift(), z.shift(), consumer, delay.coerce())
        }
    }
}