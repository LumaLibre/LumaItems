package dev.lumas.lumaitems.items.weapons.scythe

import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.particles.ParticleDisplay
import dev.lumas.lumaitems.particles.Particles
import dev.lumas.lumaitems.util.Executors.syncTimer
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import java.util.UUID
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class UnnamedScytheItem : CustomItemFunctions() {


    companion object {
        private val REFERENCES: MutableMap<UUID, Reckon> = mutableMapOf()
    }


    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.Companion.builder()
            .name("Unnamed Scythe")
            .customEnchants("Seize")
            .persistentData("unnamed-scythe2")
            .material(Material.NETHERITE_HOE)
            .buildPair()
    }


    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        val loc = player.getTargetBlockExact(10)?.location
            ?: player.location.add(player.location.direction.multiply(10))

        val reckon = Reckon(
            player = player,
            initialLoc = loc,
            pulses = 3,
            pulseDelay = 20
        )

        reckon.startTicking()
    }


    private class Reckon(
        val player: Player,
        val initialLoc: Location,
        val pulses: Int,
        val pulseDelay: Long = 20
    ) {


        init {
            REFERENCES[player.uniqueId] = this
        }

        var task: ScheduledTask? = null

        fun startTicking() {
            val entities = initialLoc.world.getNearbyLivingEntities(initialLoc, 15.0)
                .filter { it != player }

            var currentPulse = 0
            var sinceLastPulse = 0
            this.task = player.syncTimer(0, 1) { task ->
                if (currentPulse >= pulses) {
                    task.cancel()
                    return@syncTimer
                }

                if (sinceLastPulse >= pulseDelay) {
                    for (entity in entities) {
                        val particleDisplay = ParticleDisplay.of(Particle.LAVA)
                            //.withColor(Color.WHITE)
                            .withLocation(entity.location)
                        Particles.meguminExplosion(LumaItems.getInstance(), entity.boundingBox.widthX, particleDisplay)
                    }
                    currentPulse++
                    sinceLastPulse = 0
                } else {
                    sinceLastPulse++
                }

            }
        }


        fun stopTicking() {
            this.task?.cancel()
            REFERENCES.remove(player.uniqueId)
        }

    }
}