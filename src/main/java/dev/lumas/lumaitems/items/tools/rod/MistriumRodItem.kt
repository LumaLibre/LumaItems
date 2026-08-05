package dev.lumas.lumaitems.items.tools.rod

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.particles.ParticleDisplay
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.Executors
import dev.lumas.lumaitems.util.extensions.canBuild
import dev.lumas.lumaitems.util.extensions.isHoldingTwoRods
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.extensions.toBukkitColor
import dev.lumas.lumaitems.util.extensions.toColor
import java.util.function.Consumer
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerFishEvent

class MistriumRodItem : CustomItemFunctions() {

    private companion object {
        const val CLOUD_HEIGHT = 2.0
        const val MAX_FALL = 24.0
        const val FALL_SPEED = 1.1
        const val DROPS_PER_TICK = 2
        val RAIN = ParticleDisplay.of(Particle.RAIN)
        val DUST_COLOR_TRANSITION = ParticleDisplay.of(Particle.DUST_COLOR_TRANSITION)
        val BLOCK_EFFECTS: Map<Material, Consumer<Block>> = mapOf(
            Material.DIRT to Consumer { it.type = Material.GRASS_BLOCK },
            Material.LAVA to Consumer { it.type = Material.OBSIDIAN; it.world.playSound(it.location, Sound.BLOCK_LAVA_EXTINGUISH, 1f, 1f) },
        )
        val COLORS = listOf("#f291a4", "#fbcdd2", "#f9ecde", "#6f82b6", "#97dcfb").map { it.toColor() }
    }

    override fun createItem() = ItemFactory.builder()
        .name("<b><gradient:#f291a4:#fbcdd2:#f9ecde:#6f82b6:#97dcfb>Mistrium Rod</gradient></b>")
        .customEnchants("<#F5A5B3>Drizzle")
        .persistentData("mistrium-rod")
        .material(Material.FISHING_ROD)
        .tier(Tier.LUMARINE_2026)
        .vanillaEnchants(
            Enchantment.LURE to 5,
            Enchantment.LUCK_OF_THE_SEA to 6,
            Enchantment.UNBREAKING to 3,
            Enchantment.MENDING to 1
        )
        .lore(
            "A fishing rod that",
            "forces a permanent",
            "overcast sky.",
            "",
            "Allows baits to be",
            "reeled extremely",
            "quick when <#F5A5B3>fishing</#F5A5B3>."
        )
        .buildPair()


    override fun onFish(player: Player, event: PlayerFishEvent) {
        if (player.isHoldingTwoRods()) return

        if (event.state == PlayerFishEvent.State.FISHING) {
            val hook = event.hook
            hook.isRainInfluenced = false
            event.hand?.let { player.damageItemStack(it, 7) }

            hook.minLureTime = (hook.minLureTime * 0.22).toInt()
            hook.maxLureTime = (hook.maxLureTime * 0.22).toInt()
        }
    }

    override fun onFastAsyncRunnable(player: Player) {
        player.sync {
            if (player.isJumping || player.isUnderWater || player.world.environment == World.Environment.NETHER) return@sync

            val cloudCenter = player.eyeLocation.add(0.0, CLOUD_HEIGHT, 0.0)
            Executors.async { t -> spawnRain(cloudCenter, player) }
        }

        player.fishHook?.let {
            DUST_COLOR_TRANSITION.withTransitionColor(COLORS.random(), 0.8f, COLORS.random())
                .spawn(it.location.add(random.nextDouble(-0.1, 0.1), 0.0, random.nextDouble(-0.1, 0.1)))
        }
    }

    private fun spawnRain(cloudCenter: Location, player: Player) {
        cloudCenter.world.spawnParticle(
            Particle.CLOUD, cloudCenter, 20, 0.6, 0.1, 0.6,
            0.0
        )

        repeat(DROPS_PER_TICK) {
            val angle = Random.nextDouble(0.0, 2.0 * Math.PI)
            val radius = 1.5
            val dist = radius * sqrt(Random.nextDouble())
            val start = cloudCenter.clone().add(
                cos(angle) * dist,
                1.0,
                sin(angle) * dist
            )

            start.sync {
                val hit = start.world.rayTraceBlocks(start, BukkitVectors.DOWN, MAX_FALL, FluidCollisionMode.SOURCE_ONLY, true)
                val groundY = hit?.hitPosition?.y ?: (start.y - MAX_FALL)
                val hitBlock = hit?.hitBlock?.takeIf { BLOCK_EFFECTS.contains(it.type) && player.canBuild(it.location) }
                fallDrop(start, groundY, hitBlock)
            }
        }
    }

    private fun fallDrop(from: Location, groundY: Double, hitBlock: Block?) {
        val pos = from.clone()

        Executors.asyncTimer(0, 1) { task ->
            pos.y -= FALL_SPEED

            if (pos.y <= groundY) {
                task.cancel() // cancel first

                hitBlock?.let { block ->
                    block.sync {
                        BLOCK_EFFECTS[block.type]?.accept(block)
                    }
                }
                return@asyncTimer
            }

            RAIN.spawn(pos)
        }
    }
}