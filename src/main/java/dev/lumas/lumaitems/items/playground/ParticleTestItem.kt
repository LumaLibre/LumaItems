package dev.lumas.lumaitems.items.playground

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.particles.ParticleDisplay
import dev.lumas.lumaitems.particles.Particles
import java.awt.Color
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class ParticleTestItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "Particle Test",
            mutableListOf("This is a test item"),
            mutableListOf("This is a test item"),
            Material.NETHERITE_SWORD,
            mutableListOf("particle_test"),
            mutableMapOf(Enchantment.SHARPNESS to 2, Enchantment.MENDING to 1)
        )
        item.tier = "&a&lDebug"
        return Pair("particle_test", item.createItem())
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        val location = player.location

        val particleDisplayCloud = ParticleDisplay.of(Particle.WAX_OFF).withLocation(location).withColor(Color.RED)
        val maxRadius = 10.0
        val arms = 5
        val spiralTightness = 0.8
        val display = ParticleDisplay.of(Particle.DUST)
            .withLocation(player.location)
            .withColor(Color(255, 0, 150))

        var currentRadius = 0.0

        Bukkit.getAsyncScheduler().runAtFixedRate(instance(), { task ->
            currentRadius += 0.4

            if (currentRadius >= maxRadius) {
                task.cancel()
                return@runAtFixedRate
            }

            // Draw spiral arms up to current radius
            for (arm in 0 until arms) {
                val armOffset = (arm * Math.PI * 2.0) / arms

                var r = Math.max(0.3, currentRadius - 1.5) // only draw the leading edge
                while (r <= currentRadius) {
                    val theta = armOffset + (r * spiralTightness)
                    val x = r * Math.cos(theta)
                    val z = r * Math.sin(theta)

                    val spread = 0.1 + (r * 0.12)
                    for (s in 0..3) {
                        val sx = x + Particles.random(-spread, spread)
                        val sz = z + Particles.random(-spread, spread)
                        display.spawn(sx, 0.1, sz)
                    }
                    r += 0.1
                }
            }

            for (i in 0..5) {
                val r = currentRadius + Particles.random(0.5, 2.0)
                val theta = Particles.random(0.0, Particles.PII)
                val x = r * Math.cos(theta)
                val z = r * Math.sin(theta)
                for (j in 0..2) {
                    display.spawn(
                        x + Particles.random(-0.3, 0.3),
                        0.1,
                        z + Particles.random(-0.3, 0.3)
                    )
                }
            }
        }, 0, 50, java.util.concurrent.TimeUnit.MILLISECONDS)
    }
}
