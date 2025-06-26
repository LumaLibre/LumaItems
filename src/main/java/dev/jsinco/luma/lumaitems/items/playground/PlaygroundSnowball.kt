package dev.jsinco.luma.lumaitems.items.playground

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.Executors
import dev.jsinco.luma.lumaitems.util.Util
import java.util.LinkedList
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.BoundingBox

class PlaygroundSnowball : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("Playground Snowball")
            .persistentData("playground-snowball")
            .material(Material.SNOWBALL)
            .buildPair()
    }

//    override fun onAsyncRunnable(player: Player) {
//        Executors.sync {
//            player.freezeTicks = player.maxFreezeTicks - 2
//        }
//    }



    override fun onProjectileLaunch(player: Player, event: ProjectileLaunchEvent) {
        //event.entity.velocity = event.entity.velocity.multiply(0.3)
        Util.setPersistentKey(event.entity, "playground-snowball", PersistentDataType.SHORT, 1)
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val hitEntity = event.hitEntity ?: return
        val blocks = getBlocks(
            hitEntity.boundingBox,
            hitEntity.world
        )
        for (block in blocks) {
            if (block.type.isAir) {
                block.type = Material.ICE
            }
        }
        hitEntity.remove()
    }


    private fun getBlocks(box : BoundingBox, world: World): List<Block> {
        val bL: MutableList<Block> = LinkedList()
        for (x in box.minX.toInt()..box.maxX.toInt()) {
            for (y in box.minY.toInt()..box.maxY.toInt()) {
                for (z in box.minZ.toInt()..box.maxZ.toInt()) {
                    bL.add(world.getBlockAt(x, y, z))
                }
            }
        }
        return bL
    }
}

//val particleDisplay = ParticleDisplay.of(Particle.DUST)
//            .withColor(Color.WHITE)
//
//        val entity = event.entity
//        val position = if (!event.entity.isInWater) {
//            entity.location
//        } else {
//            // keep going up until we find a non-water block
//            val blockIterator = BlockIterator(entity.world, entity.location.toVector(), Vector(0.0, 1.0, 0.0), 0.0, 20)
//            while (blockIterator.hasNext()) {
//                val next = blockIterator.next()
//                if (next.type.isAir) {
//                    next.location
//                }
//            }
//            entity.location // fallback if no block found
//        }
//        val destination = position.clone().add(0.0, 12.0, 0.0)
//
//
//        val lineCurrentEnd: Location = position.clone()
//
//        Executors.asyncTimer(0, 1) { task ->
//            //val directionToTarget = destination.clone().subtract(position).toVector().normalize()
//
//            val toDestination = destination.clone().subtract(lineCurrentEnd)
//            val distance = toDestination.length()
//
//            if (distance > 1) {
//                val directionStep = toDestination.toVector().normalize().multiply(0.9)
//                lineCurrentEnd.add(directionStep)
//            } else {
//                task.cancel()
//            }
//
//            Particles.line(position, lineCurrentEnd!!, 0.35, particleDisplay)
//        }