package dev.jsinco.luma.lumaitems.items.tools

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.particles.ParticleDisplay
import dev.jsinco.luma.lumaitems.particles.Particles
import dev.jsinco.luma.lumaitems.shapes.Sphere
import dev.jsinco.luma.lumaitems.util.Executors
import java.awt.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Arrow
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class UnnamedToolItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("unnamedtool2")
            .material(Material.WOODEN_SWORD)
            .persistentData("unnamedtool2")
            .buildPair()
    }

    override fun onLeftClick(player: Player, event: PlayerInteractEvent) {
        tornado(player)
    }
    
    
    private fun tornado(player: Player) {
        var ticksRun = 0
        val items: MutableList<Item> = mutableListOf()
        val projectile = player.launchProjectile(Arrow::class.java)
        projectile.pickupStatus = AbstractArrow.PickupStatus.DISALLOWED
        projectile.setNoPhysics(true)
        projectile.velocity = projectile.velocity.multiply(0.1)
        val particleDisplay = ParticleDisplay.of(Particle.DUST)
            .withColor(Color.WHITE)
        println("starting")
        Executors.syncTimer(0, 1) { task ->
            if (ticksRun++ >= 150 || projectile.location.distanceSquared(player.location) > 30.0 * 30.0) {
                task.cancel()
                projectile.remove()
                println("done")
                return@syncTimer
            }

            Particles.spikeSphere(0.2, 15.0, 60, 0.1, 0.3, particleDisplay.clone().withLocation(projectile.location))



            val cuboid = Sphere(projectile.location, 2.0, 0.0)
            cuboid.getSphereFast { block ->
                if (block.isEmpty || block.isLiquid) return@getSphereFast
                val drops = block.drops.map { item ->
                    block.world.dropItemNaturally(block.location, item)
                }
                block.world.spawn(block.location, FallingBlock::class.java) { fb ->
                    fb.cancelDrop = true
                }
                items.addAll(drops)
                block.type = Material.AIR
            }


            println("test2")
            for (item in items) {
                val direction = projectile.location.toVector().subtract(item.location.toVector())
                val newVel = direction.normalize().multiply(0.3)
                    .rotateAroundY(1.1)
                    .rotateAroundZ(0.9)
                item.velocity = newVel
            }

        }
    }
}
