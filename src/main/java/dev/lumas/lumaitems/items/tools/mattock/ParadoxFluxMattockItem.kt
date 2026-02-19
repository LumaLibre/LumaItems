package dev.lumas.lumaitems.items.tools.mattock

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.particles.ParticleDisplay
import dev.lumas.lumaitems.particles.Particles
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.extensions.Executors
import dev.lumas.lumaitems.util.extensions.breakNaturallyWithLog
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.extensions.toColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class ParadoxFluxMattockItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("FLUX/PARADOX Mattock")
            .material(Material.NETHERITE_PICKAXE)
            .persistentData("flux-paradox-mattock")
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {

        val center = player.location
        val particleDisplay = ParticleDisplay.of(Particle.DUST_COLOR_TRANSITION)
            .withTransitionColor("#954381".toColor(), "#ED70BB".toColor(), 1f)
            .withLocation(center)
        val radius = 8.0
        var ticksRan = 0
        Executors.asyncTimer(0, 1) {
            if (++ticksRan > 200) {
                it.cancel()
                return@asyncTimer
            }
            Particles.neopaganPentagram(radius, 0.05, 0.0, particleDisplay, particleDisplay)

            val randomLoc = center.clone().add(random().nextDouble(-8.0, 8.0), random().nextDouble(20.0), random().nextDouble(-8.0, 8.0))
            // draw line down from this y level until center y level and break blocks along the way
            val bottom = randomLoc.clone().subtract(0.0, randomLoc.y - center.y, 0.0)
            val line = BukkitVectors.line(bottom, randomLoc)
            line.forEach { loc ->
                val block = loc.block
                if (!block.type.isAir) {
                    block.sync {
                        block.breakNaturallyWithLog(player, player.inventory.itemInMainHand, true)
                    }
                }
            }

            Particles.line(bottom, randomLoc, 0.2, particleDisplay)
        }
    }
}